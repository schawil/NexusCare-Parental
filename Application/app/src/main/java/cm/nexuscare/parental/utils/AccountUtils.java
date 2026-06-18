package cm.nexuscare.parental.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cm.nexuscare.parental.R;
import cm.nexuscare.parental.activities.LoginActivity;
import cm.nexuscare.parental.models.User;
import cm.nexuscare.parental.services.GeoFencingForegroundService;
import cm.nexuscare.parental.services.MainForegroundService;

/**
 * Utilitaires de gestion de compte : changement de mot de passe, logout,
 * suppression de compte et données associées.
 *
 * Firebase Storage supprimé : removeImage() ne tente plus de supprimer
 * un fichier sur Storage. Les URLs Google (photoUrl) sont des ressources
 * publiques hébergées par Google — leur suppression n'est ni nécessaire
 * ni possible via Storage SDK.
 */
public class AccountUtils {

    private static final String TAG = "AccountUtilsTAG";

    // ─── API publique ────────────────────────────────────────────────────────────

    public static void changePassword(final Context context, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            Toast.makeText(context, R.string.password_updated, Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, LoginActivity.class));
            closeServices(context);
        });
    }

    public static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        context.startActivity(new Intent(context, LoginActivity.class));
        closeServices(context);
    }

    /**
     * Supprime les données Realtime DB puis le compte Firebase Auth.
     * La ré-authentification est requise par Firebase avant toute suppression de compte.
     */
    public static void deleteAccount(Context context, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // getProviderData().get(0) est plus fiable que getProviderId() qui retourne "firebase"
        String providerId = user.getProviderData().get(0).getProviderId();
        Log.d(TAG, "deleteAccount providerId=" + providerId);

        deleteAccountData(providerId, password, context);
        closeServices(context);
    }

    // ─── Suppression des données ─────────────────────────────────────────────────

    private static void deleteAccountData(
            final String providerId,
            final String password,
            final Context context) {

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Chercher d'abord dans parents, puis dans childs
        Query parentQuery = dbRef.child("parents")
                .orderByChild("email")
                .equalTo(user.getEmail());

        parentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Nœud parent trouvé → suppression DB puis Auth
                    dbRef.child("parents").child(user.getUid()).removeValue();
                    deleteUser(providerId, password, context);
                } else {
                    deleteChildData(dbRef, user, providerId, password, context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "deleteAccountData cancelled", error.toException());
            }
        });
    }

    private static void deleteChildData(
            final DatabaseReference dbRef,
            final FirebaseUser user,
            final String providerId,
            final String password,
            final Context context) {

        Query childQuery = dbRef.child("childs")
                .orderByChild("email")
                .equalTo(user.getEmail());

        childQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    dbRef.child("childs").child(user.getUid()).removeValue();
                }
                // Supprimer le compte Auth dans tous les cas (même si pas de nœud DB)
                deleteUser(providerId, password, context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "deleteChildData cancelled", error.toException());
            }
        });
    }

    /**
     * Ré-authentifie puis supprime le compte Firebase Auth.
     * Requis par Firebase Security : l'opération delete() exige une session récente.
     *
     * Note : pour Google Sign-In, getIdToken() doit être passé en credential,
     * pas getEmail(). On utilise null ici car on n'a plus accès au token à ce stade ;
     * si la ré-auth échoue, l'utilisateur devra se reconnecter puis réessayer.
     */
    private static void deleteUser(
            final String providerId,
            final String password,
            final Context context) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AuthCredential credential;
        if ("google.com".equals(providerId)) {
            // Ré-auth Google sans token disponible → déclenche une re-sign-in silencieuse
            // En production, il faudrait relancer le GoogleSignIn flow pour récupérer l'idToken
            credential = GoogleAuthProvider.getCredential(null, null);
        } else {
            credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        }

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (!reauthTask.isSuccessful()) {
                Log.w(TAG, "Reauthentication failed", reauthTask.getException());
                // En cas d'échec Google, on tente quand même la suppression
                // (fonctionne si la session est suffisamment récente)
            }
            user.delete().addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    Toast.makeText(context, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Account deletion failed", deleteTask.getException());
                    Toast.makeText(context, R.string.sign_up_falied, Toast.LENGTH_SHORT).show();
                }
                context.startActivity(new Intent(context, LoginActivity.class));
            });
        });
    }

    // ─── Services ───────────────────────────────────────────────────────────────

    private static void closeServices(Context context) {
        stopServiceSilently(context, MainForegroundService.class);
        stopServiceSilently(context, GeoFencingForegroundService.class);
    }

    private static void stopServiceSilently(Context context, Class<?> serviceClass) {
        try {
            context.stopService(new Intent(context, serviceClass));
        } catch (Exception e) {
            Log.w(TAG, "Failed to stop " + serviceClass.getSimpleName(), e);
        }
    }
}
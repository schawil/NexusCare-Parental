package cm.nexuscare.parental.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cm.nexuscare.parental.R;
import cm.nexuscare.parental.dialogfragments.ConfirmationDialogFragment;
import cm.nexuscare.parental.dialogfragments.GoogleChildSignUpDialogFragment;
import cm.nexuscare.parental.dialogfragments.InformationDialogFragment;
import cm.nexuscare.parental.dialogfragments.LoadingDialogFragment;
import cm.nexuscare.parental.interfaces.OnConfirmationListener;
import cm.nexuscare.parental.interfaces.OnGoogleChildSignUp;
import cm.nexuscare.parental.models.Child;
import cm.nexuscare.parental.models.Parent;
import cm.nexuscare.parental.utils.Constant;
import cm.nexuscare.parental.utils.Validators;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Gère l'inscription email/password et Google Sign-In.
 * Firebase Storage supprimé : les photos de profil Google sont référencées
 * via l'URL publique du compte Google ; les photos custom sont ignorées
 * (Storage non activé sur le plan Spark du projet).
 */
public class SignUpActivity extends AppCompatActivity implements OnConfirmationListener, OnGoogleChildSignUp {

    private static final String TAG = "SignUpActivityTAG";

    private DatabaseReference databaseReference;
    private Uri imageUri;
    private FirebaseAuth auth;

    private EditText txtSignUpEmail;
    private EditText txtParentEmail;
    private EditText txtSignUpPassword;
    private EditText txtSignUpName;
    private Button btnSignUp;
    private Button btnSignUpWithGoogle;
    private CircleImageView imgProfile;
    private FragmentManager fragmentManager;

    private String uid;
    private boolean googleAuth  = false;
    private boolean parent      = true;
    private boolean validParent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        fragmentManager   = getSupportFragmentManager();
        parent            = getIntent().getBooleanExtra(Constant.PARENT_SIGN_UP, true);
        auth              = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        bindViews();
        setupParentEmailWatcher();
        setupClickListeners();
    }

    // ─── Binding ────────────────────────────────────────────────────────────────

    private void bindViews() {
        txtSignUpEmail    = findViewById(R.id.txtSignUpEmail);
        txtParentEmail    = findViewById(R.id.txtParentEmail);
        txtSignUpPassword = findViewById(R.id.txtSignUpPassword);
        txtSignUpName     = findViewById(R.id.txtSignUpName);
        btnSignUp         = findViewById(R.id.btnSignUp);
        btnSignUpWithGoogle = findViewById(R.id.btnSignUpWithGoogle);
        imgProfile        = findViewById(R.id.imgProfile);

        if (!parent) txtParentEmail.setVisibility(View.VISIBLE);
    }

    private void setupParentEmailWatcher() {
        txtParentEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Requête temps-réel pour valider l'existence du parent dans la DB
                Query query = databaseReference.child("parents")
                        .orderByChild("email")
                        .equalTo(editable.toString().toLowerCase().trim());

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        validParent = snapshot.exists();
                        Log.d(TAG, "validParent=" + validParent);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "Parent email validation cancelled", error.toException());
                    }
                });
            }
        });
    }

    private void setupClickListeners() {
        imgProfile.setOnClickListener(v -> openFileChooser());

        btnSignUp.setOnClickListener(v -> signUp(
                txtSignUpEmail.getText().toString().toLowerCase().trim(),
                txtSignUpPassword.getText().toString()
        ));

        btnSignUpWithGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    // ─── Inscription email/password ─────────────────────────────────────────────

    private void signUp(String email, String password) {
        if (!isValid()) return;

        LoadingDialogFragment loading = new LoadingDialogFragment();
        startLoadingFragment(loading);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    stopLoadingFragment(loading);
                    if (task.isSuccessful()) {
                        signUpRoutine(txtParentEmail.getText().toString().toLowerCase().trim());
                    } else {
                        handleAuthError((FirebaseAuthException) task.getException());
                    }
                });
    }

    private void handleAuthError(FirebaseAuthException e) {
        if (e == null) {
            Toast.makeText(this, getString(R.string.sign_up_falied), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (e.getErrorCode()) {
            case "ERROR_INVALID_EMAIL":
                txtSignUpEmail.setError(getString(R.string.enter_valid_email));
                break;
            case "ERROR_EMAIL_ALREADY_IN_USE":
                txtSignUpEmail.setError(getString(R.string.email_is_already_in_use));
                break;
            case "ERROR_WEAK_PASSWORD":
                txtSignUpPassword.setError(getString(R.string.weak_password));
                break;
            default:
                Toast.makeText(this, getString(R.string.sign_up_falied), Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Routine post-authentification ──────────────────────────────────────────

    private void signUpRoutine(String parentEmail) {
        uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "signUpRoutine uid=" + uid);
        addUserToDB(parentEmail);
        persistProfileImage();
        startAccountVerificationActivity();
    }

    /**
     * Stratégie photo de profil sans Firebase Storage :
     * - Auth Google  → on stocke l'URL publique Google (hébergée par Google, pas par nous)
     * - Auth email   → on stocke null ; l'UI affichera l'avatar par défaut
     * Aucun upload tiers, aucune dépendance Storage.
     */
    private void persistProfileImage() {
        String nodeKey = parent ? "parents" : "childs";

        if (googleAuth) {
            // L'URL photo Google est publique et ne nécessite pas Storage
            FirebaseUser user = auth.getCurrentUser();
            if (user != null && user.getPhotoUrl() != null) {
                databaseReference.child(nodeKey).child(uid)
                        .child("profileImage")
                        .setValue(user.getPhotoUrl().toString());
            }
            // Si pas de photo Google → null en DB, UI charge l'avatar par défaut
        }
        // Auth email : pas d'upload Storage disponible → profileImage reste null en DB
    }

    private void addUserToDB(String parentEmail) {
        String email;
        String name;

        if (googleAuth) {
            FirebaseUser user = auth.getCurrentUser();
            email = user.getEmail();
            name  = user.getDisplayName();
        } else {
            email = txtSignUpEmail.getText().toString().toLowerCase().trim();
            name  = txtSignUpName.getText().toString().replaceAll("\\s+$", "");
        }

        if (parent) {
            databaseReference.child("parents").child(uid).setValue(new Parent(name, email));
        } else {
            databaseReference.child("childs").child(uid).setValue(new Child(name, email, parentEmail));
        }
    }

    // ─── Google Sign-In ─────────────────────────────────────────────────────────

    private void signInWithGoogle() {
        if (!Validators.isInternetAvailable(this)) {
            startInformationDialogFragment();
            return;
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        startActivityForResult(client.getSignInIntent(), Constant.RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        googleAuth = true;
                        // Pour un enfant Google, demander l'email du parent via dialog
                        if (!parent) getParentEmail();
                        else signUpRoutine("");
                    } else {
                        Toast.makeText(this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getParentEmail() {
        GoogleChildSignUpDialogFragment dialog = new GoogleChildSignUpDialogFragment();
        dialog.setCancelable(false);
        dialog.show(fragmentManager, Constant.GOOGLE_CHILD_SIGN_UP);
    }

    // ─── Validation ─────────────────────────────────────────────────────────────

    private boolean isValid() {
        if (!Validators.isValidName(txtSignUpName.getText().toString())) {
            txtSignUpName.setError(getString(R.string.name_validation));
            txtSignUpName.requestFocus();
            return false;
        }
        if (!Validators.isValidEmail(txtSignUpEmail.getText().toString())) {
            txtSignUpEmail.setError(getString(R.string.enter_valid_email));
            txtSignUpEmail.requestFocus();
            return false;
        }
        if (!parent) {
            if (!Validators.isValidEmail(txtParentEmail.getText().toString()) || !validParent) {
                txtParentEmail.setError(getString(R.string.this_email_isnt_registered_as_parent));
                txtParentEmail.requestFocus();
                return false;
            }
        }
        if (!Validators.isValidPassword(txtSignUpPassword.getText().toString())) {
            txtSignUpPassword.setError(getString(R.string.enter_valid_password));
            txtSignUpPassword.requestFocus();
            return false;
        }
        // Photo optionnelle : si absente, proposer confirmation avant de continuer sans avatar
        if (!Validators.isValidImageURI(imageUri)) {
            ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.CONFIRMATION_MESSAGE,
                    getString(R.string.would_you_love_to_add_a_profile_image));
            dialog.setArguments(bundle);
            dialog.setCancelable(false);
            dialog.show(fragmentManager, Constant.CONFIRMATION_FRAGMENT_TAG);
            return false;
        }
        if (!Validators.isInternetAvailable(this)) {
            startInformationDialogFragment();
            return false;
        }
        return true;
    }

    // ─── Navigation & Dialogs ───────────────────────────────────────────────────

    private void startAccountVerificationActivity() {
        startActivity(new Intent(this, AccountVerificationActivity.class));
    }

    private void startLoadingFragment(LoadingDialogFragment f) {
        f.setCancelable(false);
        f.show(fragmentManager, Constant.LOADING_FRAGMENT);
    }

    private void stopLoadingFragment(LoadingDialogFragment f) {
        f.dismiss();
    }

    private void startInformationDialogFragment() {
        InformationDialogFragment dialog = new InformationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.INFORMATION_MESSAGE,
                getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));
        dialog.setArguments(bundle);
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST);
    }

    // ─── Activity Results ───────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
            // imageUri est gardé en mémoire uniquement pour l'affichage local ;
            // il ne sera pas uploadé (Storage non disponible)
        }

        if (requestCode == Constant.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                firebaseAuthWithGoogle(task.getResult(ApiException.class));
            } catch (ApiException e) {
                Log.w(TAG, "Google sign-in failed", e);
                Toast.makeText(this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── Callbacks interfaces ───────────────────────────────────────────────────

    /** L'utilisateur veut ajouter une photo → on remet le focus sur le sélecteur */
    @Override
    public void onConfirm() {
        imgProfile.requestFocus();
        Toast.makeText(this, getString(R.string.please_add_a_profile_image), Toast.LENGTH_SHORT).show();
    }

    /** L'utilisateur confirme qu'il veut continuer sans photo → on force imageUri à non-null */
    @Override
    public void onConfirmationCancel() {
        // Uri resource drawable : satisfait isValidImageURI() sans upload
        imageUri = Uri.parse("android.resource://cm.nexuscare.parental/drawable/ic_default_avatar");
        signUp(
            txtSignUpEmail.getText().toString().toLowerCase().trim(),
            txtSignUpPassword.getText().toString()
        );
    }

    @Override
    public void onModeSelected(String parentEmail) {
        signUpRoutine(parentEmail);
    }
}
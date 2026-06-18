package cm.nexuscare.parental.dialogfragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Dialog;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cm.nexuscare.parental.R;
import cm.nexuscare.parental.interfaces.OnPasswordResetListener;
import cm.nexuscare.parental.utils.Validators;

public class RecoverPasswordDialogFragment extends DialogFragment {
	private EditText txtRecoveryEmail;
	private Button btnRecoverPassword;
	private Button btnCancelRecoverPassword;
	private OnPasswordResetListener onPasswordResetListener;

    @NonNull

    @Override

    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        android.app.Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;

    }

    @Override

    public void onStart() {

        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {

            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        }

    }


	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_dialog_recover_password, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		onPasswordResetListener = (OnPasswordResetListener) getActivity();
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		txtRecoveryEmail = view.findViewById(R.id.txtRecoveryEmail);
		
		
		btnRecoverPassword = view.findViewById(R.id.btnRecoverPassword);
		btnRecoverPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = txtRecoveryEmail.getText().toString();
				if (Validators.isValidEmail(email)) {
					onPasswordResetListener.onOkClicked(email);
					dismiss();
				} else {
					txtRecoveryEmail.setError(getString(R.string.enter_valid_email));
					txtRecoveryEmail.requestFocus();
				}
			}
		});
		
		btnCancelRecoverPassword = view.findViewById(R.id.btnCancelRecoverPassword);
		btnCancelRecoverPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPasswordResetListener.onCancelClicked();
				dismiss();
			}
		});
	}
}

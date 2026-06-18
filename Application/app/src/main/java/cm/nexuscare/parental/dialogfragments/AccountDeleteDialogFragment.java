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
import cm.nexuscare.parental.interfaces.OnDeleteAccountListener;
import cm.nexuscare.parental.utils.Constant;
import cm.nexuscare.parental.utils.SharedPrefsUtils;
import cm.nexuscare.parental.utils.Validators;

public class AccountDeleteDialogFragment extends DialogFragment {
	private EditText txtDeleteAccountPassword;
	private Button btnDeleteAccount;
	private Button btnCancelDeleteAccount;
	private OnDeleteAccountListener onDeleteAccountListener;

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
		return inflater.inflate(R.layout.fragment_dialog_delete_account, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		onDeleteAccountListener = (OnDeleteAccountListener) getActivity();
		
		txtDeleteAccountPassword = view.findViewById(R.id.txtDeleteAccountPassword);
		btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
		btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isValid()) {
					onDeleteAccountListener.onDeleteAccount(txtDeleteAccountPassword.getText().toString());
					dismiss();
				}
			}
		});
		
		
		btnCancelDeleteAccount = view.findViewById(R.id.btnCancelDeleteAccount);
		btnCancelDeleteAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		
		
	}
	
	private boolean isValid() {
		if (!Validators.isValidPassword(txtDeleteAccountPassword.getText().toString())) {
			txtDeleteAccountPassword.setError(getString(R.string.wrong_password));
			txtDeleteAccountPassword.requestFocus();
			return false;
		}
		
		if (!txtDeleteAccountPassword.getText().toString().equals(SharedPrefsUtils.getStringPreference(getContext(), Constant.PASSWORD, ""))) {
			txtDeleteAccountPassword.setError(getString(R.string.wrong_password));
			txtDeleteAccountPassword.requestFocus();
			return false;
		}
		
		return true;
	}
}

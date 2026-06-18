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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cm.nexuscare.parental.R;
import cm.nexuscare.parental.interfaces.OnConfirmationListener;
import cm.nexuscare.parental.utils.Constant;

public class ConfirmationDialogFragment extends DialogFragment {
	private Button btnConfirm;
	private Button btnCancelConfirm;
	private TextView txtConfirmationBody;
	private OnConfirmationListener onConfirmationListener;

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
		return inflater.inflate(R.layout.fragment_dialog_confirmation, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		onConfirmationListener = (OnConfirmationListener) getActivity();
		
		Bundle bundle = getArguments();
		String confirmationMessage = bundle.getString(Constant.CONFIRMATION_MESSAGE);
		
		txtConfirmationBody = view.findViewById(R.id.txtConfirmationBody);
		txtConfirmationBody.setText(confirmationMessage);
		
		btnConfirm = view.findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onConfirmationListener.onConfirm();
				dismiss();
			}
			
		});
		
		btnCancelConfirm = view.findViewById(R.id.btnCancelConfirm);
		btnCancelConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onConfirmationListener.onConfirmationCancel();
				dismiss();
			}
		});
		
	}
}

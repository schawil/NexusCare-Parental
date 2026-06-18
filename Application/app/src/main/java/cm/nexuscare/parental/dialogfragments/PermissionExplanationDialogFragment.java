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
import cm.nexuscare.parental.interfaces.OnPermissionExplanationListener;
import cm.nexuscare.parental.utils.Constant;


public class PermissionExplanationDialogFragment extends DialogFragment {
	private TextView txtPermissionBody;
	private Button btnOkPermission;
	private Button btnCancelPermission;
	private OnPermissionExplanationListener onPermissionExplanationListener;

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
		return inflater.inflate(R.layout.fragment_dialog_permission_explanation, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		txtPermissionBody = view.findViewById(R.id.txtPermissionBody);
		if (getTargetFragment() != null)
			onPermissionExplanationListener = (OnPermissionExplanationListener) getTargetFragment();
		else onPermissionExplanationListener = (OnPermissionExplanationListener) getActivity();
		
		Bundle bundle = getArguments();
		final int requestCode = bundle.getInt(Constant.PERMISSION_REQUEST_CODE);
		final int switchId = bundle.getInt(Constant.SWITCH_ID);
		
		switch (requestCode) {
			case Constant.SEND_SMS_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.send_sms_explanation));
				break;
			case Constant.READ_SMS_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.read_sms_explanation));
				break;
			case Constant.RECEIVE_SMS_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.receive_sms_explanation));
				break;
			case Constant.READ_PHONE_STATE_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.read_phone_state_explanation));
				break;
			case Constant.READ_CALL_LOG_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.read_call_log_explanation));
				break;
			case Constant.READ_CONTACTS_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.read_contacts_explanation));
				break;
			case Constant.LOCATION_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.location_permission_explanation));
				break;
			case Constant.WRITE_SETTINGS_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.write_settings_permission_explanation));
				break;
			case Constant.OVERLAY_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.overlay_permission_explanation));
				break;
			case Constant.PACKAGE_USAGE_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.package_usage_permission_explanation));
				break;
			case Constant.CALL_PHONE_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.phone_call_permission_explanation));
				break;
			case Constant.USER_LOCATION_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.cant_start_the_fence_without_your_location));
				break;
			case Constant.CHILD_LOCATION_PERMISSION_REQUEST_CODE:
				txtPermissionBody.setText(getString(R.string.please_enable_your_gps));
				break;
			default:
				txtPermissionBody.setText(getString(R.string.please_allow_the_following_permissions));
				break;
			
		}
		
		btnOkPermission = view.findViewById(R.id.btnOkPermission);
		btnOkPermission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPermissionExplanationListener.onOk(requestCode);
				dismiss();
			}
		});
		
		btnCancelPermission = view.findViewById(R.id.btnCancelPermission);
		btnCancelPermission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPermissionExplanationListener.onCancel(switchId);
				dismiss();
			}
		});
		
	}
}

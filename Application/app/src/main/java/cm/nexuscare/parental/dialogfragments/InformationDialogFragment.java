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
import cm.nexuscare.parental.utils.Constant;

public class InformationDialogFragment extends DialogFragment {
	private TextView txtInformationBody;
	private Button btnInformationOk;

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
		return inflater.inflate(R.layout.fragment_dialog_information, container, false);
	}
	
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		Bundle bundle = getArguments();
		String message = bundle.getString(Constant.INFORMATION_MESSAGE);
		
		txtInformationBody = view.findViewById(R.id.txtInformationBody);
		txtInformationBody.setText(message);
		btnInformationOk = view.findViewById(R.id.btnInformationOk);
		btnInformationOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
	}
}

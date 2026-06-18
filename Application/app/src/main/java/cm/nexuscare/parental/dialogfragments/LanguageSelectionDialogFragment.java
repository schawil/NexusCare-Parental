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
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cm.nexuscare.parental.R;
import cm.nexuscare.parental.interfaces.OnLanguageSelectionListener;
import cm.nexuscare.parental.utils.Constant;
import cm.nexuscare.parental.utils.SharedPrefsUtils;

public class LanguageSelectionDialogFragment extends DialogFragment {
	private Spinner spinnerLanguageEntries;
	private Button btnOkLanguageSelection;
	private Button btnCancelLanguageSelection;
	private OnLanguageSelectionListener onLanguageSelectionListener;

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
		return inflater.inflate(R.layout.fragment_dialog_language_selection, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		onLanguageSelectionListener = (OnLanguageSelectionListener) getActivity();
		
		String appLanguage = SharedPrefsUtils.getStringPreference(getContext(), Constant.APP_LANGUAGE, "en");
		spinnerLanguageEntries = view.findViewById(R.id.spinnerLanguageEntries);
		if (appLanguage.equals("en")) spinnerLanguageEntries.setSelection(0);
		else if (appLanguage.equals("ar")) spinnerLanguageEntries.setSelection(1);
		
		
		btnOkLanguageSelection = view.findViewById(R.id.btnOkLanguageSelection);
		btnOkLanguageSelection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onLanguageSelectionListener.onLanguageSelection(spinnerLanguageEntries.getSelectedItem().toString());
				dismiss();
			}
		});
		
		btnCancelLanguageSelection = view.findViewById(R.id.btnCancelLanguageSelection);
		btnCancelLanguageSelection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		
		
	}
}

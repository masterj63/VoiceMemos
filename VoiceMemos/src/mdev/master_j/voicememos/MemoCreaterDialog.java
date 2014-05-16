package mdev.master_j.voicememos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MemoCreaterDialog extends DialogFragment {
	private static final int ERROR_DURATION_MS = 1000;

	private View dialogView;
	private AlertDialog dialog;
	private EditText memoNameEditText;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = inflater.inflate(R.layout.memo_creator, null, false);

		builder.setView(dialogView);
		builder.setTitle("title");
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNeutralButton(R.string.button_record_start, null);

		dialog = builder.create();
		// dialog.setOnShowListener(new OnDialogShowListener(dialog));
		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();

		memoNameEditText = (EditText) dialogView.findViewById(R.id.name_memo_dialog);
		memoNameEditText.addTextChangedListener(textWatcher);

		Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(onPositiveButtonClickListener);

		Button neutralButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);
		neutralButton.setOnClickListener(onNeutralButtonClickListener);
	}

	private OnClickListener onNeutralButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO recording
			Toast.makeText(getActivity(), "i am recording", Toast.LENGTH_SHORT).show();
		}
	};

	private OnClickListener onPositiveButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String memoNameString = memoNameEditText.getText().toString().trim();

			if (memoNameString.length() == 0) {
				memoNameEditText.setError("Cannot be empty");
				memoNameEditText.removeCallbacks(textViewErrorRemoverRunnable);
				memoNameEditText.postDelayed(textViewErrorRemoverRunnable, ERROR_DURATION_MS);
				return;
			}
		}
	};

	private Runnable textViewErrorRemoverRunnable = new Runnable() {
		@Override
		public void run() {
			memoNameEditText.setError(null);
		}
	};

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
}

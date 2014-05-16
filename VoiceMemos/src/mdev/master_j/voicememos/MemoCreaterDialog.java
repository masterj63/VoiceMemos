package mdev.master_j.voicememos;

import mdev.master_j.voicememos.MainActivity.Memo;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class MemoCreaterDialog extends DialogFragment {
	private static final int DURATION_ERROR_MS = 1000;
	private static final int DURATION_RECORD_MS = 10 * 1000;
	private static final int DURATION_PROGRESSBAR_FADE_IN_MS = 1000;

	private MainActivity mainActivity;

	private View dialogView;
	private AlertDialog dialog;
	private EditText memoNameEditText;

	private Button positiveButton;
	private Button neutralButton;

	ProgressBar recordProgressBar;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = inflater.inflate(R.layout.memo_creator, null, false);

		builder.setView(dialogView);
		builder.setTitle("title");
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNeutralButton(R.string.button_record_start, null);

		dialog = builder.create();
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		recordProgressBar = (ProgressBar) dialogView.findViewById(R.id.progress_bar_record);

		memoNameEditText = (EditText) dialogView.findViewById(R.id.name_memo_dialog);
		memoNameEditText.addTextChangedListener(textWatcher);

		positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(onPositiveButtonClickListener);

		neutralButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);
		neutralButton.setOnClickListener(onNeutralButtonClickListener);

		textWatcher.afterTextChanged(memoNameEditText.getText());
	}

	private OnClickListener onNeutralButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			neutralButton.setText(R.string.button_record_stop);

			Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
			fadeInAnimation.setDuration(DURATION_PROGRESSBAR_FADE_IN_MS);
			fadeInAnimation.setStartOffset(0);
			recordProgressBar.startAnimation(fadeInAnimation);
			recordProgressBar.findViewById(R.id.progress_bar_record).setVisibility(View.VISIBLE);
		}
	};

	private OnClickListener onPositiveButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO save memo
		}
	};

	private TextWatcher textWatcher = new TextWatcher() {
		boolean changedProgramatically = false;

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
			if (changedProgramatically) {
				changedProgramatically = false;
				return;
			}

			String name = editable.toString().trim();
			if (name.length() == 0) {
				positiveButton.setEnabled(false);
				return;
			}

			positiveButton.setEnabled(true);

			boolean duplicateFound = false;
			for (Memo memo : mainActivity.memoList)
				if (memo.name.equals(name)) {
					duplicateFound = true;
					break;
				}

			if (!duplicateFound)
				return;

			for (int i = 1; i <= 999; i++) {
				String newSuffix = ' ' + String.format("%03d", i);
				String newName = name + newSuffix;

				boolean newNameIsOk = true;
				for (Memo memo : mainActivity.memoList)
					if (memo.name.equals(newName)) {
						newNameIsOk = false;
						break;
					}
				if (!newNameIsOk)
					continue;

				changedProgramatically = true;
				editable.append(newSuffix);
				return;
			}

			positiveButton.setEnabled(false);
			memoNameEditText.setError(getString(R.string.error_too_many_similar_memos));
		}
	};
}

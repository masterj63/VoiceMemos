package mdev.master_j.voicememos;

import java.io.File;
import java.io.IOException;

import mdev.master_j.voicememos.MainActivity.Memo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MemoCreaterDialog extends DialogFragment {
	private static final int PERIOD_RECORD_PROGRESS_UPDATE_MS = 200;
	private static final int DURATION_RECORD_MS = 10 * 1000;
	private static final int DURATION_PROGRESSBAR_FADE_IN_MS = 1000;

	private MainActivity mainActivity;

	private View dialogView;
	private AlertDialog dialog;
	private EditText memoNameEditText;

	private Button positiveButton;
	private Button neutralButton;

	private RecordState recordState = RecordState.IDLE;

	ProgressBar recordProgressBar;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();

		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

		LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		recordProgressBar.setMax(DURATION_RECORD_MS);
		recordProgressBar.setProgress(0);

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
			switch (recordState) {
			case IDLE:
				onStartRecord();
				break;
			case IN_PROGRESS:
				break;
			case RECORDED:
				break;
			default:
				Log.d("mj", "recordState==null?");
			}
		}
	};

	private void onStartRecord() {
		recordState = RecordState.IN_PROGRESS;

		neutralButton.setText(R.string.button_record_stop);

		Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
		fadeInAnimation.setDuration(DURATION_PROGRESSBAR_FADE_IN_MS);
		fadeInAnimation.setStartOffset(0);
		recordProgressBar.startAnimation(fadeInAnimation);
		recordProgressBar.findViewById(R.id.progress_bar_record).setVisibility(View.VISIBLE);

		MediaRecorder recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

		String path = mainActivity.getFilesDir() + "/" + memoNameEditText.getText().toString();
		File outputFile = new File(path);
		if (outputFile.exists())
			outputFile.delete();
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_LONG).show();
			Log.e("mj", e.toString());
			e.printStackTrace();
			return;
		}
		recorder.setOutputFile(path);

		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			recorder.prepare();
		} catch (Throwable e) {
			Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_LONG).show();
			Log.e("mj", e.toString());
			e.printStackTrace();
			return;
		}

		new Thread(recordProgressUpdaterRunnable).start();
		recorder.start();
	}

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

	private Runnable recordProgressUpdaterRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				int progress = 0;
				while (progress <= DURATION_RECORD_MS) {
					progress += PERIOD_RECORD_PROGRESS_UPDATE_MS;
					recordProgressBar.setProgress(progress);
					Thread.sleep(PERIOD_RECORD_PROGRESS_UPDATE_MS);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.e("mj", e.toString());
			}
			// TODO on finish
		}
	};

	private enum RecordState {
		IDLE, IN_PROGRESS, RECORDED
	}
}

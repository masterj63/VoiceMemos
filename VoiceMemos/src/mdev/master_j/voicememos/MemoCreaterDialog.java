package mdev.master_j.voicememos;

import java.io.File;

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
	private static final int DURATION_RECORD_MS = 4 * 1000;
	private static final int DURATION_PROGRESSBAR_FADE_IN_MS = 1000;

	private static final String FILENAME_RECORD_TMP = " tmp ";

	private MainActivity mainActivity;

	private String tmpOutputFilePath;

	private View dialogView;
	private AlertDialog dialog;

	private EditText memoNameEditText;

	private Button positiveButton;
	private Button neutralButton;

	private ProgressBar recordProgressBar;

	private RecordState recordState = RecordState.IDLE;

	private MediaRecorder mediaRecorder;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();

		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

		LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dialogView = inflater.inflate(R.layout.memo_creator, null, false);

		builder.setView(dialogView);
		builder.setTitle(R.string.label_add_memo);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setNeutralButton(R.string.button_record_start, null);

		dialog = builder.create();
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		tmpOutputFilePath = mainActivity.getFilesDir() + "/" + FILENAME_RECORD_TMP;

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

	@Override
	public void onDestroy() {
		super.onDestroy();
		deleteTmpOutputFile();
	}

	private OnClickListener onNeutralButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (recordState) {
			case IDLE:
				onStartRecord();
				break;
			case IN_PROGRESS:
				// this will automatically lead to stopping a record, @see
				// recordProgressUpdaterRunnable
				recordState = RecordState.RECORDED;
				break;
			case RECORDED:
				onStartRecord();
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

		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

		mediaRecorder.setOutputFile(tmpOutputFilePath);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mediaRecorder.prepare();
		} catch (Throwable e) {
			Toast.makeText(mainActivity, e.toString(), Toast.LENGTH_LONG).show();
			Log.e("mj", e.toString());
			e.printStackTrace();
			return;
		}

		new Thread(recordProgressUpdaterRunnable).start();
		mediaRecorder.start();
	}

	private void deleteTmpOutputFile() {
		File outputFile = new File(tmpOutputFilePath);
		outputFile.delete();
	}

	private OnClickListener onPositiveButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String memoName = memoNameEditText.getText().toString();
			String memoAudioFile = mainActivity.getFilesDir() + "/" + memoName;

			File tmpFile = new File(tmpOutputFilePath);
			File newFile = new File(memoAudioFile);
			tmpFile.renameTo(newFile);

			mainActivity.saveNewMemo(memoNameEditText.getText().toString());
			dismiss();
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
			if (recordState == RecordState.IN_PROGRESS)
				neutralButton.setText(R.string.button_record_stop);
			else
				neutralButton.setText(R.string.button_record_start);

			if (changedProgramatically) {
				changedProgramatically = false;
				return;
			}

			String name = editable.toString().trim();
			if (name.length() == 0) {
				positiveButton.setEnabled(false);
				return;
			}

			if (recordState == RecordState.RECORDED)
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
				while (progress <= DURATION_RECORD_MS && recordState == RecordState.IN_PROGRESS) {
					progress += PERIOD_RECORD_PROGRESS_UPDATE_MS;
					recordProgressBar.setProgress(progress);
					Thread.sleep(PERIOD_RECORD_PROGRESS_UPDATE_MS);
				}
				recordState = RecordState.RECORDED;

				dialogView.post(new Runnable() {
					@Override
					public void run() {
						textWatcher.afterTextChanged(memoNameEditText.getText());
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.e("mj", e.toString());
				recordState = RecordState.IDLE;
				deleteTmpOutputFile();
			} finally {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
			}
		}
	};

	private enum RecordState {
		IDLE, IN_PROGRESS, RECORDED
	}
}

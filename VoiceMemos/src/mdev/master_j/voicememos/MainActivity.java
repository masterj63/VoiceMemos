package mdev.master_j.voicememos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	List<Memo> memoList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		updateMemoList();
	}

	private void updateMemoList() {
		SharedPreferences pp = getPreferences(MODE_PRIVATE);

		memoList = new ArrayList<Memo>();

		if (pp.getAll().isEmpty()) {
			findViewById(R.id.list_view_memos).setVisibility(View.INVISIBLE);
			findViewById(R.id.text_view_placeholder).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.list_view_memos).setVisibility(View.VISIBLE);
			findViewById(R.id.text_view_placeholder).setVisibility(View.INVISIBLE);

			CustomMemoAdapter adapter = new CustomMemoAdapter();

			for (String name : pp.getAll().keySet()) {
				Long datetimeMS = pp.getLong(name, 0L);
				Date date = new Date(datetimeMS);

				memoList.add(new Memo(name, date));
				adapter.add("");
			}

			Collections.sort(memoList, nameMemoComparator);

			ListView listView = (ListView) findViewById(R.id.list_view_memos);
			listView.setAdapter(adapter);

			// TODO OnItemClickListener
		}
	}

	void saveNewMemo(String title) {
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putLong(title, System.currentTimeMillis());
		editor.commit();
		updateMemoList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_add_memo) {
			DialogFragment memoCreatorDialog = new MemoCreaterDialog();
			memoCreatorDialog.show(getFragmentManager(), "");
		}
		return super.onOptionsItemSelected(item);
	}

	private class CustomMemoAdapter extends ArrayAdapter<String> {
		public CustomMemoAdapter() {
			super(MainActivity.this, R.layout.memo);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Memo memo = memoList.get(position);

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View memoView = inflater.inflate(R.layout.memo, parent, false);

			TextView memoName = (TextView) memoView.findViewById(R.id.name_memo);
			memoName.setText(memo.name);

			String memoDateString = new SimpleDateFormat("dd MMM yyyy").format(memo.datetime);
			TextView memoDate = (TextView) memoView.findViewById(R.id.date_memo);
			memoDate.setText(memoDateString);

			String memoTimeString = new SimpleDateFormat("HH:mm:ss").format(memo.datetime);
			TextView memoTime = (TextView) memoView.findViewById(R.id.time_memo);
			memoTime.setText(memoTimeString);

			return memoView;
		}
	}

	class Memo {
		String name;
		private Date datetime;

		public Memo(String name, Date datetime) {
			super();
			this.name = name;
			this.datetime = datetime;
		}

	}

	private Comparator<Memo> nameMemoComparator = new Comparator<Memo>() {
		@Override
		public int compare(Memo memo1, Memo memo2) {
			return memo1.name.compareTo(memo2.name);
		}
	};

	private Comparator<Memo> datetimeMemoComparator = new Comparator<Memo>() {
		@Override
		public int compare(Memo memo1, Memo memo2) {
			return memo1.datetime.compareTo(memo2.datetime);
		}
	};
}

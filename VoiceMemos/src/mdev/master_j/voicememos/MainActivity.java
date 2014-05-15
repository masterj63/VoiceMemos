package mdev.master_j.voicememos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
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
	private static final String NAME_PREFERENCE_NAME = "mdev.master_j.voicememos.MainActivity.NAME_PREFERENCE_NAME";
	private static final String NAME_PREFERENCE_DATETIME = "mdev.master_j.voicememos.MainActivity.NAME_PREFERENCE_DATETIME";

	private static final String KEY_MEMO_NAME = "mdev.master_j.voicememos.MainActivity.KEY_MEMO_NAME";
	private static final String KEY_MEMO_DATETIME = "mdev.master_j.voicememos.MainActivity.KEY_MEMO_DATETIME";

	private List<Memo> memoList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences namePreferences = getSharedPreferences(NAME_PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences datetimePreferences = getSharedPreferences(NAME_PREFERENCE_DATETIME, MODE_PRIVATE);

		if (namePreferences.getAll().isEmpty()) {
			findViewById(R.id.text_view_placeholder).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.list_view_memos).setVisibility(View.VISIBLE);

			memoList = new ArrayList<Memo>();

			CustomMemoAdapter adapter = new CustomMemoAdapter();

			for (String key : namePreferences.getAll().keySet()) {
				String name = namePreferences.getString(KEY_MEMO_NAME, "");
				Long datetimeMS = datetimePreferences.getLong(KEY_MEMO_DATETIME, 0L);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_add_memo) {

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

			TextView memoDate = (TextView) memoView.findViewById(R.id.date_memo);
			memoDate.setText(memo.datetime.toString());

			// TextView memoTime = (TextView)
			// memoView.findViewById(R.id.time_memo);
			// TODO fix that

			return super.getView(position, convertView, parent);
		}

	}

	private class Memo {
		private String name;
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

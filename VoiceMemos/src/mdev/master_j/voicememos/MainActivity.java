package mdev.master_j.voicememos;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String KEY_MEMOS_CREATED = "mdev.master_j.voicememos.MainActivity.KEY_MEMOS_CREATED";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null)
			Toast.makeText(this, SqlStaff.SQL_CREATE_TABLE, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_add_memo)
			;
		return super.onOptionsItemSelected(item);
	}

	private static class TableConsts implements BaseColumns {
		static final String TABLE_NAME = "maintable";
		static final String COLUMN_NAME_TITLE = "title";
		static final String COLUMN_NAME_TIMEDATE = "timedate";
	}

	private static class SqlStaff {
		static final String TYPE_TEXT = " TEXT";
		static final String TYPE_INT = " INTEGER";
		static final String COMMA = ", ";
		static final String SQL_CREATE_TABLE = "CREATE TABLE " + TableConsts.TABLE_NAME + " (" + TableConsts._ID
				+ " INTEGER PRIMARY KEY" + COMMA + TableConsts.COLUMN_NAME_TITLE + TYPE_TEXT + COMMA
				+ TableConsts.COLUMN_NAME_TIMEDATE + TYPE_INT + ")";
	}

	private class DBHelper extends SQLiteOpenHelper {
		static final int DB_VERSION = 1;
		static final String DB_NAME = "Memos.db";

		DBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SqlStaff.SQL_CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			throw new UnsupportedOperationException("no onUpgrade, sir");
		}
	}
}

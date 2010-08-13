package com.redhorse.reddial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * DB format: _id, type, item, url
 */

/*
 * Database operator class, to create, open, use, and close DB. *
 */
public class dbDialConfigAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_PKGNAME = "pkgname";
	public static final String KEY_APPNAME = "appname";
	public static final String KEY_CONTENT = "content";
	private static final String TAG = "dial";
	private static final String DATABASE_NAME = "reddial";
	private static final String DATABASE_TABLE = "dial";
	private static final int DATABASE_VERSION = 1;

	/*
	 * create table SQL
	 */
	private static final String DATABASE_CREATE = "create table "+DATABASE_TABLE+" (_id integer primary key autoincrement, "
			+ "pkgname text not null, appname text not null, "
			+ "content text not null);";

	private final Context context;

	// DB assistant instance

	private DatabaseHelper DBHelper;

	// DB instance

	private SQLiteDatabase db;

	/*
	 * DBAdapter constructor
	 */
	public dbDialConfigAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	/*
	 * DB help class, it is a DB assistant class You will need to override
	 * onCreate() and onUpgrade() method.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS items");
			onCreate(db);
		}
	}// end of DatabaseHelper

	/*****************************************************
	 * Below are all DBAdaptor method: create, open...
	 ****************************************************/

	/*
	 * Open DB
	 */
	public dbDialConfigAdapter open() throws SQLException {
		// get a DB through DB assistant

		db = DBHelper.getWritableDatabase();
		return this;
	}

	/*
	 * close DB
	 */
	public void close() {
		// close DB through DB assistant

		DBHelper.close();
	}

	/*
	 * Insert one item
	 */
	public long insertItem(String pkgname, String appname, String content) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PKGNAME, pkgname);
		initialValues.put(KEY_APPNAME, appname);
		initialValues.put(KEY_CONTENT, content);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/*
	 * Delete one item
	 */
	public boolean deleteItem(String rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/*
	 * Delete all items
	 */
	public boolean deleteAllItems() {
		return db.delete(DATABASE_TABLE, "", null) > 0;
	}

	/*
	 * Query all items
	 */
	public Cursor getAllItems() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_PKGNAME,
				KEY_APPNAME, KEY_CONTENT }, null, null, null, null, null);
	}

	/*
	 * Query a specified item
	 */
	public Cursor getItem(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_PKGNAME, KEY_APPNAME, KEY_CONTENT }, KEY_ROWID + "="
				+ rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/*
	 * update a item
	 */
	public boolean updateItem(long rowId, String pkgname, String appname, String content) {
		ContentValues args = new ContentValues();
		args.put(KEY_PKGNAME, pkgname);
		args.put(KEY_APPNAME, appname);
		args.put(KEY_CONTENT, content);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
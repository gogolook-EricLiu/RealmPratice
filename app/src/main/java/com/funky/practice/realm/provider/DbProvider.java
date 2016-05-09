package com.funky.practice.realm.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class DbProvider extends ContentProvider {

	private static final String DATABASE_NAME = "db";
	private static final int DATABASE_VERSION = 1;

	private static final String TABLE_NAME = "contact";

	private SQLiteDatabase db;

	private static class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
					"_id INTEGER PRIMARY KEY," +
					"contact_id INTEGER," +
					"name TEXT," +
					"number TEXT," +
					"e164 TEXT)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onCreate(db);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = db.delete(TABLE_NAME, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/com.funky.realm.contact";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = db.insert(TABLE_NAME, "", values);
		final Uri resultUri = ContentUris.withAppendedId(uri, rowId);
		getContext().getContentResolver().notifyChange(resultUri, null);
		return resultUri;
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		OpenHelper dbHelper = new OpenHelper(context);

		/**
		 * Create a write able database which will trigger its
		 * creation if it doesn't already exist.
		 */
		db = dbHelper.getWritableDatabase();
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);

		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = db.update(TABLE_NAME, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}

package com.vapid_software.prodigy.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private final static int VERSION = 1;
    private final static String DB_NAME = "prodigy";
    public final static String USERS_TABLE = "users";
    public final static String TOKENS_TABLE = "tokens";

    private void createTokensTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TOKENS_TABLE + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "token TEXT," +
                "last_updated INTEGER" +
                ");");
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ USERS_TABLE +"(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "access_token TEXT," +
                "user_id TEXT" +
                ");");
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createTokensTable(db);
    }
	
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

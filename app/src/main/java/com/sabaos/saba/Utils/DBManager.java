package com.sabaos.saba.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBManager {

    static final String dbName = "SabaDB";
    static final String tableName = "NetworkDataUsage";
    static final String column_date = "date";
    static final String column_data = "data";
    static final int dbVersion = 1;
    private SQLiteDatabase sqLiteDatabase;


    public DBManager(Context context) {

        DBCreate dbCreate = new DBCreate(context, dbName, null, dbVersion);
        sqLiteDatabase = dbCreate.getWritableDatabase();
    }

    static class DBCreate extends SQLiteOpenHelper {

        String createTable = "create Table IF NOT EXISTS " + tableName + "(ID integer PRIMARY KEY AUTOINCREMENT," + column_date + " text"
                + " NOT NULL, " + column_data + " text);";

        public DBCreate(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("Drop table IF EXISTS " + tableName);
            onCreate(db);
        }
    }

    public long insert(ContentValues values) {

        long id = sqLiteDatabase.insert(tableName, "", values);
        return id;
    }
}

package com.example.zaverecka;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "skilltracker.db";
    private static final int DB_VERSION = 1;
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSkills = "CREATE TABLE skills (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "level INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createSkills);

        String createProgress = "CREATE TABLE progress (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "skill_id INTEGER, " +
                "date TEXT, " +
                "note TEXT" +
                ")";
        db.execSQL(createProgress);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS skills");
        db.execSQL("DROP TABLE IF EXISTS progress");

        onCreate(db);
    }
}

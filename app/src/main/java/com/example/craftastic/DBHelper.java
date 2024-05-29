package com.example.craftastic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "craftastic.db";
    private static final int VERSION_NUMBER = 1;
    private String CREATE_TABLE_CATEGORIES =
            "Create table categories(" +
                    "id integer primary key autoincrement,"+
                    "categoryname text not null);";

    private String CREATE_TABLE_CRAFTS =
            "CREATE TABLE crafts(" +
                    "id integer primary key autoincrement,"+
                    "craftname text not null," +
                    "craftprice real not null," +
                    "craftcondition text not null," +
                    "craftdescription text not null," +
                    "longitude real not null, " +
                    "latitude real not null, " +
                    "postdate date not null," +
                    "userId text not null);";

    private String CREATE_TABLE_PHOTOS =
            "CREATE TABLE photos (" +
                    "photo_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "craft_id INTEGER NOT NULL, " +
                    "image BLOB  NOT NULL, " +
                    "FOREIGN KEY (craft_id) REFERENCES crafts(id) " +
                    "ON DELETE CASCADE);";

    private String CREATE_TABLE_FAVORITES =
            "CREATE TABLE favorites (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "craft_id INTEGER NOT NULL, " +
                    "userId text NOT NULL, " +
                    "FOREIGN KEY (craft_id) REFERENCES crafts(id));";


    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CRAFTS);
        db.execSQL(CREATE_TABLE_PHOTOS);
        db.execSQL(CREATE_TABLE_FAVORITES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

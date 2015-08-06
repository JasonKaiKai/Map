package com.example.user.map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by user on 2015/7/12.
 */
public class MapDB extends SQLiteOpenHelper {
    private static String DATABASE_NAME = "MyMapDB";
    private static int DATABASE_VERSION = 1;
    private static String TABLE_NAME = "MapInfo";
    private static String column1 = "_id INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static String column2 = "_lat DECIMAL(10,7), ";
    private static String column3 = "_lng DECIMAL(10,7), ";
    private static String column4 = "_localName VARCHAR(10),";
    private static String column5 = "_intro TEXT";
    private final static String createTable = "CREATE TABLE "+TABLE_NAME+"("
            +column1+column2+column3+column4+column5+");";

    public MapDB(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
        Log.d("dbiscreate?","yes");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

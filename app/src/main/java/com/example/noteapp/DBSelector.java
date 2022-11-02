package com.example.noteapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

public class DBSelector extends SQLiteOpenHelper {

    private final static String CREATE_TABLE =
            String.format("CREATE TABLE %s (ID INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)",
                    DBDefinition.tbNAME, DBDefinition.EVENT, DBDefinition.TIME, DBDefinition.DATE, DBDefinition.MONTH,DBDefinition.YEAR);
    private final static String DROP_TABLE = String.format("DROP TABLE IF EXISTS %s",DBDefinition.tbNAME);
    public DBSelector(@Nullable Context context){
        super(context,DBDefinition.dbNAME,null,DBDefinition.dbVERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void SaveEvent(String event, String time, String date, String month, String year, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBDefinition.EVENT,event);
        contentValues.put(DBDefinition.TIME,time);
        contentValues.put(DBDefinition.DATE,date);
        contentValues.put(DBDefinition.MONTH,month);
        contentValues.put(DBDefinition.YEAR,year);

        sqLiteDatabase.insert(DBDefinition.tbNAME,null,contentValues);

    }

    public Cursor ReadEvent(String date, SQLiteDatabase sqLiteDatabase){
        String[] colTable = {DBDefinition.EVENT,DBDefinition.TIME,DBDefinition.DATE,DBDefinition.MONTH,DBDefinition.YEAR};
        return sqLiteDatabase.query(DBDefinition.tbNAME,colTable, DBDefinition.DATE + "=?", new String[]{date},null,null,null);
    }

    public Cursor ReadEventPerMonth(String month, String year, SQLiteDatabase sqLiteDatabase){
        String[] colTable = {DBDefinition.EVENT,DBDefinition.TIME,DBDefinition.DATE,DBDefinition.MONTH,DBDefinition.YEAR};
        return sqLiteDatabase.query(DBDefinition.tbNAME,colTable, DBDefinition.MONTH + "=? and " + DBDefinition.YEAR + "=?", new String[]{month,year},null,null,null);
    }

    public void DeleteEvent(String name, String date, String time, SQLiteDatabase sqLiteDatabase){
        String clause = String.format("%s = ? and %s = ? and %s = ?",DBDefinition.EVENT,DBDefinition.DATE,DBDefinition.TIME);
        sqLiteDatabase.delete(DBDefinition.tbNAME,clause,new String[]{name,date,time});
    }

}

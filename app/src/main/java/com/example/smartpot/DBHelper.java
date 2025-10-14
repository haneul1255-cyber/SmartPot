package com.example.smartpot;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context){
        super(context, "plantDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE plantTBL (" +
                "id integer primary key autoincrement," +
                "plantName char(20) not null, " +
                "plantType TEXT not null," +
                "bluetoothAddress text unique not null," +
                "bluteoothName text," +
                "registerDate integer," +
                "wateringInterval integer," +
                "isActive integer default 1)");

        db.execSQL("create table sensorData(" +
                "id integer primary key autoincrement," +
                "plantId integer not null," +
                "timestamp integer not null," +
                "temperature real," +
                "humidity real," +
                "soilMoisture real," +
                "lightLevel integer," +
                "foreign key(plantId) references plantTBL(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists sensorData");
        db.execSQL("drop table if exists plantTBL");
        onCreate(db);
    }
}

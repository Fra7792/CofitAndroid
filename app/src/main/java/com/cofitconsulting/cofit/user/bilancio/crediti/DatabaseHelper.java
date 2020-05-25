package com.cofitconsulting.cofit.user.bilancio.crediti;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cofitconsulting.cofit.utility.strutture.StrutturaConto;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "crediti_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TIPO_CREDITO = "tipo_credito";
    private static final String TABLE_DESCRIZIONE_CREDITO = "descrizione_crediti";
    private static final String TABLE_IMPORTO_CREDITO = "importo_crediti";
    private static final String TABLE_DATA_CREDITO = "data_crediti";
    private static final String KEY_ID = "id";
    private static final String KEY_TIPO = "tipo";
    private static final String KEY_DESCRIZIONE = "descrizione";
    private static final String KEY_IMPORTO = "importo";
    private static final String KEY_DATA = "data";

    private static final String CREATE_TABLE_TIPO_CREDITO = "CREATE TABLE "
            + TABLE_TIPO_CREDITO + "(" + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TIPO + " TEXT );";

    private static final String CREATE_TABLE_DESCRIZIONE_CREDITO = "CREATE TABLE "
            + TABLE_DESCRIZIONE_CREDITO + "(" + KEY_ID + " INTEGER,"+ KEY_DESCRIZIONE + " TEXT );";

    private static final String CREATE_TABLE_IMPORTO_CREDITO = "CREATE TABLE "
            + TABLE_IMPORTO_CREDITO + "(" + KEY_ID + " INTEGER,"+ KEY_IMPORTO + " TEXT );";

    private static final String CREATE_TABLE_DATA_CREDITO = "CREATE TABLE "
            + TABLE_DATA_CREDITO + "(" + KEY_ID + " INTEGER,"+ KEY_DATA + " TEXT );";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.d("table", CREATE_TABLE_TIPO_CREDITO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TIPO_CREDITO);
        db.execSQL(CREATE_TABLE_DESCRIZIONE_CREDITO);
        db.execSQL(CREATE_TABLE_IMPORTO_CREDITO);
        db.execSQL(CREATE_TABLE_DATA_CREDITO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_TIPO_CREDITO + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_DESCRIZIONE_CREDITO + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_IMPORTO_CREDITO + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_DATA_CREDITO + "'");
        onCreate(db);
    }

    public void addUser(String tipo, String descrizione, String importo, String data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valuesTipo = new ContentValues();
        valuesTipo.put(KEY_TIPO, tipo);

        long id = db.insertWithOnConflict(TABLE_TIPO_CREDITO, null, valuesTipo, SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues valuesDescrizione = new ContentValues();
        valuesDescrizione.put(KEY_ID, id);
        valuesDescrizione.put(KEY_DESCRIZIONE, descrizione);
        db.insert(TABLE_DESCRIZIONE_CREDITO, null, valuesDescrizione);


        ContentValues valuesImporto = new ContentValues();
        valuesImporto.put(KEY_ID, id);
        valuesImporto.put(KEY_IMPORTO, importo);
        db.insert(TABLE_IMPORTO_CREDITO, null, valuesImporto);

        ContentValues valuesData = new ContentValues();
        valuesData.put(KEY_ID, id);
        valuesData.put(KEY_DATA, data);
        db.insert(TABLE_DATA_CREDITO,null, valuesData);

    }

    public ArrayList<StrutturaConto> getAllUsers() {
        ArrayList<StrutturaConto> strutturaContoArrayList = new ArrayList<StrutturaConto>();

        String selectQuery = "SELECT  * FROM " + TABLE_TIPO_CREDITO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                StrutturaConto strutturaConto = new StrutturaConto();
                strutturaConto.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                strutturaConto.setTipo(c.getString(c.getColumnIndex(KEY_TIPO)));


                String selectDescrizioneQuery = "SELECT  * FROM " + TABLE_DESCRIZIONE_CREDITO +" WHERE "+KEY_ID+" = "+ strutturaConto.getId();
                Log.d("oppp",selectDescrizioneQuery);

                Cursor cDescrizione = db.rawQuery(selectDescrizioneQuery, null);

                if (cDescrizione.moveToFirst()) {
                    do {
                        strutturaConto.setDescrizione(cDescrizione.getString(cDescrizione.getColumnIndex(KEY_DESCRIZIONE)));
                    } while (cDescrizione.moveToNext());
                }


                String selectImportoQuery = "SELECT  * FROM " + TABLE_IMPORTO_CREDITO+" WHERE "+KEY_ID+" = "+ strutturaConto.getId();;

                Cursor cImporto = db.rawQuery(selectImportoQuery, null);

                if (cImporto.moveToFirst()) {
                    do {
                        strutturaConto.setImporto(cImporto.getString(cImporto.getColumnIndex(KEY_IMPORTO)));
                    } while (cImporto.moveToNext());
                }

                String selectDataQuery = "SELECT  * FROM " + TABLE_DATA_CREDITO+" WHERE "+KEY_ID+" = "+ strutturaConto.getId();;

                Cursor cData = db.rawQuery(selectDataQuery, null);

                if (cData.moveToFirst()) {
                    do {
                        strutturaConto.setData(cData.getString(cData.getColumnIndex(KEY_DATA)));
                    } while (cData.moveToNext());
                }


                strutturaContoArrayList.add(strutturaConto);
            } while (c.moveToNext());
        }
        return strutturaContoArrayList;
    }

    public void updateUser(int id, String tipo, String descrizione, String importo, String data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valuesTipo = new ContentValues();
        valuesTipo.put(KEY_TIPO, tipo);
        db.update(TABLE_TIPO_CREDITO, valuesTipo, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues valuesDescrizione = new ContentValues();
        valuesDescrizione.put(KEY_DESCRIZIONE, descrizione);
        db.update(TABLE_DESCRIZIONE_CREDITO, valuesDescrizione, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues valuesImporto = new ContentValues();
        valuesImporto.put(KEY_IMPORTO, importo);
        db.update(TABLE_IMPORTO_CREDITO, valuesImporto, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues valuesData = new ContentValues();
        valuesData.put(KEY_DATA, data);
        db.update(TABLE_DATA_CREDITO, valuesData, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteUSer(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_TIPO_CREDITO, KEY_ID + " = ?",new String[]{String.valueOf(id)});

        db.delete(TABLE_DESCRIZIONE_CREDITO, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        db.delete(TABLE_IMPORTO_CREDITO, KEY_ID + " = ?",new String[]{String.valueOf(id)});

        db.delete(TABLE_DATA_CREDITO, KEY_ID + " = ?",new String[]{String.valueOf(id)});
    }

}
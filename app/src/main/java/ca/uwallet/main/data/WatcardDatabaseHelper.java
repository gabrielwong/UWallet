package ca.uwallet.main.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
* Created by gabriel on 5/23/14.
*/
public class WatcardDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "watcard.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TRANSACTION =
            "CREATE TABLE " + WatcardContract.Transaction.TABLE_NAME + "(" +
            WatcardContract.Transaction._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            WatcardContract.Transaction.COLUMN_NAME_AMOUNT + TYPE_INTEGER + COMMA_SEP +
            WatcardContract.Transaction.COLUMN_NAME_DATE + TYPE_INTEGER + COMMA_SEP +
            WatcardContract.Transaction.COLUMN_NAME_MONEY_TYPE + TYPE_INTEGER + COMMA_SEP +
            WatcardContract.Transaction.COLUMN_NAME_TERMINAL + TYPE_INTEGER + ")";

    private static final String SQL_CREATE_BALANCE =
            "CREATE TABLE " + WatcardContract.Balance.TABLE_NAME + "(" +
            WatcardContract.Balance._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            WatcardContract.Balance.COLUMN_NAME_AMOUNT + TYPE_INTEGER + ")";

    private static final String SQL_CREATE_TERMINAL =
            "CREATE TABLE " + WatcardContract.Terminal.TABLE_NAME + "(" +
            WatcardContract.Terminal._ID + " INTEGER PRIMARY KEY," +
            WatcardContract.Terminal.COLUMN_NAME_TEXT + TYPE_TEXT + COMMA_SEP +
            WatcardContract.Terminal.COLUMN_NAME_CATEGORY + TYPE_INTEGER + COMMA_SEP +
            WatcardContract.Terminal.COLUMN_NAME_TEXT_PRIORITY + TYPE_INTEGER +  COMMA_SEP +
            WatcardContract.Terminal.COLUMN_NAME_CATEGORY_PRIORITY + TYPE_INTEGER +")";

    private static final String SQL_CREATE_CATEGORY =
            "CREATE TABLE " + WatcardContract.Category.TABLE_NAME + "(" +
            WatcardContract.Category._ID + " INTEGER PRIMARY KEY," +
            WatcardContract.Category.COLUMN_NAME_CATEGORY_TEXT + TYPE_TEXT + ")";

    private static final String SQL_DELETE_TRANSACTION =
            "DROP TABLE IF EXISTS " + WatcardContract.Transaction.TABLE_NAME;

    private static final String SQL_DELETE_BALANCE =
            "DROP TABLE IF EXISTS " + WatcardContract.Balance.TABLE_NAME;

    private static final String SQL_DELETE_TERMINAL =
            "DROP TABLE IF EXISTS " + WatcardContract.Terminal.TABLE_NAME;

    private static final String SQL_DELETE_CATEGORY =
            "DROP TABLE IF EXISTS " + WatcardContract.Category.TABLE_NAME;

    public WatcardDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    /**
     * Build the tables
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRANSACTION);
        db.execSQL(SQL_CREATE_BALANCE);
        db.execSQL(SQL_CREATE_TERMINAL);
        db.execSQL(SQL_CREATE_CATEGORY);
    }

    @Override
    /**
     * Called with new database version. Drop the entire table and rebuild.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TRANSACTION);
        db.execSQL(SQL_DELETE_BALANCE);
        db.execSQL(SQL_DELETE_TERMINAL);
        db.execSQL(SQL_DELETE_CATEGORY);
        onCreate(db);
    }
}

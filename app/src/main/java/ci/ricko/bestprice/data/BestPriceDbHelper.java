package ci.ricko.bestprice.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ci.ricko.bestprice.data.BestPriceContract.*;


/**
 * Created by Eric Zile on 20/03/15.
 */
public class BestPriceDbHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "bestprice.db";

    public BestPriceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // produit table
        final String SQL_CREATE_PRODUIT_TABLE = "CREATE TABLE " + ProduitEntry.TABLE_NAME + " (" +
                ProduitEntry._ID + " INTEGER PRIMARY KEY," +
                ProduitEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ProduitEntry.COLUMN_IMG + " TEXT NOT NULL, " +
                "UNIQUE (" + ProduitEntry._ID  +") ON CONFLICT IGNORE);";

        // variation table
        final String SQL_CREATE_VARIATION_TABLE = "CREATE TABLE " + VariationEntry.TABLE_NAME + " (" +

                VariationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                VariationEntry.COLUMN_PRODUIT_KEY + " INTEGER NOT NULL, " +
                VariationEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                VariationEntry.COLUMN_QUANTITE + " INTEGER NOT NULL, " +
                VariationEntry.COLUMN_MESURE + " TEXT NOT NULL, " +
                VariationEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                "UNIQUE (" + VariationEntry.COLUMN_PRODUIT_KEY+","+VariationEntry.COLUMN_DATETEXT +") ON CONFLICT IGNORE"+
                // Set up the produit.id column as a foreign key to variation table.
                " FOREIGN KEY (" + VariationEntry.COLUMN_PRODUIT_KEY + ") REFERENCES " +
                ProduitEntry.TABLE_NAME + " (" + ProduitEntry._ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_PRODUIT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VARIATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProduitEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VariationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

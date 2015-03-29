package ci.ricko.bestprice.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


/**
 * Created by Eric Zile on 20/03/15.
 */
public class BestPriceProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BestPriceDbHelper mOpenHelper;

    private static final int PRODUIT = 100;
    private static final int PRODUIT_ID = 101;
    private static final int VARIATION = 105;
    private static final int VARIATION_ID = 106;
    private static final int VARIATION_PRODUIT_DATE = 107;
    private static final int VARIATION_DATE = 108;

    private static final SQLiteQueryBuilder sVariationQueryBuilder;

    static{
        sVariationQueryBuilder = new SQLiteQueryBuilder();
        sVariationQueryBuilder.setTables(BestPriceContract.ProduitEntry.TABLE_NAME + " INNER JOIN " +
                BestPriceContract.VariationEntry.TABLE_NAME +
                " ON " + BestPriceContract.VariationEntry.TABLE_NAME +
                "." + BestPriceContract.VariationEntry.COLUMN_PRODUIT_KEY +
                " = " + BestPriceContract.ProduitEntry.TABLE_NAME +
                "." + BestPriceContract.ProduitEntry._ID);
    }



    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BestPriceContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, BestPriceContract.PATH_PRODUIT, PRODUIT);
        matcher.addURI(authority, BestPriceContract.PATH_PRODUIT+"/#", PRODUIT_ID);
        matcher.addURI(authority, BestPriceContract.PATH_VARIATION, VARIATION);
        matcher.addURI(authority, BestPriceContract.PATH_VARIATION+"/#", VARIATION_ID);
        matcher.addURI(authority, BestPriceContract.PATH_VARIATION+"/#/*", VARIATION_PRODUIT_DATE);
        matcher.addURI(authority, BestPriceContract.PATH_VARIATION+"/*", VARIATION_DATE);
        return matcher;

    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BestPriceDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "produit"
            case PRODUIT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BestPriceContract.ProduitEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "produit/#"
            case PRODUIT_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BestPriceContract.ProduitEntry.TABLE_NAME,
                        projection,
                        BestPriceContract.ProduitEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "variation"
            case VARIATION: {
                retCursor = sVariationQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "variation/#"
            case VARIATION_ID: {
                retCursor = sVariationQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        BestPriceContract.VariationEntry.TABLE_NAME+"."+ BestPriceContract.VariationEntry._ID+" = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );

                break;
            }

            // "variation/#/*"
            case VARIATION_PRODUIT_DATE: {
                retCursor = sVariationQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        BestPriceContract.VariationEntry.TABLE_NAME+"."+ BestPriceContract.VariationEntry.COLUMN_PRODUIT_KEY+" = ? AND "+
                        BestPriceContract.VariationEntry.TABLE_NAME+"."+ BestPriceContract.VariationEntry.COLUMN_DATETEXT+" = ?",
                        new String[]{BestPriceContract.VariationEntry.getProduitFromUri(uri),
                                BestPriceContract.VariationEntry.getDateFromUriWithProduit(uri)},
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            // "variation/*"
            case VARIATION_DATE: {
                retCursor = sVariationQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        BestPriceContract.VariationEntry.TABLE_NAME+"."+ BestPriceContract.VariationEntry.COLUMN_DATETEXT+" = ?",
                        new String[]{BestPriceContract.VariationEntry.getDateFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUIT:
                return BestPriceContract.ProduitEntry.CONTENT_TYPE;
            case PRODUIT_ID:
                return BestPriceContract.ProduitEntry.CONTENT_ITEM_TYPE;
            case VARIATION:
                return BestPriceContract.VariationEntry.CONTENT_TYPE;
            case VARIATION_ID:
                return BestPriceContract.VariationEntry.CONTENT_ITEM_TYPE;
            case VARIATION_PRODUIT_DATE:
                return BestPriceContract.VariationEntry.CONTENT_ITEM_TYPE;
            case VARIATION_DATE:
                return BestPriceContract.VariationEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PRODUIT: {
                long _id = db.insert(BestPriceContract.ProduitEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BestPriceContract.ProduitEntry.buildProduitUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VARIATION: {
                long _id = db.insert(BestPriceContract.VariationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = BestPriceContract.VariationEntry.buildVariationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PRODUIT:
                rowsDeleted = db.delete(
                        BestPriceContract.ProduitEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VARIATION:
                rowsDeleted = db.delete(
                        BestPriceContract.VariationEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PRODUIT:
                rowsUpdated = db.update(BestPriceContract.ProduitEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case VARIATION:
                rowsUpdated = db.update(BestPriceContract.VariationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        System.out.println("*************taille **** "+values.length);
        switch (match) {
            case PRODUIT:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BestPriceContract.ProduitEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case VARIATION:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BestPriceContract.VariationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

}

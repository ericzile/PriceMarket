package ci.ricko.bestprice.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Eric Zile on 20/03/15.
        */
public class BestPriceContract {


    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "ci.ricko.bestprice";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUIT = "produit";
    public static final String PATH_VARIATION = "variation";



    /* Inner class that defines the table contents of the produit table */
    public static final class ProduitEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUIT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PRODUIT;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PRODUIT;

        public static final String TABLE_NAME = "produit";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMG = "img";


        public static Uri buildProduitUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }


    /* Inner class that defines the table contents of the variation table */
    public static final class VariationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VARIATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_VARIATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_VARIATION;

        public static final String TABLE_NAME = "variation";

        public static final String COLUMN_PRODUIT_KEY = "produit_id";
        // Date, stored as Text with format dd-MM-yyyy
        public static final String COLUMN_DATETEXT = "date";
        public static final String COLUMN_QUANTITE = "quantite";
        public static final String COLUMN_MESURE = "mesure";
        public static final String COLUMN_PRICE = "price";

        public static Uri buildVariationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildVariationUriWithProduitAndDate(long produit_id, String date) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(produit_id)).appendPath(date).build();
        }

        public static Uri buildVariationUriWithDate(String date) {
            return CONTENT_URI.buildUpon().appendPath(date).build();
        }

        public static String getProduitFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getDateFromUriWithProduit(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }


    }

}

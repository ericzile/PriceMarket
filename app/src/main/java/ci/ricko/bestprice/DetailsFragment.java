package ci.ricko.bestprice;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import ci.ricko.bestprice.data.BestPriceContract;


/**
 * Created by Eric Zile on 20/03/15.
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private Bundle args;
    private long id;
    private String IDKEY="id";
    private static final int DETAIL_LOADER = 0;
    private static final String PHARMACY_SHARE_HASHTAG = " #BestPriceApp";
    private ShareActionProvider mShareActionProvider;
    private String shareString;
    private DisplayImageOptions options;



    private ImageView produit_icon;
    private TextView produit_name;
    private TextView produit_quantite;
    private TextView produit_mesure;
    private TextView variation_date;
    private TextView produit_price;
    //labels
    private TextView label_quantite;
    private TextView label_price;
    private TextView label_date;

    //layout


    private static final String[] PRODUIT_COLUMNS = {

            BestPriceContract.ProduitEntry.TABLE_NAME + "." + BestPriceContract.ProduitEntry._ID,
            BestPriceContract.VariationEntry.TABLE_NAME + "." + BestPriceContract.VariationEntry._ID,
            BestPriceContract.ProduitEntry.COLUMN_NAME,
            BestPriceContract.ProduitEntry.COLUMN_IMG,
            BestPriceContract.VariationEntry.COLUMN_DATETEXT,
            BestPriceContract.VariationEntry.COLUMN_QUANTITE,
            BestPriceContract.VariationEntry.COLUMN_MESURE,
            BestPriceContract.VariationEntry.COLUMN_PRICE
    };

    public static final int COL_PRODUIT_ID = 0;
    public static final int COL_VARIATION_ID = 1;
    public static final int COL_PRODUIT_NAME = 2;
    public static final int COL_PRODUIT_IMG = 3;
    public static final int COL_VARIATION_DATE = 4;
    public static final int COL_VARIATION_QUANTITE = 5;
    public static final int COL_VARIATION_MESURE = 6;
    public static final int COL_VARIATION_PRICE = 7;


    public  DetailsFragment(){

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (shareString != null) {
            mShareActionProvider.setShareIntent(createSharePharmacyIntent());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        args = getArguments();
        if (args != null) {
            id = args.getLong(IDKEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        produit_icon= (ImageView) rootView.findViewById(R.id.produit_icon);
        produit_name = (TextView) rootView.findViewById(R.id.produit_name);
        produit_quantite = (TextView) rootView.findViewById(R.id.produit_quantite);
        produit_mesure = (TextView) rootView.findViewById(R.id.produit_mesure);
        variation_date = (TextView) rootView.findViewById(R.id.variation_date);
        produit_price = (TextView) rootView.findViewById(R.id.produit_price);
        label_quantite = (TextView) rootView.findViewById(R.id.label_quantite);
        label_price = (TextView) rootView.findViewById(R.id.label_price);
        label_date = (TextView) rootView.findViewById(R.id.label_date);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_empty)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();


        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        args = getArguments();

        if (args != null && args.containsKey(IDKEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (args != null && args.containsKey(IDKEY)) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri produitUri = BestPriceContract.VariationEntry.buildVariationUri(id);
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                produitUri,
                PRODUIT_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {

            ImageLoader.getInstance().displayImage(cursor.getString(ProduitFragment.COL_PRODUIT_IMG), produit_icon, options);
            produit_name.setText(cursor.getString(COL_PRODUIT_NAME));
            produit_quantite.setText(cursor.getString(COL_VARIATION_QUANTITE));
            variation_date.setText(cursor.getString(COL_VARIATION_DATE));
            produit_mesure.setText(cursor.getString(COL_VARIATION_MESURE));
            produit_price.setText(cursor.getString(COL_VARIATION_PRICE)+" Fcfa");

            label_quantite.setText(getString(R.string.quantite_label));
            label_price.setText(getString(R.string.price_label));
            label_date.setText(getString(R.string.date_label));


            shareString = cursor.getString(COL_PRODUIT_NAME)+", "+
                    cursor.getString(COL_VARIATION_QUANTITE)+" "+
            cursor.getString(COL_VARIATION_MESURE)+", prix: "+
                    cursor.getString(COL_VARIATION_PRICE)+" Fcfa";

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createSharePharmacyIntent());
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private Intent createSharePharmacyIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareString + PHARMACY_SHARE_HASHTAG);
        return shareIntent;
    }
}
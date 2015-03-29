package ci.ricko.bestprice;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ci.ricko.bestprice.data.BestPriceContract;


public class ProduitFragment extends Fragment implements LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ProduitFragment.class.getSimpleName();
    private ProduitAdapter produitAdapter;
    private ListView listView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int PRODUIT_LOADER = 0;

    // Specify the columns we need.
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



    public ProduitFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_produit, container, false);

       produitAdapter = new ProduitAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        listView = (ListView) rootView.findViewById(R.id.list);
        listView.setAdapter(produitAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = produitAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback)getActivity())
                            .onItemSelected(cursor.getLong(COL_VARIATION_ID));
                }
                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRODUIT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
            getLoaderManager().restartLoader(PRODUIT_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Sort order:  Ascending, by variation.
        String sortOrder = BestPriceContract.VariationEntry.TABLE_NAME+"."+BestPriceContract.VariationEntry._ID + " ASC";
        Uri variationUri = BestPriceContract.VariationEntry.buildVariationUriWithDate(Utils.getTodayDate());

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                variationUri,
                PRODUIT_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        produitAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            listView.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Long id);
    }

}
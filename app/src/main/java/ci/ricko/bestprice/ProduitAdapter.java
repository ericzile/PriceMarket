package ci.ricko.bestprice;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import ci.ricko.bestprice.data.BestPriceContract;

/**
 * Created by Eric Zile on 20/03/15.
 */
public class ProduitAdapter extends CursorAdapter {

    DisplayImageOptions options;

    /**
     * Cache of the children views for a pharmacy list item.
     */
    public static class ViewHolder {
        public final ImageView produit_icon;
        public final TextView produit_name;
        public final TextView produit_quantite;
        public final TextView produit_mesure;
        public final TextView produit_price;
        public final ImageView variation_icon;

        public ViewHolder(View view) {
            produit_icon = (ImageView) view.findViewById(R.id.produit_icon);
            produit_name = (TextView) view.findViewById(R.id.produit_name);
            produit_quantite = (TextView) view.findViewById(R.id.produit_quantite);
            produit_mesure = (TextView) view.findViewById(R.id.produit_mesure);
            produit_price = (TextView) view.findViewById(R.id.produit_price);
            variation_icon = (ImageView) view.findViewById(R.id.variation_icon);

        }
    }

    public ProduitAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_empty)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ImageLoader.getInstance().displayImage(cursor.getString(ProduitFragment.COL_PRODUIT_IMG), viewHolder.produit_icon, options);
        viewHolder.produit_name.setText(cursor.getString(ProduitFragment.COL_PRODUIT_NAME));
        viewHolder.produit_quantite.setText(cursor.getString(ProduitFragment.COL_VARIATION_QUANTITE));
        viewHolder.produit_mesure.setText(cursor.getString(ProduitFragment.COL_VARIATION_MESURE));
        viewHolder.produit_price.setText(cursor.getString(ProduitFragment.COL_VARIATION_PRICE)+" Fcfa");

        //requesting yesterday data
        Uri variationUri = BestPriceContract.VariationEntry.buildVariationUriWithProduitAndDate(cursor.getLong(ProduitFragment.COL_PRODUIT_ID),Utils.getYesterdayDate());

        Cursor variationCursor = context.getContentResolver().query(
                variationUri,
                new String[]{BestPriceContract.VariationEntry.COLUMN_PRICE},
                null,
                null,
                null);

        if (variationCursor.moveToFirst()){
            int yesterdayPrice = variationCursor.getInt(0);
            int todayPrice = cursor.getInt(ProduitFragment.COL_VARIATION_PRICE);
            if(todayPrice>yesterdayPrice)
            viewHolder.variation_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.b_up));
            else
            viewHolder.variation_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.b_down));
        }else
            viewHolder.variation_icon.setImageDrawable(context.getResources().getDrawable(R.drawable.b_up));



    }


}
package ci.ricko.bestprice;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import ci.ricko.bestprice.data.BestPriceContract;

public class Utils {
    public static String LOG_TAG = Utils.class.getSimpleName();
    static Date date = new Date();
	static Calendar c = Calendar.getInstance(Locale.FRANCE);


	
	public static String getYesterdayDate(){
		c.setTime(date);
        c.add(Calendar.DATE,-1);
		Date dateStart = c.getTime();
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		
       return df.format(dateStart.getTime());
		
	}

    public static String getTodayDate(){
        c.setTime(date);
        Date dateStart = c.getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

        return df.format(dateStart.getTime());

    }

    public static String getPerdiod(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_period_key),
                context.getString(R.string.pref_period_default));
    }

    public static void loadData(final Context context) {

        if(checkNetworkState(context))
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {


                Log.d(LOG_TAG, "Starting sync");
                String periodQuery = Utils.getPerdiod(context);

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String bestPriceJsonStr = null;


                try {
                    // Construct the URL for the bestprice query
                    final String BACKEND_URL ="https://pelagic-voice-87423.appspot.com/_ah/api/rest/v1/bestprice";
                    final String BESTPRICE_URL = BACKEND_URL+"/"+periodQuery;

                    Uri builtUri = Uri.parse(BESTPRICE_URL).buildUpon().build();

                    URL url = new URL(builtUri.toString());

                    // Create the request to the backend, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return false;
                    }

                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream has empty.  No point in parsing.
                        return false;
                    }
                    bestPriceJsonStr = buffer.toString();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    return false;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

                // Now we have a String representing the complete forecast in JSON Format.
                // Fortunately parsing is easy:  constructor takes the JSON string and converts it
                // into an Object hierarchy for us.

                // These are the names of the JSON objects that need to be extracted.

                // Produit information
                final String BACKEND_PRODUIT = "items";
                final String BACKEND_PRODUIT_ID = "id";
                final String BACKEND_PRODUIT_NAME = "name";
                final String BACKEND_PRODUIT_IMG = "img";

                // Variations informations
                final String BACKEND_VARIATION = "variations";
                final String BACKEND_VARIATION_DATE = "date";
                final String BACKEND_VARIATION_QUANTITE = "quantite";
                final String BACKEND_VARIATION_MESURE = "mesure";
                final String BACKEND_VARIATION_PRICE = "price";



                try {
                    JSONObject bestPriceJson = new JSONObject(bestPriceJsonStr);

                    JSONArray produitArray = new JSONArray();
                    if(bestPriceJson.has(BACKEND_PRODUIT))
                    produitArray = bestPriceJson.getJSONArray(BACKEND_PRODUIT);

                    Vector<ContentValues> produitVector = new Vector<ContentValues>(produitArray.length());

                    for(int i = 0; i < produitArray.length(); i++) {
                        // These are the values that will be collected.

                        long id;
                        String name;
                        String img;

                        // Get the JSON object representing the produit
                        JSONObject produit = produitArray.getJSONObject(i);

                        id = produit.getLong(BACKEND_PRODUIT_ID);
                        name = produit.getString(BACKEND_PRODUIT_NAME);
                        img = produit.getString(BACKEND_PRODUIT_IMG);


                                ContentValues produitValues = new ContentValues();
                                produitValues.put(BestPriceContract.ProduitEntry._ID, id);
                                produitValues.put(BestPriceContract.ProduitEntry.COLUMN_NAME, name);
                                produitValues.put(BestPriceContract.ProduitEntry.COLUMN_IMG, img);
                                produitVector.add(produitValues);

                        JSONArray variationArray = new JSONArray();
                        if(produit.has(BACKEND_VARIATION))
                            variationArray = produit.getJSONArray(BACKEND_VARIATION);

                        Vector<ContentValues> variationVector = new Vector<ContentValues>(variationArray.length());


                        for(int j = 0; j < variationArray.length(); j++) {
                            JSONObject variation = variationArray.getJSONObject(j);
                            long produit_id;
                            String date;
                            int quantite;
                            String mesure;
                            int price;

                            produit_id = id;
                            date = variation.getString(BACKEND_VARIATION_DATE);
                            quantite = variation.getInt(BACKEND_VARIATION_QUANTITE);
                            mesure = variation.getString(BACKEND_VARIATION_MESURE);
                            price = variation.getInt(BACKEND_VARIATION_PRICE);

                            ContentValues variationValues = new ContentValues();
                            variationValues.put(BestPriceContract.VariationEntry.COLUMN_PRODUIT_KEY, produit_id);
                            variationValues.put(BestPriceContract.VariationEntry.COLUMN_DATETEXT, date);
                            variationValues.put(BestPriceContract.VariationEntry.COLUMN_QUANTITE, quantite);
                            variationValues.put(BestPriceContract.VariationEntry.COLUMN_MESURE, mesure);
                            variationValues.put(BestPriceContract.VariationEntry.COLUMN_PRICE, price);
                            variationVector.add(variationValues);

                        }


                        if ( variationVector.size() > 0 ) {
                            ContentValues[] cvArray = new ContentValues[variationVector.size()];
                            variationVector.toArray(cvArray);
                            context.getContentResolver().bulkInsert(BestPriceContract.VariationEntry.CONTENT_URI, cvArray);

                        }



                    }
                    if ( produitVector.size() > 0 ) {
                        ContentValues[] cvArray = new ContentValues[produitVector.size()];
                        produitVector.toArray(cvArray);
                        context.getContentResolver().bulkInsert(BestPriceContract.ProduitEntry.CONTENT_URI, cvArray);

                    }


                    Log.d(LOG_TAG, "FetchBestPriceTask Complete. " + produitVector.size() + " Inserted");


                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }

                return true;
            }



        }.execute();

    }


    private static boolean checkNetworkState(Context context) {
        boolean isOnWifi=false;
        boolean is3G=false;
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo m3G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifi!=null)
            isOnWifi = mWifi.isConnected();
        if (m3G!=null)
            is3G = m3G.isConnected();

        if(isOnWifi == true || is3G == true) {
            return true;
        }

        return false;
    }



}

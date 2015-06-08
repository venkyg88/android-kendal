package app.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import app.staples.mobile.cfa.MainActivity;

import java.util.HashSet;

public class PersonalFeedSingleton {
    private static final String TAG = PersonalFeedSingleton.class.getSimpleName();

    public static final int SEEN_PRODUCTS_AMOUNT = 3;

    private static PersonalFeedSingleton personalFeedSingleton = null;
    private PersistentSizedArrayList<String> savedSeenProducts = null;
    private HashSet<String> savedSkus = null;

    private PersonalFeedSingleton(Activity activity){
        savedSeenProducts = getSavedSeenProducts(activity);
        savedSkus = getSavedSkus(activity);
    }

    public static PersonalFeedSingleton getInstance(Activity activity) {
        if(personalFeedSingleton == null){
            personalFeedSingleton = new PersonalFeedSingleton(activity);
        }
        return personalFeedSingleton;
    }

    public PersistentSizedArrayList<String> getSavedSeenProducts(){
        return savedSeenProducts;
    }

    // get seen products from the phone
    public PersistentSizedArrayList<String> getSavedSeenProducts(Activity activity) {
        SharedPreferences sp =
                activity.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);

        String savedProductsString = sp.getString(PersonalFeedFragment.SEEN_PRODUCT_LIST, "");

        PersistentSizedArrayList<String> savedProductsList
                = new PersistentSizedArrayList<String>(SEEN_PRODUCTS_AMOUNT);;

        if (!savedProductsString.equals("")) {
            String[] savedProductsArray = savedProductsString.split(PersistentSizedArrayList.FIELD_SEPARATOR);

            for (int i = 0; i < savedProductsArray.length; i++) {
                String sku = savedProductsArray[i];
                savedProductsList.add(sku);

                //Log.d(TAG, i + 1 + "th Saved Seen Product -> " + sku);
            }
        }
        else{
            Log.d(TAG, "No Saved Seen Products Found!");
        }

       return savedProductsList;
    }

    public HashSet<String> getSavedSkus(){
        return savedSkus;
    }

    // get seen products' sku from the phone
    public HashSet<String> getSavedSkus(Activity activity) {
        SharedPreferences sp =
                activity.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);

        String savedSkusString = sp.getString(PersonalFeedFragment.SEEN_PRODUCT_SKU_LIST, "");

        HashSet<String> savedProductSkuSet = new HashSet<String>();

        if (!savedSkusString.equals("")) {
            String[] savedSkusArray = savedSkusString.split(PersistentSizedArrayList.FIELD_SEPARATOR);

            // safe check for empty fields
            if(savedSkusArray.length > 0) {
                for (int i = 0; i < savedSkusArray.length; i++) {
                    savedProductSkuSet.add(savedSkusArray[i]);
                    // Log.d(TAG, i + 1 + "th Saved Sku -> " + savedSkusArray[i]);
                }
            }
            else{
                Log.d(TAG, "savedSkusArray[] has nothing. No Saved Sku Yet!");
            }
        }
        else{
            Log.d(TAG, "savedSkusString is empty. No Saved Sku Yet!");
        }

        return savedProductSkuSet;
    }

    public void setSavedSeenProducts(PersistentSizedArrayList<String> updatedSeenProducts){
        savedSeenProducts = updatedSeenProducts;
    }

    public void setSavedSkus(HashSet<String> updatedSkuSet){
        savedSkus = updatedSkuSet;
    }

}

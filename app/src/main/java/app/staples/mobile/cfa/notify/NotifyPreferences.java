package app.staples.mobile.cfa.notify;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.channel.api.ChannelApi;
import com.staples.mobile.common.access.channel.model.notify.Preferences;
import com.staples.mobile.common.access.channel.model.notify.Tag;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.config.model.Configurator;
import com.staples.mobile.common.access.config.model.Descriptor;
import com.staples.mobile.common.access.config.model.Holding;

import java.util.ArrayList;
import java.util.HashMap;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.profile.ProfileDetails;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotifyPreferences implements Callback<Preferences> {
    private static final String TAG = NotifyPreferences.class.getSimpleName();

    private static final boolean DEFAULTENABLE = true;

    private static NotifyPreferences instance;

    private ArrayList<Item> array;

    public static class Item {
        public String attribute;
        public String key;
        public String title;
        public boolean enable;

        private Item(String attribute, String title, boolean enable) {
            this.attribute = attribute;
            key = "notify"+Character.toUpperCase(attribute.charAt(0))+attribute.substring(1);
            this.title = title;
            this.enable = enable;
        }
    }

    public static NotifyPreferences getInstance() {
        if (instance==null) {
            synchronized(NotifyPreferences.class) {
                if (instance==null) {
                    instance = new NotifyPreferences();
                }
            }
        }
        return(instance);
    }

    // Basic array stuff

    private NotifyPreferences() {
        array = new ArrayList<Item>();

        AppConfigurator appConfigurator = AppConfigurator.getInstance();
        Configurator configurator = appConfigurator.getConfigurator();
        if(configurator == null) return;
        if(configurator.getAppContext().getHoldings() == null) return;
        for(Holding holding : configurator.getAppContext().getHoldings()) {
            if(holding.getName().equals("notifications")) {
                for(Descriptor descriptor : holding.getDescriptors()) {
                    if(descriptor.getValue().equals("in")){
                        addItem(descriptor.getKey(), descriptor.getParticulars().get(0).getValue());
                    }
                }
            }
        }
    }

    public Item addItem(String attribute, String title) {
        Item item = new Item(attribute, title, DEFAULTENABLE);
        array.add(item);
        return(item);
    }

    public ArrayList<Item> getArray() {
        return(array);
    }

    // Shared preferences

    public void loadPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        for(Item item : array) {
            item.enable = prefs.getBoolean(item.key, DEFAULTENABLE);
        }
    }

    public void savePreferences(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        for(Item item : array) {
            editor.putBoolean(item.key, item.enable);
        }
        editor.apply();
    }

    // Leanplum

    public HashMap<String, Object> getUserAttributes() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for(Item item : array) {
            map.put(item.attribute, item.enable);
        }
        return(map);
    }

    // Channel API

    public void uploadPreferences(Context context) {
        // Collect tags
        ArrayList<Tag> tags = new ArrayList<Tag>();
        for(Item item : array) {
            Tag tag = new Tag();
            tag.setTag(item.attribute);
            tag.setEnable(item.enable);
            tags.add(tag);
        }

        if(Access.getInstance().isLoggedIn()) {
            if(ProfileDetails.getMember() != null) {
                // Make body
                Preferences prefs = new Preferences();
                prefs.setApplication("staples");
                prefs.setNotificationType("android");
                prefs.setEmail(ProfileDetails.getMember().getEmailAddress());
                String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                prefs.setUaChannelId(id);
                prefs.setTags(tags);

                // Upload to preference center
                ChannelApi api = Access.getInstance().getChannelApi(false);
                api.setNotificationPreferences(prefs, this);
            }
            else {
                // Leave breadcrumb here once we define the platform
                // Logged in user should have details
            }
        }
    }

    @Override
    public void success(Preferences prefs, Response response) {
        Log.d(TAG, "Retrofit success");
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.d(TAG, "Retrofit failure");
    }
}

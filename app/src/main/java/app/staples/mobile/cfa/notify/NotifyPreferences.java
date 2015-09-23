package app.staples.mobile.cfa.notify;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.crittercism.app.Crittercism;
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
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.profile.ProfileDetails;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotifyPreferences implements Callback<Preferences> {
    private static final String TAG = NotifyPreferences.class.getSimpleName();

    private static final boolean DEFAULTENABLE = true;

    private static final String APPLICATION = "staples";
    private static final String NOTIFICATION_TYPE = "android";

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

    // Shared preferences methods - commented out for now - remove after Nov 2015

//    public void loadPreferences(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
//        for(Item item : array) {
//            item.enable = prefs.getBoolean(item.key, DEFAULTENABLE);
//        }
//    }

//    public void savePreferences(Context context) {
//        SharedPreferences sharedPrefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPrefs.edit();
//        for(Item item : array) {
//            editor.putBoolean(item.key, item.enable);
//        }
//        editor.apply();
//    }

    // Preference Center

    public void loadPreferences(final Context context) {
        ChannelApi channelApi = Access.getInstance().getChannelApi(false);
        if (ProfileDetails.getMember() != null) {
            String userEmail = ProfileDetails.getMember().getEmailAddress();

            channelApi.getNotificationPreferences(userEmail, APPLICATION, NOTIFICATION_TYPE, new Callback<List<Preferences>>() {
                @Override
                public void success(List<Preferences> preferencesList, Response response) {
                    if(preferencesList.size() == 0) {
                        uploadPreferences(context);                       // new user - preference center doesn't have a record
                    } else {
                        int updatesReceived = 0;
                        Preferences preferences = preferencesList.get(0); // call returns only one notificationType (android)
                        if(preferences != null) {
                            List<Tag> tags = preferences.getTags();       // get tags from pref center
                            for(Tag tag : tags) {
                                if(tag != null) {
                                    for(Item item : array) {              // loop through switches provisioned on MCS
                                        if(item.attribute.equals(tag.getTag())) {
                                            item.enable = tag.isEnable();
                                            updatesReceived++;
                                        }
                                    }
                                }

                            }
                            if(updatesReceived < array.size()) {
                                uploadPreferences(context);                // upload new tag(s)
                            }
                        }

                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    Crittercism.leaveBreadcrumb("NotifyPreferences:loadPreferences(): Error when trying to get user preferences.");
                }
            });
        }
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
        if(!Access.getInstance().isGuestLogin()) {
            // Preference Center requires user information

            // Collect tags
            ArrayList<Tag> tags = new ArrayList<Tag>();
            for(Item item : array) {
                Tag tag = new Tag();
                tag.setTag(item.attribute);
                tag.setEnable(item.enable);
                tags.add(tag);
            }

            if(ProfileDetails.getMember() != null) {
                // Make body
                Preferences prefs = new Preferences();
                prefs.setApplication(APPLICATION);
                prefs.setNotificationType(NOTIFICATION_TYPE);
                prefs.setEmail(ProfileDetails.getMember().getEmailAddress());
                String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                prefs.setUaChannelId(id);
                prefs.setTags(tags);

                // Upload to preference center
                ChannelApi api = Access.getInstance().getChannelApi(false);
                api.setNotificationPreferences(prefs, this);
            } else {
                // Logged in user should have profile with details
                Crittercism.leaveBreadcrumb("NotifyPreferences:uploadPreferences(): User is logged in but profile doesn't have details");
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

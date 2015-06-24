package app.staples.mobile.cfa.home;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.config.model.Area;
import com.staples.mobile.common.access.config.model.Configurator;
import com.staples.mobile.common.access.config.model.Item;
import com.staples.mobile.common.access.config.model.Screen;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.analytics.Tracker;

import java.text.MessageFormat;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.store.StoreItem;
import app.staples.mobile.cfa.store.TimeSpan;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.TileLayout;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private static class Tile {
        private String title;
        private String identifier;

        private Tile(String title, String identifier) {
            this.title = title;
            this.identifier = identifier;
        }
    }

    private View header;
    private TileLayout frame;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("HomeFragment:onCreateView(): Displaying the home/landing screen.");

        View view = layoutInflater.inflate(R.layout.home_fragment, container, false);
        view.setTag(this);

        header = view.findViewById(R.id.header);
        frame = (TileLayout) view.findViewById(R.id.tiles);

        header.findViewById(R.id.login_message).setOnClickListener(this);
        header.findViewById(R.id.reward_message).setOnClickListener(this);
        header.findViewById(R.id.store_layout).setOnClickListener(this);

        refreshMessageBar();
        refreshNearbyStore();
        refreshTiles();

        return (view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.DEFAULT);
        Tracker.getInstance().trackStateForHome(); // Analytics
    }

    public void refreshMessageBar() {
        Access access = Access.getInstance();
        TextView loginText = (TextView) header.findViewById(R.id.login_message);
        TextView rewardText = (TextView) header.findViewById(R.id.reward_message);

        // Logged In
        // Note that member can be null if failure to retrieve profile following successful login
        if (access.isLoggedIn() && !access.isGuestLogin() && ProfileDetails.getMember() != null) {
            float rewards = 0;
            Member member = ProfileDetails.getMember();
            if (member.getRewardsNumber() != null && member.getRewardDetails() != null) {
                rewards = ProfileDetails.getRewardsTotal();
            }
            if (rewards != 0) {
                loginText.setVisibility(View.GONE);
                rewardText.setVisibility(View.VISIBLE);
                String rewardsMessage = MiscUtils.getCurrencyFormat().getPositivePrefix() + (int) rewards + " " + getString(R.string.rewards);
                rewardText.setText(rewardsMessage);
            } else {
                loginText.setVisibility(View.VISIBLE);
                rewardText.setVisibility(View.GONE);
                String text = member.getWelcomeMessage();
                if (text==null || text.isEmpty()) {
                    text = member.getUserName();
                }
                text = MessageFormat.format(getString(R.string.welcome_format), text);
                loginText.setText(text);
            }
        }
        // Not Logged In
        else {
            loginText.setVisibility(View.VISIBLE);
            rewardText.setVisibility(View.GONE);
            loginText.setText(R.string.login_greeting);
        }
    }

    public void refreshNearbyStore() {
        Activity activity = getActivity();
        StoreItem store = LocationFinder.getInstance(activity).getNearestStore();
        if (store!=null) {
            TextView storeName = (TextView) header.findViewById(R.id.store_name);
            storeName.setText(store.formatCityState());

            String status = TimeSpan.formatStatus(activity, store.getSpans(), System.currentTimeMillis());
            int i = status.indexOf(' ');
            if (i > 0) status = status.substring(0, i);
            TextView storeStatus = (TextView) header.findViewById(R.id.store_status);
            storeStatus.setText(status);
        }
    }

    private TileLayout.LayoutParams getTileLayoutParams(String size) {
        if (size==null || size.isEmpty()) return(null);
        switch(Character.toUpperCase(size.charAt(0))) {
            case 'A':
                return(new TileLayout.LayoutParams(4, 4));
            case 'B':
                return(new TileLayout.LayoutParams(4, 2));
            case 'C':
                return(new TileLayout.LayoutParams(2, 2));
            case 'D':
                return(new TileLayout.LayoutParams(4, 1));
            default:
                return(null);
        }
    }

    public void refreshTiles() {
        Resources resources = getActivity().getResources();

        // Get frame of items (safely)
        AppConfigurator appConfigurator = AppConfigurator.getInstance();
        Configurator configurator = appConfigurator.getConfigurator();
        if (configurator==null) return;
        List<Screen> screens = configurator.getScreen();
        if (screens==null || screens.size()==0) return;
        Screen screen = screens.get(0);
        List<Item> items = screen.getItem();
        if (items==null || items.size()==0) return;

        Activity activity = getActivity();
        Picasso picasso = Picasso.with(activity);
        frame.removeAllViews();
        for(Item item : items) {
            // Get identifier (safely)
            String identifier = null;
            List<Area> areas = item.getArea();
            if (areas!=null) {
                for(Area area : areas) {
                    String skuList = area.getSkuList();
                    if (skuList!=null && !skuList.isEmpty()) {
                        identifier = skuList;
                        break;
                    }
                }
            }

            TileLayout.LayoutParams params = getTileLayoutParams(item.getSize());
            if (params!=null) {
                ImageView image = new ImageView(activity);
                image.setLayoutParams(params);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Tile tile = new Tile(item.getTitle(), identifier);
                image.setTag(tile);
                image.setOnClickListener(this);
                frame.addView(image);

                picasso.load(item.getBanner()).into(image);
            }
        }
    }

    @Override
    public void onClick(View view) {
        MainActivity activity = (MainActivity) getActivity();

        switch(view.getId()) {
            case R.id.login_message:
                Access access = Access.getInstance();
                if (access.isLoggedIn() && !access.isGuestLogin()) {
                    activity.selectProfileFragment();
                } else {
                    Tracker.getInstance().trackActionForPersonalizedMessaging("Login");
                    activity.selectLoginFragment();
                }
                break;
            case R.id.reward_message:
                Tracker.getInstance().trackActionForPersonalizedMessaging("Reward");
                activity.selectRewardsFragment();
                break;
            case R.id.store_layout:
                Tracker.getInstance().trackActionForPersonalizedMessaging("Store");
                activity.selectStoreFragment();
                break;
            default:
                Object tag = view.getTag();
                if (tag instanceof Tile) {
                    Tile tile = (Tile) tag;
                    Tracker.getInstance().trackActionForHomePage(tile.title);
                    if (tile.identifier!=null) {
                        activity.selectBundle(tile.title, tile.identifier);
                        Crittercism.leaveBreadcrumb("HomeFragment: User selected "+tile.identifier);
                    }
                }
        }
    }
}

package app.staples.mobile.cfa.browse;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Browse;
import com.staples.mobile.common.access.easyopen.model.browse.Category;
import com.staples.mobile.common.access.easyopen.model.browse.Description;
import com.staples.mobile.common.access.easyopen.model.browse.SubCategory;
import com.staples.mobile.common.analytics.Tracker;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.IdentifierType;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.FixedSizeLayoutManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BrowseFragment extends Fragment  implements Callback<Browse>, View.OnClickListener {
    private static final String TAG = BrowseFragment.class.getSimpleName();

    private static final String EXTERNALROOT = "http://m.staples.com";
    private static final int MAXFETCH = 50;

    private BrowseAdapter adapter;
    private DataWrapper.State state;
    private Dialog popup;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        adapter = new BrowseAdapter(getActivity());
        adapter.setOnClickListener(this);
        fill(null, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("BrowseFragment:onCreateView(): Displaying the Browse screen.");
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.browse_frame, container, false);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.products);
        FixedSizeLayoutManager layoutManager = new FixedSizeLayoutManager(activity);
        layoutManager.setUnitHeight(activity.getResources().getDimensionPixelSize(R.dimen.browse_item_height));
        list.setLayoutManager(layoutManager);
        applyState(view);
        return (view);
    }

    private void applyState(View view) {
        if (view==null) view = getView();
        if (view==null) return;
        DataWrapper wrapper = (DataWrapper) view.findViewById(R.id.wrapper);
        if (adapter!=null) {
            RecyclerView list = (RecyclerView) wrapper.findViewById(R.id.products);
            list.setAdapter(adapter);
        }
        wrapper.setState(state);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.BROWSE);
    }

    void fill(String parentIdentifier, String childIdentifier) {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        if (parentIdentifier!=null || childIdentifier==null) {
            easyOpenApi.topCategories(parentIdentifier, childIdentifier, null, MAXFETCH, this);
        } else {
            easyOpenApi.getCategory(childIdentifier, null, MAXFETCH, null, null, this);
        }
        state = DataWrapper.State.ADDING;
        applyState(null);
    }

    @Override
    public void success(Browse browse, Response response) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        int count = processCategories(browse);
        if (count==0) state = DataWrapper.State.NOMORE;
        else state = DataWrapper.State.DONE;
        applyState(null);
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Activity activity = getActivity();
        if (!(activity instanceof MainActivity)) return;

        String msg = ApiError.getErrorMessage(retrofitError);
        ((MainActivity) activity).showErrorDialog(msg);
        Log.d(TAG, msg);
        state = DataWrapper.State.NOMORE;
        applyState(null);
    }

    private String getTitleFromDescriptions(List<Description> descriptions) {
        if (descriptions==null) return(null);
        for(Description description : descriptions) {
            String title = description.getName();
            if (title==null) title = description.getText();
            if (title==null) title = description.getDescription();
            if (title!=null) {
                title = Html.fromHtml(title).toString();
                return(title);
            }
        }
        return(null);
    }

    private int processCategories(Browse browse) {
        if (browse==null || browse.getRecordSetTotal()==0) return(0);
        List<Category> categories = browse.getCategory();
        if (categories==null || categories.size()==0) return(0);
        int count = 0;

        for(Category category : categories) {
            String parentIdentifier = category.getIdentifier();
            List<SubCategory> subCategories = category.getSubCategory();
            if (subCategories!=null) {
                count += processSubCategories(parentIdentifier, subCategories);
            } else {
                List<Description> descriptions = category.getDescription1();
                String title = getTitleFromDescriptions(descriptions);
                if (title != null) {
                    BrowseItem item = new BrowseItem(BrowseItem.Type.ITEM, title, category.getIdentifier(), null);
                    adapter.addItem(item);
                    count++;
                }
            }
        }

        adapter.notifyDataSetChanged();
        return(count);
    }

    private int processSubCategories(String parentIdentifier, List<SubCategory> subCategories) {
        BrowseItem item;

        int count = 0;
        for(SubCategory subCategory : subCategories) {
            List<SubCategory> subSubCategories = subCategory.getSubCategory();
            if (subSubCategories != null) {
                count += processSubCategories(parentIdentifier, subSubCategories);
            } else {
                List<Description> descriptions = subCategory.getDescription();
                String title = getTitleFromDescriptions(descriptions);
                if (title != null) {
                    Boolean navigable = subCategory.isNavigable();
                    // Category is only viewable on web
                    if (navigable!=null && navigable==false) {
                        item = new BrowseItem(BrowseItem.Type.ITEM, title, null, subCategory.getIdentifier());
                        item.webLink = subCategory.getCategoryUrl();
                    }
                    // Category is a direct "top" category
                    else if (subCategory.getChildCount()>0) {
                        item = new BrowseItem(BrowseItem.Type.ITEM, title, parentIdentifier, subCategory.getIdentifier());
                    }
                    // Category is a "non-top" category
                    else {
                        item = new BrowseItem(BrowseItem.Type.ITEM, title, null, subCategory.getIdentifier());
                    }
                    adapter.addItem(item);
                    count++;
                }
            }
        }
        return(count);
    }

    private void showPopup() {
        popup = new Dialog(getActivity());
        Window window = popup.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(R.drawable.dialog_frame);
        popup.setContentView(R.layout.external_dialog);
        popup.setCanceledOnTouchOutside(false);
        popup.findViewById(R.id.no).setOnClickListener(this);
        popup.findViewById(R.id.yes).setOnClickListener(this);
        popup.show();
    }

    private boolean gotoWeb() {
        BrowseItem item = adapter.getSelectedItem();
        if (item==null) return(false);
        String url = item.webLink;
        if (url==null || url.isEmpty()) return(false);

        if (!url.startsWith("http:")) {
            url = EXTERNALROOT + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            getActivity().startActivity(intent);
        } catch(ActivityNotFoundException e) {
            return(false);
        }
        return(true);
    }

    private void activateItem(BrowseItem item) {
        switch(item.type) {
            case STACK:
                adapter.popStack(item);
                fill(item.parentIdentifier, item.childIdentifier);
                Tracker.getInstance().trackActionForShopByCategory(adapter.getCategoryHierarchy()); // analytics
                break;
            case ITEM:
            case SELECTED:
                if (item.webLink!=null) {
                    adapter.selectItem(item);
                    showPopup();
                } else {
                    switch(IdentifierType.detect(item.childIdentifier)) {
                        case CLASS:
                        case BUNDLE:
                            adapter.selectItem(item);
                            MainActivity activity = (MainActivity) getActivity();
                            if (activity != null) {
                                Tracker.getInstance().trackActionForShopByCategory(adapter.getCategoryHierarchy() + ":" + item.title); // analytics
                                activity.selectBundle(item.title, item.childIdentifier);
                            }
                            break;
                        default:
                            adapter.pushStack(item);
                            fill(item.parentIdentifier, item.childIdentifier);
                            Tracker.getInstance().trackActionForShopByCategory(adapter.getCategoryHierarchy()); // analytics
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.no:
                if (popup != null) popup.dismiss();
                break;
            case R.id.yes:
                if (popup != null) popup.dismiss();
                gotoWeb();
                break;
            default:
                Object obj = view.getTag();
                if (obj instanceof BrowseItem) {
                    activateItem((BrowseItem) obj);
                }
                break;
        }
    }
}

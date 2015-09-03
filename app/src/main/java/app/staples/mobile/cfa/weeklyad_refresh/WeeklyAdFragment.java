package app.staples.mobile.cfa.weeklyad_refresh;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.shoplocal.api.ShopLocalApi;
import com.staples.mobile.common.shoplocal.models.CategoryList;
import com.staples.mobile.common.shoplocal.models.DealList;
import com.staples.mobile.common.shoplocal.models.DealResults;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.widget.HorizontalDivider;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeeklyAdFragment extends Fragment implements View.OnClickListener {

    private ShopLocalApi shopLocalApi;
    ImageView dealImage;
    TextView dealTitle;
    TextView dealPrice;
    TextView dealExpiry;
    LinearLayout dealLayout;

    private RecyclerView mRecyclerView;
    private WeeklyAdCategoryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<DealResults> dealResultsList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        shopLocalApi = Access.getInstance().getShopLocalAPi();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_weekly_ad, container, false);
        dealLayout = (LinearLayout)rootView.findViewById(R.id.deal_layout);
        dealLayout.setOnClickListener(this);
        dealImage = (ImageView)rootView.findViewById(R.id.dealImage);
        dealTitle = (TextView)rootView.findViewById(R.id.dealTitle);
        dealPrice = (TextView)rootView.findViewById(R.id.dealPricing);
        dealExpiry = (TextView)rootView.findViewById(R.id.dealExpiry);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.weekly_ad_category_items);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new HorizontalDivider(getActivity()));
        mAdapter = new WeeklyAdCategoryAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        getWeeklyAdDeals();
        getWeeklyAdCategories();

        return rootView;
    }

    private void getWeeklyAdDeals() {
        if(shopLocalApi == null)return;
        shopLocalApi.getDeals("2278492", new Callback<DealList>() {
            @Override
            public void success(DealList dealList, Response response) {
                dealResultsList = dealList.getDealResultsList();
                int random = (int)(Math.random() * dealResultsList.size());
                DealResults deal = dealResultsList.get(random);
                Picasso.with(getActivity())
                        .load(deal.getImageLocation())
                        .into(dealImage);
                dealTitle.setText(deal.getTitle());
                dealPrice.setText(deal.getDeal());
                String saleStart = deal.getSaleStartDateString().substring(0, deal.getSaleStartDateString().indexOf(" "));
                String saleEnd = deal.getSaleEndDateString().substring(0, deal.getSaleEndDateString().indexOf(" "));
                dealExpiry.setText("Valid " + saleStart + " - " + saleEnd);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void getWeeklyAdCategories() {
        if(shopLocalApi == null)return;
        shopLocalApi.getCategories("2278492", new Callback<CategoryList>() {
            @Override
            public void success(CategoryList categoryList, Response response) {
                mAdapter.fill(categoryList.getCategoryResultsList());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.deal_layout:

                break;
        }
    }
}

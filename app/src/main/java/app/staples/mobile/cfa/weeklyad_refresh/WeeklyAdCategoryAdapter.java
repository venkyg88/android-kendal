package app.staples.mobile.cfa.weeklyad_refresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.shoplocal.models.CategoryResults;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.DrawerItem;
import app.staples.mobile.cfa.MainActivity;

/**
 * Created by Avinash Dodda.
 */
public class WeeklyAdCategoryAdapter extends RecyclerView.Adapter<WeeklyAdCategoryAdapter.ViewHolder>{
    private ArrayList<CategoryResults> array;
    private Context context;
    private LayoutInflater inflater;
    ArrayList<String> categoryTreeIds;
    ArrayList<String> titles;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView categoryTitle;
        public TextView categoryCount;
        private ImageView categoryImage;
        private ClickListener clickListener;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            categoryTitle = (TextView)v.findViewById(R.id.category_title);
            categoryCount = (TextView)v.findViewById(R.id.category_count);
            categoryImage = (ImageView)v.findViewById(R.id.category_image);
        }

        public interface ClickListener {

            /**
             * Called when the view is clicked.
             *
             * @param v view that is clicked
             * @param position of the clicked item
             */
            public void onClick(View v, int position);

        }

        /* Setter for listener. */
        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getPosition());
        }
    }

    public WeeklyAdCategoryAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.array = new ArrayList<CategoryResults>();
        this.categoryTreeIds = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_weekly_ad_category, parent, false);
        ViewHolder vh = new ViewHolder(view);
        vh.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position) {
                WeeklyAdListFragment weeklyAdFragment = new WeeklyAdListFragment();
                weeklyAdFragment.setArguments("2278492", position, categoryTreeIds, titles);
                ((MainActivity) context).selectFragment(DrawerItem.WEEKLYDETAIL, weeklyAdFragment, MainActivity.Transition.RIGHT);
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        CategoryResults category = array.get(position);

        vh.categoryTitle.setText(category.getPreferredDescription());
        vh.categoryCount.setText(String.valueOf(category.getListingCount()));
        Picasso.with(context)
                .load(category.getCategoryTreeImageLocation()) // note that urls returned from category api don't include dimensions
                .error(R.drawable.no_photo)
                .into(vh.categoryImage);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void fill(List<CategoryResults> items) {
        array.addAll(items);
        for (CategoryResults item : items) {
            categoryTreeIds.add(String.valueOf(item.getId()));
            titles.add(item.getPreferredDescription());
        }
        notifyDataSetChanged();
    }
}

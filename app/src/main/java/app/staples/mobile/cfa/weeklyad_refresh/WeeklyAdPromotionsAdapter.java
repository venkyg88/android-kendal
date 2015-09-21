package app.staples.mobile.cfa.weeklyad_refresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.common.shoplocal.models.PromotionPageCategoryResults;

import java.util.ArrayList;
import java.util.List;

import app.staples.R;

/**
 * Created by Avinash Dodda.
 */

public class WeeklyAdPromotionsAdapter extends RecyclerView.Adapter<WeeklyAdPromotionsAdapter.ViewHolder>{
    private ArrayList<PromotionPageCategoryResults> array;
    private Context context;
    private LayoutInflater inflater;
    ArrayList<String> categoryTreeIds;
    ArrayList<String> titles;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView categoryTitle;
        private ImageView categoryImage;
        private ClickListener clickListener;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            categoryTitle = (TextView)v.findViewById(R.id.category_title);
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

    public WeeklyAdPromotionsAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.array = new ArrayList<PromotionPageCategoryResults>();
        this.categoryTreeIds = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.weeklyad_promotion_listings, parent, false);
        ViewHolder vh = new ViewHolder(view);
        vh.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position) {

            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        PromotionPageCategoryResults category = array.get(position);

        vh.categoryTitle.setText(category.getTitle());
        Picasso.with(context)
                .load(category.getImageLocation()) // note that urls returned from category api don't include dimensions
                .error(R.drawable.no_photo)
                .into(vh.categoryImage);
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    public void fill(List<PromotionPageCategoryResults> items) {
        array.addAll(items);
        notifyDataSetChanged();
    }

    public void clear(){
        array.clear();
    }
}

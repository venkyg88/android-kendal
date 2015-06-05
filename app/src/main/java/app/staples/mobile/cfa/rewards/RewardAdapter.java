package app.staples.mobile.cfa.rewards;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.common.access.easyopen.model.member.Reward;

import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {

    private static final String TAG = RewardAdapter.class.getSimpleName();

    private MainActivity activity;
    private int rewardItemLayoutResId;

    List<Reward> rewards;

    // widget listeners
    private View.OnClickListener listener;

    /** constructor */
    public RewardAdapter(Activity activity, View.OnClickListener listener) {
        rewardItemLayoutResId = R.layout.coupon_item_redeemable;
        this.activity = (MainActivity)activity;
        this.listener = listener;
    }

    @Override
    public RewardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity)
                .inflate(rewardItemLayoutResId, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Reward reward = rewards.get(position);

        // set reward text
        vh.amountField.setText(reward.getAmount());
        vh.expireField.setText("expires " + reward.getExpiryDate());

        Resources r = activity.getResources();

        // set up applied state
        int blackTextColor = r.getColor(R.color.staples_black);
        int grayTextColor = r.getColor(R.color.staples_middle_gray);
        if (reward.isIsApplied()) {
            vh.amountField.setTextColor(grayTextColor);
            vh.expireField.setTextColor(grayTextColor);
            vh.addButton.setText(R.string.added_to_cart);
        } else {
            vh.amountField.setTextColor(blackTextColor);
            vh.expireField.setTextColor(blackTextColor);
        }

        vh.addButton.setTag(reward);
        vh.viewButton.setTag(reward);

        // set widget listeners
        vh.addButton.setOnClickListener(listener);
        vh.viewButton.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView amountField;
        private TextView expireField;
        private Button addButton;
        private Button viewButton;

        /** constructor */
        public ViewHolder (View itemView) {
            super(itemView);
            amountField = (TextView) itemView.findViewById(R.id.coupon_amount);
            expireField = (TextView) itemView.findViewById(R.id.coupon_expire);
            addButton = (Button) itemView.findViewById(R.id.coupon_add_button);
            viewButton = (Button) itemView.findViewById(R.id.coupon_view_button);
        }
    }
}

package app.staples.mobile.cfa.notify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import app.staples.R;

import java.util.ArrayList;

public class NotifyPrefsAdapter extends RecyclerView.Adapter<NotifyPrefsAdapter.ViewHolder> {
    private static final String TAG = NotifyPrefsAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private CompoundButton enable;

        private ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            enable = (CompoundButton) view.findViewById(R.id.enable);
        }
    }

    private LayoutInflater inflater;
    private ArrayList<NotifyPreferences.Item> array;
    private CompoundButton.OnCheckedChangeListener listener;

    public NotifyPrefsAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setArray(ArrayList<NotifyPreferences.Item> array) {
        this.array = array;
    }

    public void setOnCheckedChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        if (array==null) return(0);
        return(array.size());
    }

    public NotifyPreferences.Item getItem(int position) {
        return(array.get(position));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(R.layout.notify_prefs_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        NotifyPreferences.Item item = array.get(position);
        vh.title.setText(item.title);
        vh.enable.setTag(item);
        vh.enable.setOnCheckedChangeListener(null);
        vh.enable.setChecked(item.enable);
        vh.enable.setOnCheckedChangeListener(listener);
    }
}

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

public class TagItemAdapter extends RecyclerView.Adapter<TagItemAdapter.ViewHolder> {
    private static final String TAG = TagItemAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private CompoundButton enable;

        private ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            enable = (CompoundButton) view.findViewById(R.id.enable);
        }
    }

    public static class Item {
        public String tag;
        public String title;
        public boolean enable;
        
        public Item(String tag, String title) {
            this.tag = tag;
            this.title = title;
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Item> array;
    private CompoundButton.OnCheckedChangeListener listener;

    public TagItemAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<Item>();
    }

    public void setOnCheckedChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    public Item getItem(int position) {
        return(array.get(position));
    }

    public Item addTagItem(String tag, String title) {
        Item item = new Item(tag, title);
        array.add(item);
        return(item);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(R.layout.notify_prefs_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        vh.enable.setOnCheckedChangeListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Item item = array.get(position);
        vh.enable.setTag(item);
        vh.title.setText(item.title);
        vh.enable.setChecked(item.enable);
    }
}

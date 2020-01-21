package ndds.com.trakidhome;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ChildListAdapter extends RecyclerView.Adapter<ChildListAdapter.ChildViewHolder> implements View.OnClickListener {
    private ArrayList<String> children;
    private int itemSelectedColor;
    private int normalColor;
    private int indexOfClickedItem = 1;

    public ChildListAdapter(ArrayList<String> children, int itemSelectedColor, int normalColor) {
        this.children = children;
        this.itemSelectedColor = itemSelectedColor;
        this.normalColor = normalColor;
    }
    public abstract void onItemClicked(int position);

    @Override
    public void onClick(View v) {

        if ((int) v.getTag() == 0) {
            //don't update when + button clicked
            onItemClicked(0);
            return;
        }
        int prev=indexOfClickedItem;
        indexOfClickedItem = (int) v.getTag();
        notifyItemChanged(prev);
        notifyItemChanged(indexOfClickedItem);
        /*notifyItemChanged(indexOfClickedItem);
        notifyItemChanged(0);*/
        onItemClicked((int) v.getTag());
    }

    class ChildViewHolder extends RecyclerView.ViewHolder {

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }


    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        return new ChildViewHolder(
                v=LayoutInflater.from(parent.getContext()).inflate(R.layout.single_child_selector, parent, false)
        );
    }
    public void setSelectedItemDarker(View v, boolean mode){
        v.getBackground().setColorFilter(mode?itemSelectedColor:normalColor, PorterDuff.Mode.SRC);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(children.get(position));
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
        setSelectedItemDarker(holder.itemView,position==indexOfClickedItem);
    }
    @Override
    public int getItemCount() {
        return children.size();
    }
}

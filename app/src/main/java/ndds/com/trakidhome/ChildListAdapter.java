package ndds.com.trakidhome;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ChildListAdapter extends RecyclerView.Adapter<ChildListAdapter.ChildViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private final int addButtonColor;
    private ArrayList<String> children;
    private int itemSelectedColor;
    private int normalColor;
    private int indexOfClickedItem = 1;

    public ChildListAdapter(ArrayList<String> children, Resources resources) {
        this.children = children;
        this.itemSelectedColor = resources.getColor(R.color.appThemeDark);
        this.normalColor = resources.getColor(R.color.appThemeNormal);
        this.addButtonColor = resources.getColor(R.color.appThemeCyan);
    }
    public abstract void onItemClicked(int position);

    public void addChildAndFocus(String name) {
        children.add(name);
        indexOfClickedItem = children.size() - 1;
        notifyDataSetChanged();
        // onItemClicked(children.size()-1);
    }

    public void forceFocusItem(int position) {
        indexOfClickedItem = position + 1;
        notifyDataSetChanged();
    }

    public void removeItemAndFocusFirstItem(int removedPosition) {
        if (children.size() > 2)
            indexOfClickedItem = 1;
        children.remove(removedPosition + 1);
        notifyDataSetChanged();
    }

    @Override
    public boolean onLongClick(View v) {
        if ((int) v.getTag() == 0)
            return true;//ignore if plus button clicked
        onItemLongClicked((int) v.getTag());
        return true;
    }

    abstract void onItemLongClicked(int position);
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
        return new ChildViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.single_child_selector, parent, false)
        );
    }
    public void setSelectedItemDarker(View v, boolean mode){
        v.getBackground().setColorFilter(mode?itemSelectedColor:normalColor, PorterDuff.Mode.SRC);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        String name = children.get(position);
        ((TextView) holder.itemView).setText(Character.toString(name.charAt(0)).toUpperCase() + name.substring(1));
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
        holder.itemView.setOnLongClickListener(this);
        if (position == 0) { // make the add button green
            holder.itemView.getBackground().setColorFilter(addButtonColor, PorterDuff.Mode.SRC);
            ((TextView) holder.itemView).setTextColor(Color.BLACK);
        } else
            setSelectedItemDarker(holder.itemView, position == indexOfClickedItem);
    }
    @Override
    public int getItemCount() {
        return children.size();
    }
}

package com.alexandrius.accordionswipelayout.sample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alexandrius.accordionswipelayout.library.SwipeLayout;

import java.util.ArrayList;

/**
 * Created by alex on 12/6/16.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<String> strings = new ArrayList<>();

    public RecyclerAdapter() {
        for (int i = 0; i < 30; i++) {
            strings.add(i + "");
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText("Item # " + strings.get(position));
        ((SwipeLayout) holder.itemView).setItemState(SwipeLayout.ITEM_STATE_COLLAPSED, false);
    }

    @Override
    public int getItemCount() {
        return strings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, SwipeLayout.OnSwipeItemClickListener {

        TextView textView;

        public ViewHolder(final View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.recycler_item_tv);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            ((SwipeLayout) itemView).setOnSwipeItemClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Clicked at " + strings.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(view.getContext(), "Long Clicked at " + strings.get(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public void onSwipeItemClick(boolean left, int index) {
            if (left) {
                Toast.makeText(itemView.getContext(), "Left", Toast.LENGTH_SHORT).show();
            } else {
                if (index == 0) {
                    Toast.makeText(itemView.getContext(), "Reload", Toast.LENGTH_SHORT).show();
                } else if (index == 1) {
                    Toast.makeText(itemView.getContext(), "Settings", Toast.LENGTH_SHORT).show();
                } else if (index == 2) {
                    int pos = getAdapterPosition();
                    strings.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(itemView.getContext(), "Trash", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

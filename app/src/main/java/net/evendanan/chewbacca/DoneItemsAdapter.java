package net.evendanan.chewbacca;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.base.Preconditions;

import net.evendanan.chewbacca.model.DoneItemModel;

import java.util.ArrayList;
import java.util.List;

public class DoneItemsAdapter extends RecyclerView.Adapter<DoneItemsAdapter.DoneViewHolder> {

    public static class DoneViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        public final ImageView imageView;
        @NonNull
        public final TextView title;

        public DoneViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) Preconditions.checkNotNull(itemView.findViewById(R.id.image_view));
            title = (TextView) Preconditions.checkNotNull(itemView.findViewById(R.id.title));
        }
    }

    @NonNull
    private List<DoneItemModel> mDoneItemsList = new ArrayList<>();
    private final Activity mActivity;
    private final LayoutInflater mLayoutInflater;

    public DoneItemsAdapter(MainActivity mainActivity) {
        mActivity = mainActivity;
        mLayoutInflater = LayoutInflater.from(mainActivity);
    }

    @Override
    public DoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View pendingItemLayout = mLayoutInflater.inflate(R.layout.done_item, parent, false);

        return new DoneViewHolder(pendingItemLayout);
    }

    @Override
    public void onBindViewHolder(DoneViewHolder holder, int position) {
        DoneItemModel item = mDoneItemsList.get(position);
        Glide.with(mActivity).load(item.imageUri).into(holder.imageView);
        holder.title.setText(mActivity.getString(R.string.item_took_time_duration, DateUtils.formatElapsedTime(item.itemDuration/1000)));
    }

    @Override
    public int getItemCount() {
        return mDoneItemsList.size();
    }

    public void addDoneItem(@NonNull DoneItemModel item) {
        mDoneItemsList.add(0, Preconditions.checkNotNull(item));
        notifyItemInserted(0);
    }

    @NonNull
    public DoneItemModel removeLatestDoneItem() {
        DoneItemModel item = mDoneItemsList.remove(0);
        notifyItemRemoved(0);
        return item;
    }

    public void setData(@NonNull List<DoneItemModel> items) {
        mDoneItemsList = Preconditions.checkNotNull(items);
        notifyDataSetChanged();
    }
}

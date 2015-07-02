package net.evendanan.chewbacca;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.base.Preconditions;

import net.evendanan.chewbacca.model.PendingItemModel;

import java.util.ArrayList;
import java.util.List;

public class PendingItemsAdapter extends RecyclerView.Adapter<PendingItemsAdapter.PendingViewHolder> {

    private static final int TYPE_ADD_PENDING_ITEM = 0;
    private static final int TYPE_PENDING_ITEM = 1;
    private final View.OnClickListener mDeleteItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                PendingViewHolder holder = (PendingViewHolder) v.getTag();
                final int adapterPosition = holder.getAdapterPosition();
                mPendingItemsList.remove(adapterPosition - 1);
                notifyItemRemoved(adapterPosition);
            }
        }
    };

    public static class PendingViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        public final ImageView imageView;
        @Nullable
        public final TextView title;
        @Nullable
        public final TextView subtitle;
        @Nullable
        public final View deleteIcon;

        public PendingViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) Preconditions.checkNotNull(itemView.findViewById(R.id.image_view));
            title = (TextView) itemView.findViewById(R.id.title);
            subtitle = (TextView) itemView.findViewById(R.id.subtitle);
            deleteIcon = itemView.findViewById(R.id.delete_item_icon);
        }
    }

    private final List<View> mDeleteItemIcons = new ArrayList<>();
    private List<PendingItemModel> mPendingItemsList = new ArrayList<>();
    private final MainActivity mActivity;
    private final LayoutInflater mLayoutInflater;

    private boolean mInEditMode = true;

    public PendingItemsAdapter(MainActivity mainActivity) {
        mActivity = mainActivity;
        mLayoutInflater = LayoutInflater.from(mainActivity);
    }

    @Override
    public int getItemViewType(int position) {
        if (mInEditMode && position == 0) return TYPE_ADD_PENDING_ITEM;
        else return TYPE_PENDING_ITEM;
    }

    @Override
    public PendingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View pendingItemLayout = mLayoutInflater.inflate(
                viewType == TYPE_ADD_PENDING_ITEM? R.layout.add_pending_item : R.layout.pending_item, parent, false);
        PendingViewHolder holder = new PendingViewHolder(pendingItemLayout);
        if (viewType == TYPE_ADD_PENDING_ITEM) {
            holder.imageView.setImageResource(R.drawable.ic_add_item);
            pendingItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.startTakeCameraPhotoFlow();
                }
            });
        }
        return holder;
    }

    @Override
    public void onViewAttachedToWindow(PendingViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        View deleteIcon = holder.deleteIcon;
        if (deleteIcon == null) return;
        mDeleteItemIcons.add(deleteIcon);
        deleteIcon.setVisibility(mInEditMode? View.VISIBLE : View.GONE);
        deleteIcon.setOnClickListener(mDeleteItemClickListener);
    }

    @Override
    public void onViewDetachedFromWindow(PendingViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        View deleteIcon = holder.deleteIcon;
        if (deleteIcon == null) return;
        mDeleteItemIcons.remove(deleteIcon);
    }

    @Override
    public void onBindViewHolder(PendingViewHolder holder, int position) {
        if (mInEditMode && position == 0) return;
        if (mInEditMode) position--;
        PendingItemModel pendingItem = mPendingItemsList.get(position);
        Glide.with(mActivity).load(pendingItem.imageUri).centerCrop().into(holder.imageView);
        holder.deleteIcon.setTag(holder);
    }

    @Override
    public int getItemCount() {
        return mPendingItemsList.size() + (mInEditMode ? 1 : 0);
    }

    public void addPendingItemToLast(PendingItemModel pendingItem) {
        Preconditions.checkArgument(mInEditMode, "May not add pending items while not in Edit-Mode!");
        mPendingItemsList.add(pendingItem);
        notifyItemInserted(mPendingItemsList.size());
    }

    public void addPendingItemToHead(PendingItemModel pendingItem) {
        final int insertLocation = mInEditMode? 1 : 0;
        mPendingItemsList.add(0, pendingItem);
        notifyItemInserted(insertLocation);
    }

    public boolean isInEditMode() {
        return mInEditMode;
    }

    public void setEditMode(boolean editMode) {
        if (editMode != mInEditMode) {
            mInEditMode = editMode;
            if (mInEditMode) {
                notifyItemInserted(0);
                Animation inAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.delete_icon_in);
                for (View deleteIcon : mDeleteItemIcons) {
                    deleteIcon.clearAnimation();
                    deleteIcon.setVisibility(View.VISIBLE);
                    deleteIcon.startAnimation(inAnimation);
                }
            } else {
                notifyItemRemoved(0);
                Animation inAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.delete_icon_out);
                for (View deleteIcon : mDeleteItemIcons) {
                    deleteIcon.clearAnimation();
                    deleteIcon.setVisibility(View.GONE);
                    deleteIcon.startAnimation(inAnimation);
                }
            }
        }
    }

    public PendingItemModel popTopPendingItem() {
        Preconditions.checkArgument(!mInEditMode, "May not pop pending items while in Edit-Mode!");
        notifyItemRemoved(0);
        return mPendingItemsList.remove(0);
    }

    public void setData(List<PendingItemModel> items) {
        Preconditions.checkArgument(mInEditMode, "May set items while only in Edit-Mode!");
        mPendingItemsList = items;
        notifyDataSetChanged();
    }
}

package net.evendanan.chewbacca.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Session implements IModel {
    public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
        public Session createFromParcel(Parcel in) {
            CurrentItemModel currentItemModel = in.readParcelable(CurrentItemModel.class.getClassLoader());
            Parcelable[] pendingItems = in.readParcelableArray(PendingItemModel.class.getClassLoader());
            Parcelable[] doneItems = in.readParcelableArray(DoneItemModel.class.getClassLoader());

            ArrayList<PendingItemModel> pendingItemsList = new ArrayList<>(pendingItems.length);
            for (Parcelable pendingItem : pendingItems) {
                pendingItemsList.add((PendingItemModel) pendingItem);
            }

            ArrayList<DoneItemModel> doneItemsList = new ArrayList<>(doneItems.length);
            for (Parcelable doneItem : doneItems) {
                doneItemsList.add((DoneItemModel) doneItem);
            }

            return new Session(pendingItemsList, doneItemsList, currentItemModel);
        }

        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    @NonNull
    public final List<PendingItemModel> pendingItems;
    @NonNull
    public final List<DoneItemModel> doneItems;
    @Nullable
    public CurrentItemModel currentItem;

    public Session(@NonNull List<PendingItemModel> pendingItems, @NonNull List<DoneItemModel> doneItems, @Nullable CurrentItemModel currentItem) {
        this.pendingItems = pendingItems;
        this.doneItems = doneItems;
        this.currentItem = currentItem;
    }

    public Session() {
        pendingItems = new ArrayList<>();
        doneItems = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(currentItem, 0);
        dest.writeParcelableArray(pendingItems.toArray(new PendingItemModel[pendingItems.size()]), 0);
        dest.writeParcelableArray(doneItems.toArray(new DoneItemModel[doneItems.size()]), 0);
    }
}

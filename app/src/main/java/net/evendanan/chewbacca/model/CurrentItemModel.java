package net.evendanan.chewbacca.model;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class CurrentItemModel implements IModel {

    public static final Creator<CurrentItemModel> CREATOR = new Creator<CurrentItemModel>() {
        public CurrentItemModel createFromParcel(Parcel in) {
            final Uri imageUri = in.readParcelable(CurrentItemModel.class.getClassLoader());
            final long startTimeMillis = in.readLong();
            return new CurrentItemModel(imageUri, startTimeMillis);
        }

        public CurrentItemModel[] newArray(int size) {
            return new CurrentItemModel[size];
        }
    };

    public final long startTimeMillis;

    @NonNull
    public final Uri imageUri;

    public CurrentItemModel() {
        this(Uri.EMPTY);
    }

    public CurrentItemModel(@NonNull PendingItemModel pendingItemModel) {
        this(pendingItemModel.imageUri);
    }

    public CurrentItemModel(@NonNull DoneItemModel doneItemModel) {
        this(doneItemModel.imageUri, System.currentTimeMillis() - doneItemModel.itemDuration);
    }

    private CurrentItemModel(@NonNull Uri imageUri) {
        this(Preconditions.checkNotNull(imageUri), System.currentTimeMillis());
    }

    private CurrentItemModel(@NonNull Uri imageUri, long startTimeMillis) {
        this.imageUri = Preconditions.checkNotNull(imageUri);
        this.startTimeMillis = startTimeMillis;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUri, 0);
        dest.writeLong(startTimeMillis);
    }
}

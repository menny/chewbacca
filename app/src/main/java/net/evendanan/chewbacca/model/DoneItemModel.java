package net.evendanan.chewbacca.model;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class DoneItemModel implements IModel {

    public static final Creator<DoneItemModel> CREATOR = new Creator<DoneItemModel>() {
        public DoneItemModel createFromParcel(Parcel in) {
            final Uri imageUri = in.readParcelable(DoneItemModel.class.getClassLoader());
            final long itemDuration = in.readLong();
            return new DoneItemModel(imageUri, itemDuration);
        }

        public DoneItemModel[] newArray(int size) {
            return new DoneItemModel[size];
        }
    };

    public final long itemDuration;

    @NonNull
    public final Uri imageUri;

    public DoneItemModel() {
        this(Uri.EMPTY, 0);
    }

    public DoneItemModel(@NonNull CurrentItemModel currentItemModel) {
        this(currentItemModel.imageUri, System.currentTimeMillis() - currentItemModel.startTimeMillis);
    }
    public DoneItemModel(@NonNull Uri imageUri, long itemDuration) {
        this.imageUri = Preconditions.checkNotNull(imageUri);
        this.itemDuration = itemDuration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUri, 0);
        dest.writeLong(itemDuration);
    }
}

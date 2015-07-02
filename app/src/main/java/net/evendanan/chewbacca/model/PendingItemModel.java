package net.evendanan.chewbacca.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class PendingItemModel implements IModel {

    public static final Parcelable.Creator<PendingItemModel> CREATOR = new Parcelable.Creator<PendingItemModel>() {
        public PendingItemModel createFromParcel(Parcel in) {
            final Uri imageUri = in.readParcelable(PendingItemModel.class.getClassLoader());
            return new PendingItemModel(imageUri);
        }

        public PendingItemModel[] newArray(int size) {
            return new PendingItemModel[size];
        }
    };

    @NonNull
    public final Uri imageUri;

    public PendingItemModel() {
        imageUri = Uri.EMPTY;
    }

    public PendingItemModel(@NonNull Uri imageUri) {
        this.imageUri = Preconditions.checkNotNull(imageUri);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(imageUri, 0);
    }
}

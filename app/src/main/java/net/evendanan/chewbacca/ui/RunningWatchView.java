package net.evendanan.chewbacca.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.google.common.base.Preconditions;

public class RunningWatchView extends TextView {

    public static final long NO_TIME_SET = 0l;

    private long mStartTimeMillis = NO_TIME_SET;
    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStartTimeMillis == NO_TIME_SET) {
                setText("00:00");
            } else {
                final long runningTime = System.currentTimeMillis() - mStartTimeMillis;
                final String elapsedTime = DateUtils.formatElapsedTime(runningTime / 1000);
                setText(elapsedTime);
                postDelayed(mUpdateTextRunnable, 100);
            }
        }
    };

    public RunningWatchView(Context context) {
        super(context);
        updateRunningWatchText();
    }

    public RunningWatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateRunningWatchText();
    }

    public RunningWatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateRunningWatchText();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RunningWatchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        updateRunningWatchText();
    }

    public void setStartTime(long startTimeMillis) {
        Preconditions.checkArgument(startTimeMillis >= NO_TIME_SET);
        mStartTimeMillis = startTimeMillis;
        updateRunningWatchText();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateRunningWatchText();

    }

    @Override
    public Parcelable onSaveInstanceState() {
        return new WatchState(super.onSaveInstanceState(), mStartTimeMillis);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state != null && state instanceof WatchState) {
            WatchState watchState = (WatchState) state;
            super.onRestoreInstanceState(watchState.superState);
            setStartTime(watchState.startTime);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void updateRunningWatchText() {
        mUpdateTextRunnable.run();
    }

    private static class WatchState implements Parcelable {
        public final Parcelable superState;
        public final long startTime;

        private WatchState(Parcelable superState, long startTime) {
            this.superState = superState;
            this.startTime = startTime;
        }

        private WatchState(Parcel in) {
            superState = in.readParcelable(WatchState.class.getClassLoader());
            startTime = in.readLong();
        }

        public static final Creator<WatchState> CREATOR = new Creator<WatchState>() {
            @Override
            public WatchState createFromParcel(Parcel in) {
                return new WatchState(in);
            }

            @Override
            public WatchState[] newArray(int size) {
                return new WatchState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(superState, 0);
            dest.writeLong(startTime);
        }
    }
}

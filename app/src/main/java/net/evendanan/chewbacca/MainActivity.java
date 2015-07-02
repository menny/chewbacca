package net.evendanan.chewbacca;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import net.evendanan.chewbacca.model.CurrentItemModel;
import net.evendanan.chewbacca.model.DoneItemModel;
import net.evendanan.chewbacca.model.GsonFactory;
import net.evendanan.chewbacca.model.PendingItemModel;
import net.evendanan.chewbacca.model.Session;
import net.evendanan.chewbacca.ui.RunningWatchView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private enum UiState {
        NoSchedule,
        BeforeScheduleStart,
        ScheduleInProgress,
        ScheduleDone
    }

    private static final String TAG = "CHWBC_MainActivity";

    private static final int CAMERA_REQUEST_CODE = 213;

    private static final String PREFS_KEY_CURRENT_SESSION = "PREFS_KEY_CURRENT_SESSION";

    private static final String SAVED_INSTANCE_KEY_TEMP_CAMERA_FILE = "SAVED_INSTANCE_KEY_TEMP_CAMERA_FILE";
    private static final String SAVED_INSTANCE_KEY_SESSION = "SAVED_INSTANCE_KEY_SESSION";

    private RecyclerView mDoneItemsRecyclerView;
    private RecyclerView mPendingItemsRecyclerView;

    private Session mCurrentSession;

    private ImageView mCurrentItem;
    private RunningWatchView mRunningWatch;
    private Uri mTempImageFileUri;
    private DoneItemsAdapter mDoneItemsAdapter;
    private PendingItemsAdapter mPendingItemsAdapter;

    private SharedPreferences mSharedPreferences;
    private Gson mGson;
    private final OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return handleTap();
        }

        private boolean handleTap() {
            switch (getCurrentUiState()) {
                case ScheduleInProgress:
                case BeforeScheduleStart:
                    nextScheduleItem();
                    return true;
                case ScheduleDone:
                    restartSchedule();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return handleTap();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            final UiState uiState = getCurrentUiState();
            if (velocityX > 0 && (uiState == UiState.ScheduleInProgress || uiState == UiState.ScheduleDone)) {
                //fling from left to right. Rewind item.
                previousScheduleItem();
                return true;
            } else if (velocityX < 0 && (uiState == UiState.ScheduleInProgress || uiState == UiState.BeforeScheduleStart)) {
                //fling from right to left. Next item.
                nextScheduleItem();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        mRunningWatch = (RunningWatchView) findViewById(R.id.running_watch);
        mDoneItemsRecyclerView = (RecyclerView) findViewById(R.id.done_items_list);
        mDoneItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDoneItemsRecyclerView.addItemDecoration(new MarginDecoration(this));
        mDoneItemsAdapter = new DoneItemsAdapter(this);
        mDoneItemsRecyclerView.setAdapter(mDoneItemsAdapter);
        mPendingItemsRecyclerView = (RecyclerView) findViewById(R.id.pending_items_list);
        mPendingItemsAdapter = new PendingItemsAdapter(this);
        mPendingItemsAdapter.setEditMode(true);
        mPendingItemsRecyclerView.setAdapter(mPendingItemsAdapter);
        mPendingItemsRecyclerView.addItemDecoration(new MarginDecoration(this));
        mPendingItemsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mCurrentItem = (ImageView) findViewById(R.id.current_item_image);
        mCurrentItem.setOnTouchListener(new View.OnTouchListener() {
            GestureDetectorCompat mGestureDetectorCompat = new GestureDetectorCompat(MainActivity.this, mGestureListener);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;//always handle the touches
            }
        });
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mGson = GsonFactory.build();

        if (savedInstanceState != null) {
            mTempImageFileUri = savedInstanceState.getParcelable(SAVED_INSTANCE_KEY_TEMP_CAMERA_FILE);
            if (savedInstanceState.containsKey(SAVED_INSTANCE_KEY_SESSION)) {
                mCurrentSession = savedInstanceState.getParcelable(SAVED_INSTANCE_KEY_SESSION);
            }
        }

        if (mCurrentSession == null) {
            String currentSessionJson = mSharedPreferences.getString(PREFS_KEY_CURRENT_SESSION, null);
            if (TextUtils.isEmpty(currentSessionJson)) {
                mCurrentSession = new Session();
            } else {
                try {
                    mCurrentSession = mGson.fromJson(currentSessionJson, Session.class);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to parse stored current-session pref.", e);
                    mCurrentSession = new Session();
                }
            }
        }

        mPendingItemsAdapter.setData(mCurrentSession.pendingItems);
        mDoneItemsAdapter.setData(mCurrentSession.doneItems);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateCurrentItemUi();
    }

    private UiState getCurrentUiState() {
        if (mCurrentSession.currentItem != null) {
            return UiState.ScheduleInProgress;
        } else if (mDoneItemsAdapter.getItemCount() > 0) {
            return UiState.ScheduleDone;
        } else if (mPendingItemsAdapter.getItemCount() > 0) {
            return UiState.BeforeScheduleStart;
        } else {
            return UiState.NoSchedule;
        }
    }

    private void updateCurrentItemUi() {
        switch (getCurrentUiState()) {
            case NoSchedule:
                mCurrentItem.setImageDrawable(null);
                mPendingItemsAdapter.setEditMode(true);
                break;
            case BeforeScheduleStart:
                Glide.with(this).load(R.drawable.ic_start_schedule).fitCenter().into(mCurrentItem);
                mPendingItemsAdapter.setEditMode(true);
                break;
            case ScheduleInProgress:
                Preconditions.checkNotNull(mCurrentSession.currentItem);
                Glide.with(this).load(mCurrentSession.currentItem.imageUri).centerCrop().into(mCurrentItem);
                mPendingItemsAdapter.setEditMode(false);
                break;
            case ScheduleDone:
                Glide.with(this).load(R.drawable.ic_restart).fitCenter().into(mCurrentItem);
                mPendingItemsAdapter.setEditMode(false);
                break;
        }
    }

    public void startTakeCameraPhotoFlow() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mTempImageFileUri = Uri.fromFile(createImageFile(getApplicationContext()));
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageFileUri);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Mmm, can't interact with camera. Storage full?", Toast.LENGTH_LONG).show();
            return;
        }
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(getApplicationContext(), "Mmm, can't interact with camera. Maybe camera app is broken?", Toast.LENGTH_LONG).show();
        }
    }

    private void persistCurrentSessionState() {
        mSharedPreferences.edit().putString(PREFS_KEY_CURRENT_SESSION, mGson.toJson(mCurrentSession)).apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTempImageFileUri != null) {
            outState.putParcelable(SAVED_INSTANCE_KEY_TEMP_CAMERA_FILE, mTempImageFileUri);
        }
        outState.putParcelable(SAVED_INSTANCE_KEY_SESSION, mCurrentSession);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && mTempImageFileUri != null) {
            mPendingItemsAdapter.addPendingItemToLast(new PendingItemModel(mTempImageFileUri));
            persistCurrentSessionState();
            mTempImageFileUri = null;
        }
    }

    private void restartSchedule() {
        while (mDoneItemsAdapter.getItemCount() > 0 || mCurrentSession.currentItem != null) {
            previousScheduleItem();
        }
    }

    private void nextScheduleItem() {
        mPendingItemsAdapter.setEditMode(false);

        if (mCurrentSession.currentItem != null) {
            mDoneItemsAdapter.addDoneItem(new DoneItemModel(mCurrentSession.currentItem));
        }
        if (mPendingItemsAdapter.getItemCount() > 0) {
            mCurrentSession.currentItem = new CurrentItemModel(mPendingItemsAdapter.popTopPendingItem());
            mRunningWatch.setStartTime(mCurrentSession.currentItem.startTimeMillis);
        } else {
            mCurrentSession.currentItem = null;
            mRunningWatch.setStartTime(RunningWatchView.NO_TIME_SET);
        }

        updateCurrentItemUi();

        persistCurrentSessionState();
    }

    private void previousScheduleItem() {
        if (mPendingItemsAdapter.isInEditMode()) return;
        if (mCurrentSession.currentItem != null) {
            mPendingItemsAdapter.addPendingItemToHead(new PendingItemModel(mCurrentSession.currentItem.imageUri));
        }

        if (mDoneItemsAdapter.getItemCount() > 0) {
            mCurrentSession.currentItem = new CurrentItemModel(mDoneItemsAdapter.removeLatestDoneItem());
            mRunningWatch.setStartTime(mCurrentSession.currentItem.startTimeMillis);
        } else {
            mCurrentSession.currentItem = null;
            mRunningWatch.setStartTime(RunningWatchView.NO_TIME_SET);
        }

        updateCurrentItemUi();

        persistCurrentSessionState();
    }

    private static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

}

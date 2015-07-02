package net.evendanan.chewbacca;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class MarginDecoration extends RecyclerView.ItemDecoration {
    private final int mMargin;

    public MarginDecoration(Context context) {
        mMargin = context.getResources().getDimensionPixelSize(R.dimen.global_content_padding_side);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(mMargin, mMargin, mMargin, mMargin);
    }
}

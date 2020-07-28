package com.example.catchy.misc;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;

public class MarqueeTextView extends AppCompatTextView implements View.OnLayoutChangeListener {
    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSelected(true);

        addOnLayoutChangeListener(this);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = bottom - top;
        layoutParams.width = right - left;
        removeOnLayoutChangeListener(this);
        setLayoutParams(layoutParams);
    }
}

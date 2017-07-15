package com.irwin.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Irwin on 2015/11/23.
 */
public class ViewUtil {
    public static void extendTouchArea(final View v, final int left, final int top, final int right, final int bottom) {
        final ViewParent parent = v.getParent();
        if (parent != null && parent instanceof View) {
            ((View) parent).post(new Runnable() {
                @Override
                public void run() {
                    Rect rect = new Rect();
                    v.getHitRect(rect);
                    rect.right += right;
                    rect.left -= left;
                    rect.top -= top;
                    rect.bottom += bottom;
                    TouchDelegate delegate = new TouchDelegate(rect, v);
                    ((View) parent).setTouchDelegate(delegate);
                }
            });
        }

    }

    public static void extendTouchArea(View v, int size) {
        extendTouchArea(v, size, size, size, size);
    }

    public static void restoreTouchArea(View v) {
        final ViewParent parent = v.getParent();
        if (parent != null && parent instanceof View) {
            ((View) parent).setTouchDelegate(new TouchDelegate(new Rect(), v));
        }
    }

    /**
     * Hide soft input keyboard
     *
     * @param focusView
     */
    public static void hideKeyboard(EditText focusView) {
        focusView.clearFocus();
        InputMethodManager inputManager = (InputMethodManager) focusView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
    }

}

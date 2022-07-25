package theopalgames.tanks;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidKeyboardHeightListener implements IKeyboardHeightListener
{
    protected AndroidApplication androidApplication;
    protected AndroidGlobalLayoutListener globalLayoutListener;

    protected double defaultHeight = -1;

    public AndroidKeyboardHeightListener(AndroidApplication androidApplication)
    {
        this.androidApplication = androidApplication;
    }

    @Override
    public void init()
    {
        globalLayoutListener = new AndroidGlobalLayoutListener(androidApplication);
        Window window = androidApplication.getWindow();
        if (window != null)
        {
            View decorView = window.getDecorView();
            if (decorView != null)
            {
                View rootView = decorView.getRootView();
                if (rootView != null)
                {
                    ViewTreeObserver viewTreeObserver= rootView.getViewTreeObserver();
                    if (viewTreeObserver != null)
                    {
                        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);
                    }
                }
            }
        }
    }

    @Override
    public double getUsableWindowHeight()
    {
        if (globalLayoutListener != null)
        {
            double h = globalLayoutListener.getHeight();

            if (h != 0 && defaultHeight == -1)
                defaultHeight = h;

            return h / defaultHeight;
        }
        return 0;
    }

    protected static class AndroidGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener
    {
        protected AndroidApplication androidApplication;
        protected int height;

        protected AndroidGlobalLayoutListener(AndroidApplication androidApplication)
        {
            this.androidApplication = androidApplication;
        }

        @Override
        public void onGlobalLayout()
        {
            height = 0;
            Window window = androidApplication.getWindow();
            if (window != null)
            {
                View currentFocus = window.getCurrentFocus();
                if (currentFocus != null)
                {
                    View rootView = currentFocus.getRootView();
                    if (rootView != null)
                    {
                        Rect rect = new Rect();
                        rootView.getWindowVisibleDisplayFrame(rect);
                        height = rect.bottom;
                    }
                }
            }
        }

        public int getHeight()
        {
            return height;
        }
    }
}
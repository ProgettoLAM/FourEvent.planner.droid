package lam.project.foureventplannerdroid.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by Vale on 26/08/2016.
 */

public class CustomNumberPicker extends NumberPicker {

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        if(child instanceof EditText) {
            ((EditText) child).setTextSize(25);
        }
    }
}

package com.govnokoder.velotracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class LineColorDialog extends Preference {
    private static final int DEFAULT_MIN_PROGRESS = 0;
    private static final int DEFAULT_MAX_PROGRESS = 255;
    private static final int DEFAULT_PROGRESS = 0;

    private int mMinProgress;
    private int mMaxProgress;
    private int mProgress;
    private CharSequence mProgressTextSuffix;
    private TextView mProgressText;
    private SeekBar mSeekBar;

    public LineColorDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public LineColorDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LineColorDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineColorDialog(Context context) {
        super(context);
    }
}

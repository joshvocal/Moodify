package me.joshvocal.moodify;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by josh on 8/15/17.
 */

public class PrefManager {

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    Context mContext;

    // Shared Preferences keys
    private static final String PREFERENCE_KEY = "Moodify";
    private static final String IS_FIRST_TIME_LAUNCH_KEY = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void setIsFirstTimeLaunch(boolean isFirstTimeLaunch) {
        mEditor.putBoolean(IS_FIRST_TIME_LAUNCH_KEY, isFirstTimeLaunch);
        mEditor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return mSharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH_KEY, true);
    }
}

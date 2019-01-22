package io.awesdroid.awesauth.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * @author awesdroid
 */
public class ActivityHelper {
    private static final String TAG = "ActivityHelper";
    private static ActivityHelper instance;
    private WeakReference<Activity> activity;

    public static ActivityHelper getInstance() {
        if (instance == null) {
            synchronized (ActivityHelper.class) {
                instance = new ActivityHelper();
            }
        }
        return instance;
    }

    public static Activity getActivity() {
        return ActivityHelper.getInstance().activity();
    }

    public static Context getContext() {
        return ActivityHelper.getInstance().activity.get();
    }

    public void setActivity(Activity activity) {
        Log.d(TAG, "setActivity(): " + activity);
        this.activity = new WeakReference<>(activity);
    }

    public void clear() {
        activity.clear();
        activity = null;
        instance = null;
        Log.d(TAG, "clear(): ");
    }

    private Activity activity() {
        Log.d(TAG, "activity():  " + activity.get());
        return activity.get();
    }




}

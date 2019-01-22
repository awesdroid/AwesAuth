package io.awesdroid.awesauth.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.awesdroid.awesauth.utils.ActivityHelper;
import io.awesdroid.awesauth.R;

import static io.awesdroid.awesauth.R.string.pref_key_google_auth_type;
import static io.awesdroid.awesauth.R.string.pref_key_use_pending_intent;

/**
 * @auther Awesdroid
 */
final public class SettingsRepository {
    private static final String TAG = SettingsRepository.class.getSimpleName();
    private Context context;
    // Live Data
    private MutableLiveData<String> authType;
    private MutableLiveData<String> authTypeName;
    private MutableLiveData<Boolean> appAuthUsePendingInent;
    private MutableLiveData<Boolean> googleSinInUseIdToken;

    private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "preferenceListener: key = " + key);
            if(key.equals(ActivityHelper.getContext().getResources()
                    .getString(pref_key_google_auth_type))) {
                    authType.postValue(sharedPreferences.getString(key, "-1"));
            } else if (key.equals(ActivityHelper.getContext().getResources()
                    .getString(pref_key_use_pending_intent))) {
                appAuthUsePendingInent.postValue(sharedPreferences.getBoolean(key, false));
            } else if (key.equals(ActivityHelper.getContext().getResources()
                    .getString(R.string.pref_key_use_id_token))) {
                googleSinInUseIdToken.postValue(sharedPreferences.getBoolean(key, false));
            } else {
                Log.d(TAG, "onSharedPreferenceChanged(): Uncared key" + key);
            }
        }
    };

    public SettingsRepository(Context context) {
        Log.d(TAG, "SettingsRepository(): this = " + this);
        this.context = context;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        authType = new MutableLiveData<>(sharedPreferences.getString(
                context.getResources().getString(R.string.pref_key_google_auth_type), "-1"));
        authTypeName = new MutableLiveData<>("");
        appAuthUsePendingInent = new MutableLiveData<>(sharedPreferences.getBoolean(
                context.getResources().getString(R.string.pref_key_use_pending_intent), false));
        googleSinInUseIdToken = new MutableLiveData<>(sharedPreferences.getBoolean(
                context.getResources().getString(R.string.pref_key_use_id_token), false));

        PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(listener);
    }

    public LiveData<String> getAuthType() {
        return authType;
    }

    public LiveData<String> getAuthTypeName() {
        return authTypeName;
    }

    public void setAuthTypeName(String name) {
        authTypeName.setValue(name);
    }

    public LiveData<Boolean> isAppAuthUsePendingIntent() {
        return appAuthUsePendingInent;
    }

    public LiveData<Boolean> isGoogleSignInUseIdToken() {
        return googleSinInUseIdToken;
    }

    public void destroy() {
        PreferenceManager.getDefaultSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(listener);
        context = null;
    }
}

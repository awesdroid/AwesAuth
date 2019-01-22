package io.awesdroid.awesauth.db.converter;

import android.util.Log;

import androidx.room.TypeConverter;
import io.awesdroid.awesauth.model.AppAuthConfig;

/**
 * @auther Awesdroid
 */
public final class ConfigConverter {
    private static final String TAG = "ConfigConverter";

    @TypeConverter
    public AppAuthConfig toConfig(String str) {
        Log.d(TAG, "toConfig(): str = " + str);
        return AppAuthConfig.create(str);
    }

    @TypeConverter
    public String fromConfig(AppAuthConfig config) {
        Log.d(TAG, "fromConfig(): appAuthConfig = " + config);
        return config.toString();
    }

}

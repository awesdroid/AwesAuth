package io.awesdroid.awesauth.db.converter;

import android.util.Log;

import net.openid.appauth.AuthState;

import org.json.JSONException;

import androidx.room.TypeConverter;
import io.awesdroid.awesauth.model.AppAuthState;

/**
 * @auther Awesdroid
 */
public final class StateConverter {
    private static final String TAG = "Converters";
    @TypeConverter
    public AppAuthState toState(String str) {
        Log.d(TAG, "toState(): str = " + str);
        try {
            return new AppAuthState(AuthState.jsonDeserialize(str));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TypeConverter
    public String fromState(AppAuthState state) {
        Log.d(TAG, "fromState(): appAuthState = " + state);
        return state.getAuthState().jsonSerializeString();
    }
}

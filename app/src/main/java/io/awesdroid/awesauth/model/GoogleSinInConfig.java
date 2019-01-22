package io.awesdroid.awesauth.model;

import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;

/**
 * @auther Awesdroid
 */
public class GoogleSinInConfig {


    private String client_id;

    public String getClientId() {
        return client_id;
    }

    @NonNull
    @Override
    public String toString() {
        return new GsonBuilder()
                .create()
                .toJson(this);
    }
}

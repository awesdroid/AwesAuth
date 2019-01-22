package io.awesdroid.awesauth.model;

import com.google.gson.GsonBuilder;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * @auther Awesdroid
 */
public class AppAuthConfig {

    private String client_id;
    private String authorization_scope;
    private Uri redirect_uri;
    private Uri discovery_uri;
    private Uri authorization_endpoint_uri;
    private Uri token_endpoint_uri;
    private Uri registration_endpoint_uri;
    private Uri user_info_endpoint_uri;
    private boolean https_required;


    public String getClientId() {
        return client_id;
    }

    public String getScope() {
        return authorization_scope;
    }


    public Uri getRedirectUri() {
        return redirect_uri;
    }

    public Uri getDiscoveryUri() {
        return discovery_uri;
    }

    public Uri getAuthEndpointUri() {
        return authorization_endpoint_uri;
    }

    public Uri getTokenEndpointUri() {
        return token_endpoint_uri;
    }

    public Uri getRegistrationEndpointUri() {
        return registration_endpoint_uri;
    }

    public Uri getUserInfoEndpointUri() {
        return user_info_endpoint_uri;
    }

    public boolean isHttpsRequired() {
        return https_required;
    }

    AppAuthConfig() {
    }

    @NonNull
    @Override
    public String toString() {
        return new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriAdapter())
                .create()
                .toJson(this);
    }

    public static AppAuthConfig create(String str) {
        return new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriAdapter())
                .create()
                .fromJson(str, AppAuthConfig.class);
    }
}

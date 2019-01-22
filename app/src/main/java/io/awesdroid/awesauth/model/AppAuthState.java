package io.awesdroid.awesauth.model;

import net.openid.appauth.AuthState;

import androidx.annotation.NonNull;
import io.awesdroid.awesauth.utils.Utils;

/**
 * @auther Awesdroid
 */
public class AppAuthState {
    private AuthState authState;

    public AppAuthState(AuthState authState) {
        this.authState = authState;
    }

    public AuthState getAuthState() {
        return authState;
    }

    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }

    public String getAuthorizationCode() {
        if (authState != null && authState.getLastAuthorizationResponse() != null)
            return authState.getLastAuthorizationResponse().authorizationCode;
        return null;
    }

    public boolean hasLastTokenResponse() {
        return authState != null && authState.getLastTokenResponse() != null;
    }

    public String getRefreshToken() {
        if (authState == null)
            return null;
        return authState.getRefreshToken();
    }

    public String getIdToken() {
        if (authState == null)
            return null;
        return authState.getIdToken();
    }

    public String getAccessToken() {
        if (authState == null)
            return null;
        return authState.getAccessToken();
    }

    public Long getAccessTokenExpirationTime() {
        if (authState == null)
            return null;
        return authState.getAccessTokenExpirationTime();
    }


    public Exception getAuthorizationException() {
        return authState.getAuthorizationException();
    }

    @NonNull
    @Override
    public String toString() {
        return Utils.prettyJson(authState.jsonSerializeString());
    }
}
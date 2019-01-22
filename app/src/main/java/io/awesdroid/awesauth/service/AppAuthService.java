package io.awesdroid.awesauth.service;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.browser.AnyBrowserMatcher;
import net.openid.appauth.connectivity.DefaultConnectionBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import io.awesdroid.awesauth.model.AppAuthConfig;
import io.awesdroid.awesauth.net.Http;
import io.awesdroid.awesauth.utils.ActivityHelper;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * @auther Awesdroid
 */
public class AppAuthService {
    private static final String TAG = AppAuthService.class.getSimpleName();
    private Context context;
    private Activity completeActivity;
    private Activity cancelActivity;
    private boolean isInit = false;
    private AppAuthConfig appAuthConfig;
    private AuthorizationService authService;
    private AuthorizationRequest authRequest;
    private CustomTabsIntent authIntent;
    private CompletableFuture<Boolean> isWarmUpBrowser;
    private AuthState authState;

    public void init(Context context, Activity completeActivity, Activity cancelActivity) {
        this.init(context, completeActivity, cancelActivity, null, null);
    }

    public void init(Context context, Activity completeActivity, Activity cancelActivity,
                      AppAuthConfig config, AuthState initState) {
        if (isInit) return;
        if (config == null)
            throw new IllegalArgumentException("AppAuthConfig is null");

        this.context = context;
        this.completeActivity = completeActivity;
        this.cancelActivity = cancelActivity;
        this.appAuthConfig = config;
        this.authState = initState;

        if (authState != null && authState.getAuthorizationServiceConfiguration() != null) {
            resumeAppAuth();
        } else {
            initAppAuth(appAuthConfig);
        }

        isInit = true;
    }

    private void initAppAuth(AppAuthConfig appAuthConfig) {
        this.appAuthConfig = appAuthConfig;
        Uri discoveryUri = appAuthConfig.getDiscoveryUri();
        if (discoveryUri != null && !discoveryUri.toString().isEmpty()) {
            Single.just(discoveryUri)
                    .subscribeOn(Schedulers.io())
                    .subscribe(uri -> AuthorizationServiceConfiguration
                            .fetchFromUrl(
                                    uri,
                                    this::handleConfigFetchResult,
                                    DefaultConnectionBuilder.INSTANCE));
        } else {
            Single.just(new AuthorizationServiceConfiguration(
                                appAuthConfig.getAuthEndpointUri(),
                                appAuthConfig.getTokenEndpointUri()))
                    .subscribe(configuration -> handleConfigFetchResult(configuration, null));

        }
    }

    private void handleConfigFetchResult(
            AuthorizationServiceConfiguration config,
            AuthorizationException ex) {
        if (config == null) {
            Log.i(TAG, "Failed to retrieve discovery document", ex);
            return;
        }

        authState = new AuthState(config);
        Log.i(TAG, "Fetched discovery configuration is " + config.toJsonString());
        resumeAppAuth();
    }

    private void initClient() {
        if (appAuthConfig.getClientId() == null || appAuthConfig.getClientId().isEmpty()) {
            Log.w(TAG, "initClient: dynamic registeration is not supported yet");
            return;
        }

        createAuthRequest(null);
        isWarmUpBrowser = warmUpBrowser();
    }

    private void createAuthRequest(@Nullable String loginHint) {
        Log.i(TAG, "Creating auth request for login hint: " + loginHint);
        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                authState.getAuthorizationServiceConfiguration(),
                appAuthConfig.getClientId(),
                ResponseTypeValues.CODE,
                appAuthConfig.getRedirectUri())
                .setScope(appAuthConfig.getScope());

        if (!TextUtils.isEmpty(loginHint)) {
            authRequestBuilder.setLoginHint(loginHint);
        }

        authRequest = authRequestBuilder.build();
    }

    private CompletableFuture<Boolean> warmUpBrowser() {
        return CompletableFuture.supplyAsync(() -> {
            CustomTabsIntent.Builder intentBuilder =
                    authService.createCustomTabsIntentBuilder(authRequest.toUri());
            authIntent = intentBuilder.build();
            return true;
        });
    }

    private void recreateAuthorizationService() {
        Log.d(TAG, "recreateAuthorizationService: authService = " + authService);
        if (authService != null) {
            Log.i(TAG, "Discarding existing AuthService instance");
            authService.dispose();
        }
        authService = createAuthorizationService();
        authRequest = null;
        authIntent = null;
    }

    private void resumeAppAuth() {
        recreateAuthorizationService();
        initClient();
    }

    private AuthorizationService createAuthorizationService() {
        Log.i(TAG, "createAuthorizationService(): ");
        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        builder.setBrowserMatcher(AnyBrowserMatcher.INSTANCE);
        builder.setConnectionBuilder(DefaultConnectionBuilder.INSTANCE);

        return new AuthorizationService(ActivityHelper.getContext(), builder.build());
    }

    public void doAuth(boolean usePendingIntent, int requestCode) {
        isWarmUpBrowser.thenApplyAsync(ret -> {
            if (usePendingIntent) {
                Log.d(TAG, "doAuth(): usePendingIntent");
                Intent completionIntent = new Intent(context, completeActivity.getClass());
                Intent cancelIntent = new Intent(context, cancelActivity.getClass());
                cancelIntent.putExtra("failed", true);
                cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                authService.performAuthorizationRequest(
                        authRequest,
                        PendingIntent.getActivity(context, 0, completionIntent, 0),
                        PendingIntent.getActivity(context, 0, cancelIntent, 0),
                        authIntent);
            } else {
                Log.d(TAG, "doAuth(): NOT usePendingIntent");
                Intent intent = authService.getAuthorizationRequestIntent(authRequest, authIntent);
                ActivityHelper.getActivity().startActivityForResult(intent, requestCode);
            }
            return true;
        });
    }

    public Single<AuthState> handleAuthResponse(Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException exception = AuthorizationException.fromIntent(intent);
        if (response == null || exception != null) {
            return Single.create(emitter -> {
                    if (exception != null) {
                        emitter.onError(exception);
                    } else {
                        emitter.onError(new Throwable("Response is NULL!"));
                    }
                }
            );
        }
        authState.update(response, null);
        return Single.create(source ->
                performTokenRequest(response.createTokenExchangeRequest(), (r, e) -> {
                    if (authState != null) {
                        authState.update(r, e);
                        if (!authState.isAuthorized()) {
                            final String message = "Authorization Code exchange failed: "
                                    + ((e != null) ? e.getMessage() : "");
                            Log.w(TAG, message);
                            source.onError(new IllegalAccessException(message));
                        } else {
                            Log.d(TAG, "handleCodeExchan`geResponse(): tokenResponse = " + r);
                            source.onSuccess(authState);
                        }
                    } else {
                        source.onError(new IllegalStateException("Current State is null and can't be updated"));
                    }
                }));
    }


    private void performTokenRequest(
            TokenRequest request,
            AuthorizationService.TokenResponseCallback callback) {
        ClientAuthentication clientAuthentication;
        try {
            clientAuthentication = authState.getClientAuthentication();
        } catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
            Log.d(TAG, "Token request cannot be made, client authentication for the token "
                    + "endpoint could not be constructed (%s)", ex);
            return;
        }

        authService.performTokenRequest(
                request,
                clientAuthentication,
                callback);
    }

    public Maybe<JSONObject> fetchUserInfo() {
        return Maybe.create(source ->
                authState.performActionWithFreshTokens(authService, (accessToken, idToken, ex) -> {
                    if (ex != null) {
                        Log.e(TAG, "Token refresh failed when fetching user info");
                        source.onError(ex);
                        return;
                    }

                    AuthorizationServiceDiscovery discovery =
                            authState.getAuthorizationServiceConfiguration().discoveryDoc;
                    String userInfoEndpoint = appAuthConfig.getUserInfoEndpointUri() != null
                            ? appAuthConfig.getUserInfoEndpointUri().toString()
                            : discovery.getUserinfoEndpoint().toString();

                    Http.getUserInfo(userInfoEndpoint, accessToken)
                            .subscribeOn(Schedulers.io())
                            .subscribe(info -> {
                                Log.d(TAG, "fetchUserInfo(): info = " + info);
                                if (info.isEmpty())
                                    source.onComplete();
                                else {
                                    try {
                                        source.onSuccess(new JSONObject(info));
                                    } catch (JSONException e) {
                                        Log.w(TAG, "fetchUserInfo(): JSONException: ", e);
                                        source.onError(e);
                                    }
                                }
                            });
                }));
    }

    public Single<AuthState> refreshAccessToken() {
        return Single.create(source -> performTokenRequest(authState.createTokenRefreshRequest(),
                (tokenResponse, exception) -> {
            if (exception != null) {
                authState.update(tokenResponse, exception);
                source.onError(exception);
            } else {
                authState.update(tokenResponse, null);
                source.onSuccess(authState);
            }
        }));

    }

    public Single<AuthState> signOut() {
        return Single.create(emitter -> {
            if (authState == null) {
                emitter.onError(new IllegalArgumentException("authState is Null"));
            } else if (authState.getAuthorizationServiceConfiguration() == null) {
                emitter.onError(new IllegalArgumentException("authState has empty configuration"));
            } else {
                authState = new AuthState(authState.getAuthorizationServiceConfiguration());
                emitter.onSuccess(authState);
            }
        });
    }


    public void destroy() {
        Log.d(TAG, "destroy(): authService = " + authService);
        if (authService != null) {
            authService.dispose();
        }
        authService = null;
        context = null;
        completeActivity = null;
        cancelActivity = null;
        isInit = false;
    }

}

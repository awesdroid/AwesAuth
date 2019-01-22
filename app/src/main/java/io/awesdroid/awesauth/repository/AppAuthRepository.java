package io.awesdroid.awesauth.repository;

import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import net.openid.appauth.AuthState;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.db.AuthDatabase;
import io.awesdroid.awesauth.db.entity.ConfigEntity;
import io.awesdroid.awesauth.db.entity.StateEntity;
import io.awesdroid.awesauth.model.AppAuthConfig;
import io.awesdroid.awesauth.model.AppAuthState;
import io.awesdroid.awesauth.model.UriAdapter;
import io.awesdroid.awesauth.service.AppAuthService;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @auther Awesdroid
 */
public class AppAuthRepository {
    private static final String TAG = AppAuthRepository.class.getSimpleName();
    private AppAuthService appAuthService;
    private Context context;
    private Disposable fetchUserInfoSubscription = null;

    // Live Data
    private MutableLiveData<AppAuthState> appAuthState;
    private MutableLiveData<JSONObject> userInfo;
    private MutableLiveData<Exception> error;

    public AppAuthRepository(Context context) {
        this.context = context;

        appAuthState = new MutableLiveData<>(null);
        userInfo = new MutableLiveData<>(null);
    }

    public void init(Activity completeActivity, Activity cancelActivity) {
        appAuthService = new AppAuthService();
        getAppAuthConfig().subscribe(config -> {
            AuthState authState = null;
            StateEntity saved = AuthDatabase.getInstance().stateDao().loadAppAuthState(1);
            if (saved != null) {
                Log.d(TAG, "init(): load saved state = " + saved.getAppAuthState());
                authState = saved.getAppAuthState().getAuthState();
                appAuthState.postValue(saved.getAppAuthState());
            }
            appAuthService.init(context, completeActivity, cancelActivity, config, authState);
        });
    }

    public LiveData<AppAuthState> getAppAuthState() {
        return appAuthState;
    }

    public LiveData<JSONObject> getUserInfo() {
        return userInfo;
    }
    
    public LiveData<Exception> getError() {
        return error;
    }

    public void handleAuthResponse(Intent intent) {
            appAuthService.handleAuthResponse(intent)
                    .observeOn(Schedulers.io())
                    .subscribe(this::updateState, this::handleError);
    }

    public void signIn(boolean usePendingIntent, int requestCode) {
        CompletableFuture.runAsync(() -> appAuthService.doAuth(usePendingIntent, requestCode));
    }

    public void fetchUserInfo() {
        appAuthService.fetchUserInfo()
                .observeOn(Schedulers.trampoline())
                .subscribe(info -> userInfo.postValue(info),
                        this::handleError,
                        () -> userInfo.postValue(null));
    }

    public void refreshToken() {
        appAuthService.refreshAccessToken()
                .observeOn(Schedulers.io())
                .subscribe(this::updateState, this::handleError);
    }

    public void signOut() {
        appAuthService.signOut()
                .observeOn(Schedulers.single())
                .subscribe(this::updateState);
    }

    public void destroy() {
        Log.d(TAG, "destroy()");
        AuthDatabase.getInstance().destroy();
        appAuthService.destroy();
        appAuthService = null;
        if (fetchUserInfoSubscription != null && !fetchUserInfoSubscription.isDisposed()) {
            Log.d(TAG, "destroy(): fetchUserInfoSubscription = " + fetchUserInfoSubscription);
            fetchUserInfoSubscription.dispose();
        }
        fetchUserInfoSubscription = null;
        context = null;
    }

    private Single<AppAuthConfig> getAppAuthConfig() {
        return Single.defer(() -> {
            ConfigEntity configEntity = (AuthDatabase.getInstance().configDao().loadConfig(1));
            if (configEntity == null) {
                Log.d(TAG, "getAppAuthConfig(): read raw config");
                return readConfig()
                        .map(config -> {
                            AuthDatabase.getInstance().configDao()
                                    .insertConfig(new ConfigEntity(1, config));
                            return config;
                        });
            }
            return Single.just(configEntity.getAppAuthConfig());
        })
        .subscribeOn(Schedulers.io());
    }

    private Single<AppAuthConfig> readConfig() {
        return Single.just(R.raw.google_config)
                .map(id -> context.getResources().openRawResource(id))
                .map(s -> new InputStreamReader(s, StandardCharsets.UTF_8))
                .map(isr -> new GsonBuilder()
                        .registerTypeAdapter(Uri.class, new UriAdapter())
                        .create()
                        .fromJson(isr, AppAuthConfig.class))
                .subscribeOn(Schedulers.io());
    }

    private void updateState(AuthState authState) {
        AppAuthState current = appAuthState.getValue();
        if (current != null) {
            current.setAuthState(authState);
        } else {
            current = new AppAuthState(authState);
        }
        updateStateDb(current);
        appAuthState.postValue(current);
        Log.d(TAG, "updateState(): authState = " + authState);
    }

    private void updateStateDb(AppAuthState appAuthState) {
        CompletableFuture.runAsync(() -> AuthDatabase.getInstance()
                .stateDao()
                .insertAppAuthState(new StateEntity(1, appAuthState)));
    }

    private void handleError(Throwable e) {
        // TODO
        e.printStackTrace();
    }
}

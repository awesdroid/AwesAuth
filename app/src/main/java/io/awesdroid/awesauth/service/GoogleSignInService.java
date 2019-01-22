package io.awesdroid.awesauth.service;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.content.Intent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.model.GoogleSinInConfig;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * @auther Awesdroid
 */
public class GoogleSignInService {
    private Activity activity;
    private GoogleSignInClient googleSignInClient = null;
    private GoogleSinInConfig googleSinInConfig = null;

    public void init(Activity activity) {
        this.activity = activity;
        initConfig();
    }

    public void doAuth(boolean isGoogleSignInUseIdToken, int responseCode) {
        if (isGoogleSignInUseIdToken) {
            GoogleSignInOptions gso =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(googleSinInConfig.getClientId())
                            .requestEmail()
                            .build();
            googleSignInClient = GoogleSignIn.getClient(activity, gso);
        } else {
            GoogleSignInOptions gso =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();
            googleSignInClient = GoogleSignIn.getClient(activity, gso);
        }
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, responseCode);
    }

    public Completable signOut() {
        return Completable.create(v -> googleSignInClient.signOut()
                .addOnCompleteListener(task -> v.onComplete()));
    }

    public Observable<GoogleSignInAccount> refreshToken() {
        return Observable.create(v -> googleSignInClient.silentSignIn()
                .addOnCompleteListener(task -> {
                    v.onNext(task.getResult());
                    v.onComplete();
                }));

    }

    public void destroy() {
        activity = null;
        googleSignInClient = null;
    }

    private void initConfig() {
        if (googleSinInConfig == null) {
            Single.just(R.raw.google_signin)
                .observeOn(Schedulers.io())
                .map(id -> activity.getResources().openRawResource(id))
                .map(s -> new InputStreamReader(s, StandardCharsets.UTF_8))
                .map(isr -> new GsonBuilder()
                        .create()
                        .fromJson(isr, GoogleSinInConfig.class))
                .subscribe(config -> googleSinInConfig = config);
        }
    }
}

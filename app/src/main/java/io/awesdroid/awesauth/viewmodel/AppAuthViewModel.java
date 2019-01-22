package io.awesdroid.awesauth.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import io.awesdroid.awesauth.di.DaggerRepositoryComponent;
import io.awesdroid.awesauth.model.AppAuthState;
import io.awesdroid.awesauth.repository.AppAuthRepository;

/**
 * @auther Awesdroid
 */
public class AppAuthViewModel extends AndroidViewModel {
    private static final String TAG = AppAuthViewModel.class.getSimpleName();
    @Inject AppAuthRepository repository;
    private MediatorLiveData<AppAuthState> authState = new MediatorLiveData<>();
    private MediatorLiveData<JSONObject> userInfo = new MediatorLiveData<>();

    public AppAuthViewModel(@NonNull Application application) {
        super(application);
        DaggerRepositoryComponent.create().inject(this);
        authState.addSource(repository.getAppAuthState(), v -> authState.postValue(v));
        userInfo.addSource(repository.getUserInfo(), v -> userInfo.postValue(v));
    }

    public void init(Activity completeActivity, Activity cancelActivity) {
        repository.init(completeActivity, cancelActivity);
    }

    public void handleAuthResponse(Intent intent) {
        repository.handleAuthResponse(intent);
    }

    public void signIn(boolean usePendingIntent, int requestCode) {
        repository.signIn(usePendingIntent, requestCode);
    }

    public void fetchUserInfo() {
        repository.fetchUserInfo();
    }

    public void refreshToken() {
        repository.refreshToken();
    }

    public void signOut() {
        repository.signOut();
    }

    public LiveData<AppAuthState> getAuthState() {
        return authState;
    }

    public LiveData<JSONObject> getUserInfo() {
        return userInfo;
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "onCleared: ");
        if (repository != null)
            repository.destroy();

        repository = null;
        super.onCleared();
    }
}

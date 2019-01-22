package io.awesdroid.awesauth.viewmodel;

import android.app.Application;
import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import io.awesdroid.awesauth.di.DaggerRepositoryComponent;
import io.awesdroid.awesauth.repository.SettingsRepository;

/**
 * @auther Awesdroid
 */
final public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = SettingsViewModel.class.getSimpleName();
    @Inject SettingsRepository repository;
    private LiveData<String> authType;
    private MediatorLiveData<String> authTypeName;
    private LiveData<Boolean> appAuthUsePendingInent;
    private LiveData<Boolean> googleSinInUseIdToken;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        DaggerRepositoryComponent.create().inject(this);
        authType = repository.getAuthType();

        authTypeName = new MediatorLiveData<>();
        authTypeName.addSource(repository.getAuthTypeName(), v -> {
            Log.d(TAG, "SettingsViewModel(): v = " + v);
            authTypeName.setValue(v);
        });
        appAuthUsePendingInent = repository.isAppAuthUsePendingIntent();
        googleSinInUseIdToken = repository.isGoogleSignInUseIdToken();
    }

    public LiveData<String> getAuthType() {
        return authType;
    }

    public LiveData<String> getAuthTypeName() {
        return authTypeName;
    }

    public void setAuthTypeName(String name) {
        repository.setAuthTypeName(name);
    }

    public LiveData<Boolean> isAppAuthUsePendingIntent() {
        return appAuthUsePendingInent;
    }

    public LiveData<Boolean> isGoogleSignInUseIdToken() {
        return googleSinInUseIdToken;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (repository != null)
            repository.destroy();

        repository = null;
        Log.d(TAG, "onCleared: ");
    }
}

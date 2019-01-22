package io.awesdroid.awesauth.viewmodel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.awesdroid.awesauth.utils.ActivityHelper;
import io.awesdroid.awesauth.service.GoogleSignInService;
import io.reactivex.schedulers.Schedulers;

/**
 * @auther Awesdroid
 */
final public class GoogleSignInViewModel extends AndroidViewModel {
    private static final String TAG = GoogleSignInViewModel.class.getSimpleName();
    private MutableLiveData<GoogleSignInAccount> account;
    private GoogleSignInService googleSignInService;

    public GoogleSignInViewModel(@NonNull Application application) {
        super(application);
        account = new MutableLiveData<>(null);
        googleSignInService = new GoogleSignInService();
        googleSignInService.init(ActivityHelper.getActivity());
    }

    public void signIn(boolean useIdToken, int responseCode) {
        googleSignInService.doAuth(useIdToken, responseCode);
    }

    public void signOut() {
        googleSignInService.signOut()
                .observeOn(Schedulers.io())
                .subscribe(() -> account.postValue(null));
    }

    public void refreshToken() {
        googleSignInService.refreshToken()
                .observeOn(Schedulers.io())
                .subscribe(v -> account.postValue(v));
    }

    public LiveData<GoogleSignInAccount> getAccount() {
        return account;
    }

    @Override
    protected void onCleared() {
        googleSignInService.destroy();
        super.onCleared();
        Log.d(TAG, "onCleared: ");
    }
}

package io.awesdroid.awesauth.ui;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.di.DaggerFragmentComponent;
import io.awesdroid.awesauth.utils.ActivityHelper;
import io.awesdroid.awesauth.viewmodel.AppAuthViewModel;
import io.awesdroid.awesauth.viewmodel.SettingsViewModel;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int RC_AUTH = 100;
    public static final int RC_SIGN_IN = 200;

    @BindView(R.id.toolbar_text)
    TextView toolBarText;

    Unbinder unbinder;

    @Named("AppAuthFragment")
    @Inject Fragment appAuthFragment;
    @Named("SettingsFragment")
    @Inject Fragment settingsFragment;
    @Named("GoogleSignInFragment")
    @Inject Fragment GoogleSignInFragment;
    private Fragment currentAuthFragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private int currentFragment = 0;
    private SettingsViewModel settingsViewModel;
    private String authTypeName;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                currentFragment = item.getItemId();
                switch (item.getItemId()) {
                    case R.id.navigation_auth:
                        toolBarText.setText(authTypeName);
                        String type = settingsViewModel.getAuthType().getValue();
                        showAuthFragment(type == null? "0" : type);
                        return true;
                    case R.id.navigation_settings:
                        toolBarText.setText(R.string.title_settings);
                        fragmentManager.beginTransaction().hide(currentAuthFragment).show(settingsFragment).commit();
                        return true;
                }
                return false;
            };


    public void navigateToSettings() {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_settings);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        ActivityHelper.getInstance().setActivity(this);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        DaggerFragmentComponent.create().inject(this);
        currentAuthFragment = appAuthFragment;

        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        Log.d(TAG, "onCreate(): settingsViewModel" + settingsViewModel + ", name = " + settingsViewModel.getAuthTypeName().getValue());
        settingsViewModel.getAuthTypeName().observe(this, this::setTitle);

        fragmentManager.beginTransaction()
                .add(R.id.content, settingsFragment, "3")
                .add(R.id.content, appAuthFragment, "1")
                .add(R.id.content, GoogleSignInFragment, "2")
                .commit();
        showAuthFragment(settingsViewModel.getAuthType().getValue());
    }

    private void showAuthFragment(String type) {
        if (type != null && type.equals("0")) {
            fragmentManager.beginTransaction()
                    .hide(settingsFragment)
                    .hide(GoogleSignInFragment)
                    .show(appAuthFragment)
                    .commit();
            currentAuthFragment = appAuthFragment;
        } else {
            fragmentManager.beginTransaction()
                    .hide(settingsFragment)
                    .hide(appAuthFragment)
                    .show(GoogleSignInFragment)
                    .commit();
            currentAuthFragment = GoogleSignInFragment;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent(): " + intent);
        setIntent(intent);
        handleAuthResponse(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult(): requestCode = " + requestCode + ", data = " + data);
        if (requestCode == RC_AUTH) {
            handleAuthResponse(data);
        } else if (requestCode == RC_SIGN_IN) {
            GoogleSignInFragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onDestroy() {
        if (unbinder != null)
            unbinder.unbind();
        unbinder = null;
        ActivityHelper.getInstance().clear();
        super.onDestroy();
    }

    private void setTitle(String name) {
        authTypeName = name;
        if (currentFragment != R.id.navigation_settings)
            toolBarText.setText(name);
    }

    private void handleAuthResponse(Intent intent) {
        if (intent!= null) {
            ViewModelProviders.of(this).get(AppAuthViewModel.class)
                    .handleAuthResponse(intent);
        }
    }
}

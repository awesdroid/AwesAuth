package io.awesdroid.awesauth.di;

import javax.inject.Named;
import javax.inject.Singleton;

import androidx.fragment.app.Fragment;
import dagger.Module;
import dagger.Provides;
import io.awesdroid.awesauth.ui.AppAuthFragment;
import io.awesdroid.awesauth.ui.GoogleSignInFragment;
import io.awesdroid.awesauth.ui.SettingsFragment;

/**
 * @author Awesdroid
 */
@Module
final class FragmentModule {
    @Named("AppAuthFragment")
    @Singleton
    @Provides
    Fragment AppAuthFragment() {
        return new AppAuthFragment();
    }

    @Named("GoogleSignInFragment")
    @Singleton
    @Provides
    Fragment GoogleSignInFragment() { return new GoogleSignInFragment();
    }

    @Named("SettingsFragment")
    @Singleton
    @Provides
    Fragment SettingsFragment() { return new SettingsFragment();
    }
}

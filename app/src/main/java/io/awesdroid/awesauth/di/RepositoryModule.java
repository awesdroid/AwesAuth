package io.awesdroid.awesauth.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.awesdroid.awesauth.repository.AppAuthRepository;
import io.awesdroid.awesauth.repository.SettingsRepository;

/**
 * @author Awesdroid
 */
@Module
public class RepositoryModule {
    @Singleton
    @Provides
    AppAuthRepository AppAuthRepository(Context context) {
        return new AppAuthRepository(context);
    }

    @Singleton
    @Provides
    SettingsRepository SettingsRepository(Context context) {
        return new SettingsRepository(context);
    }
}

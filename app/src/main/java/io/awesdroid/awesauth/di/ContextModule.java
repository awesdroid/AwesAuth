package io.awesdroid.awesauth.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.awesdroid.awesauth.utils.ActivityHelper;

/**
 * @auther Awesdroid
 */
@Module
class ContextModule {
    @Provides
    Context provideContext() {
       return ActivityHelper.getContext();
    }
}

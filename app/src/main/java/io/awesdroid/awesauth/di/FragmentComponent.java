package io.awesdroid.awesauth.di;

import javax.inject.Singleton;

import dagger.Component;
import io.awesdroid.awesauth.ui.MainActivity;

/**
 * @author Awesdroid
 */
@Singleton
@Component(modules = {FragmentModule.class})
public interface FragmentComponent {
    void inject(MainActivity activity);
}

package io.awesdroid.awesauth.di;

import javax.inject.Singleton;

import dagger.Component;
import io.awesdroid.awesauth.viewmodel.AppAuthViewModel;
import io.awesdroid.awesauth.viewmodel.SettingsViewModel;

/**
 * @author Awesdroid
 */
@Singleton
@Component(modules = {RepositoryModule.class, ContextModule.class})
public interface RepositoryComponent {
    void inject(AppAuthViewModel viewModel);
    void inject(SettingsViewModel viewModel);
}

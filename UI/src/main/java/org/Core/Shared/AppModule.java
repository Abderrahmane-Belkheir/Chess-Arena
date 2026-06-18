package org.Core.Shared;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.Core.Auth.AuthClient;
import org.Core.Auth.AuthService;
import org.Core.Auth.TokenStorage;
import org.Core.Auth.UserSessionManager;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthClient.class).in(Scopes.SINGLETON);
        bind(AuthService.class).in(Scopes.SINGLETON);
        bind(TokenStorage.class).in(Scopes.SINGLETON);
        bind(AppConfig.class).in(Scopes.SINGLETON);
        bind(ApiClient.class).in(Scopes.SINGLETON);
        bind(UserSessionManager.class).in(Scopes.SINGLETON);
    }
}

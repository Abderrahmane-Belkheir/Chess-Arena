package org.Core.Shared;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import javafx.application.HostServices;
import javafx.scene.layout.StackPane;
import org.Core.Auth.AuthClient;
import org.Core.Auth.AuthService;
import org.Core.Auth.TokenStorage;
import org.Core.Auth.UserSessionManager;
import org.Core.Game.Services.GameSessionService;
import org.Core.Realtime.RealtimeGateway;
import org.Core.Social.FriendShipClient;
import org.Core.UI.OpeningScreens.GameController;

public class AppModule extends AbstractModule {

    private final StackPane root;
    private final HostServices hostServices;

    public AppModule(StackPane root, HostServices hostServices) {
        this.root = root;
        this.hostServices = hostServices;
    }
    // registering business logic services
    @Override
    protected void configure() {
        bind(StackPane.class).toInstance(root);
        bind(HostServices.class).toInstance(hostServices);
        bind(GameController.class).in(Scopes.SINGLETON);
        bind(AuthClient.class).in(Scopes.SINGLETON);
        bind(AuthService.class).in(Scopes.SINGLETON);
        bind(TokenStorage.class).in(Scopes.SINGLETON);
        bind(AppConfig.class).in(Scopes.SINGLETON);
        bind(ApiClient.class).in(Scopes.SINGLETON);
        bind(UserSessionManager.class).in(Scopes.SINGLETON);
        bind(FriendShipClient.class).in(Scopes.SINGLETON);
        bind(RealtimeGateway.class).in(Scopes.SINGLETON);
        bind(AppEvents.class).in(Scopes.SINGLETON);
        bind(GameSessionService.class).in(Scopes.SINGLETON);
    }
}

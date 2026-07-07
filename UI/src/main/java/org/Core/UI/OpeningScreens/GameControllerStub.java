package org.Core.UI.OpeningScreens;

import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.Core.Auth.AuthService;

import org.Core.Auth.DTO.UserSession;
import org.Core.Auth.UserSessionManager;
import org.Core.Realtime.RealtimeGateway;
import org.Core.Social.FriendShipClient;
import org.Core.UI.Game.MatchmakingHandler;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;
import org.Core.UI.LobbyScreens.Lobby.LobbyControllerStub;
import org.Core.UI.Shared.ViewNavigator;

import java.util.concurrent.CompletableFuture;


public class GameControllerStub implements GameController {

    private final StackPane root;
    private final HostServices hostServices;
    private final LobbyController lobbyController;
    private final ViewNavigator viewNavigator;

    private final RealtimeGateway realtimeGateway;
    private final FriendShipClient friendShipClient;
    private final AuthService authService;
    private final UserSessionManager sessionManager;

    private UserSession userSession;

    @Inject
    public GameControllerStub(StackPane root, HostServices hostServices, AuthService authService,
                              UserSessionManager sessionManager, FriendShipClient friendShipClient,
                              RealtimeGateway realtimeGateway, ViewNavigator viewNavigator, MatchmakingHandler matchmakingHandler)
    {
        this.root = root;
        this.hostServices = hostServices;
        this.viewNavigator=viewNavigator;
        this.lobbyController=new LobbyControllerStub(root,viewNavigator,matchmakingHandler);
        this.authService=authService;
        this.sessionManager=sessionManager;
        this.friendShipClient=friendShipClient;
        this.realtimeGateway = realtimeGateway;
    }

    public void start() {
        showSplash();
    }


    private void showSplash() {
        SplashView splash = new SplashView(() -> {
            viewNavigator.transitionTo(showModeView());
        });

        root.getChildren().setAll(splash.getView());
    }



    private StackPane showModeView() {
        return new ModeView(
                this::handleOnline,
                this::handleOffline
        ).getView();
    }

    private void handleOffline() {
        System.out.println("Offline game started");
    }

    private void handleOnline() {

        LoadingView loading = new LoadingView();
        viewNavigator.transitionTo(loading.getView());

        CompletableFuture.runAsync(() -> {
            try {

                Platform.runLater(() -> loading.setMessage("Checking session...", 0));

                boolean authenticated = authService.isUserAuthenticated();

                if (authenticated) {

                    Platform.runLater(() -> loading.setMessage("Loading your profile...", 1));

                    UserSession userSession=sessionManager.getUserSession(true);

                    Platform.runLater(() -> loading.setMessage("Almost there...", 2));

                    realtimeGateway.connect().thenAccept(u -> {
                        Platform.runLater(() -> viewNavigator.transitionTo(lobbyController.start(userSession,friendShipClient)));
                        realtimeGateway.startLobbyPING();
                    }  );
                } else {

                    Thread.sleep(800);
                    Platform.runLater(()->loading.setMessage("No Session Found",0));
                    Thread.sleep(500);
                    Platform.runLater(() -> viewNavigator.transitionTo(showAuthView()));
                }

            } catch (Exception e) {
                Platform.runLater(() -> viewNavigator.transitionTo(showAuthView()));
            }
        });
    }

    private StackPane showAuthView() {

        AuthView view = new AuthView(() -> {
            try {
                CompletableFuture<Boolean> login = authService.callbackServer();
                String url = authService.redirect();
                hostServices.showDocument(url);

                login.thenAccept(ok -> {
                    if (ok) {
                        Platform.runLater(() -> {
                            LoadingView loading = new LoadingView();
                            viewNavigator.transitionTo(loading.getView());

                            CompletableFuture.runAsync(() -> {
                                try {
                                    Platform.runLater(() -> loading.setMessage("Loading your profile...", 1));

                                    this.userSession = sessionManager.getUserSession(true);

                                    Platform.runLater(() -> loading.setMessage("Almost there...", 2));


                                        realtimeGateway.connect().thenAccept(u -> {
                                            transitionToLobby();
                                        }  );

                                } catch (Exception e) {
                                    Platform.runLater(() -> viewNavigator.transitionTo(showAuthView()));
                                }
                            });
                        });
                    }else {
                        //TODO
                    }
                });;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return view.getView();
    }


    @Override
    public  void transitionToLobby(){
        Platform.runLater(() -> viewNavigator.transitionTo(lobbyController.start(userSession,friendShipClient)));
        realtimeGateway.startLobbyPING();
    }

}
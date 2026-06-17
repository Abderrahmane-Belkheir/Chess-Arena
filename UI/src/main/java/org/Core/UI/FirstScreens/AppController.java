package org.Core.UI.FirstScreens;

import javafx.animation.FadeTransition;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.Core.Auth.AuthClient;
import org.Core.Auth.AuthService;
import org.Core.Auth.TokenStorage;
import org.Core.Auth.UserSession;
import org.Core.UI.LobbyScreens.LobbyController;
import org.Core.UI.LobbyScreens.LobbyControllerStub;
import org.Core.UI.LobbyScreens.LobbyView;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class AppController {

    private final StackPane root;
    private final HostServices hostServices;
    private final AuthService authService;
    private final AuthClient authClient;
    private final LobbyController lobbyController;


    public AppController(StackPane root,
                         HostServices hostServices,
                         AuthService authService,AuthClient authClient) {
        this.root = root;
        this.hostServices = hostServices;
        this.authService=authService;
        this.authClient=authClient;
        this.lobbyController=new LobbyControllerStub(root);
    }

    public void start() {
        showSplash();
    }


    private void showSplash() {
        SplashView splash = new SplashView(() -> {
            transitionTo(showModeView());
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
        transitionTo(loading.getView());

        CompletableFuture.runAsync(() -> {
            try {

                Platform.runLater(() -> loading.setMessage("Checking session...", 0));
                boolean authenticated = authService.isUserAuthenticated();

                if (authenticated) {


                    Platform.runLater(() -> loading.setMessage("Loading your profile...", 1));

                   UserSession userSession=authClient.getUserSession();




                    Platform.runLater(() -> loading.setMessage("Almost there...", 2));

                    Thread.sleep(600);

                    Platform.runLater(() -> transitionTo(lobbyController.start(userSession)));

                } else {

                    Thread.sleep(800);
                    Platform.runLater(()->loading.setMessage("No Session Found",0));
                    Thread.sleep(500);
                    Platform.runLater(() -> transitionTo(showAuthView()));
                }

            } catch (Exception e) {
                Platform.runLater(() -> transitionTo(showAuthView()));
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
                            transitionTo(loading.getView());

                            CompletableFuture.runAsync(() -> {
                                try {
                                    Platform.runLater(() -> loading.setMessage("Loading your profile...", 1));

                                    UserSession userSession=authClient.getUserSession();

                                    Platform.runLater(() -> loading.setMessage("Almost there...", 2));

                                    Thread.sleep(600);

                                    Platform.runLater(() -> transitionTo(lobbyController.start(userSession)));

                                } catch (Exception e) {
                                    Platform.runLater(() -> transitionTo(showAuthView()));
                                }
                            });
                        });

                    } else {
                        Platform.runLater(() -> {
                            // TODO: show error message on auth view
                            System.out.println("LOGIN FAILED");
                        });
                    }
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return view.getView();
    }


    private void transitionTo(StackPane newView) {

        StackPane old = root.getChildren().isEmpty()
                ? null
                : (StackPane) root.getChildren().get(0);

        if (old == null) {
            root.getChildren().setAll(newView);
            return;
        }

        newView.setOpacity(0);
        root.getChildren().add(newView);

        FadeTransition out = new FadeTransition(Duration.millis(200), old);
        out.setFromValue(1);
        out.setToValue(0);

        FadeTransition in = new FadeTransition(Duration.millis(200), newView);
        in.setFromValue(0);
        in.setToValue(1);

        out.setOnFinished(e -> root.getChildren().remove(old));

        out.play();
        in.play();
    }
}
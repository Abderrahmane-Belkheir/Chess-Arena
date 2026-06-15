package org.Core.Internals.Screens;

import javafx.animation.FadeTransition;
import javafx.application.HostServices;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.SneakyThrows;
import org.Core.Auth.AuthService;
import org.Core.Auth.TokenStorage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class AppController {

    private final StackPane root;
    private final HostServices hostServices;
    private final AuthService authService;
    private final TokenStorage tokenStorage;

    public AppController(StackPane root,
                         HostServices hostServices,
                         AuthService authService,TokenStorage tokenStorage) {
        this.root = root;
        this.hostServices = hostServices;
        this.authService=authService;
        this.tokenStorage=tokenStorage;
    }

    public void start() {
        showSplash();
    }

    // ---------------- SPLASH ----------------
    private void showSplash() {
        SplashView splash = new SplashView(() -> {
            transitionTo(showModeView());
        });

        root.getChildren().setAll(splash.getView());
    }

    // ---------------- MODE ----------------

    private StackPane showModeView() {
        return new ModeView(
                () -> {
                    try {
                        handleOnline();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                this::handleOffline
        ).getView();
    }

    private void handleOffline() {
        System.out.println("Offline game started");
    }

    private void handleOnline() throws IOException, InterruptedException {
             if (authService.isUserAuthenticated()) {
            System.out.println("Start online game");
             } else {
                 transitionTo(showAuthView());
              }
    }

    // ---------------- AUTH ----------------
    private StackPane showAuthView() {

        AuthView view = new AuthView(() -> {

            try {

                CompletableFuture<Boolean> login = authService.callbackServer();
                String url = authService.redirect();
                hostServices.showDocument(url);

                login.thenAccept(ok -> {
                    if (ok) {
                        System.out.println("LOGIN OK → GO GAME ");
                    } else {
                        System.out.println("LOGIN FAILED");
                    }
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        return view.getView();
    }

    // ---------------- SMOOTH TRANSITION ----------------
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
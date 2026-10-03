package org.Core.UI;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.glass.ui.Window;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;

import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.Core.Auth.AuthService;
import org.Core.Auth.TokenStorage;
import org.Core.Game.Services.GameSessionService;
import org.Core.Realtime.RealtimeGateway;
import org.Core.UI.Game.ResignConfirmCard;
import org.Core.UI.OpeningScreens.GameController;
import org.Core.UI.OpeningScreens.GameControllerStub;
import org.Core.Config.ApiClient;
import org.Core.Config.AppModule;




import javafx.scene.layout.StackPane;


public class ChessArenaEntry extends Application {

    private Injector injector;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        Scene scene = new Scene(root, 1100, 700);
        scene.setFill(Color.web("#0a0a0a"));
        stage.setScene(scene);
        stage.setTitle("Chess Arena");
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);
        stage.show();
        DarkTitleBar.apply(stage, 0x000a0a0a);
        injector=Guice.createInjector(new AppModule(root,getHostServices()));
        injector.getInstance(TokenStorage.class).clearRefreshToken();
        injector.getInstance(GameController.class).start();

        stage.setOnCloseRequest(event -> {
            event.consume();
            GameSessionService sessionService = injector.getInstance(GameSessionService.class);
            if (!sessionService.isInActiveGame()) {
                Platform.exit();
                return;
            }
            new ResignConfirmCard(root,
                    "Close Chess Arena?",
                    "Closing now while you're in a game counts as a resignation — you'll lose. Are you sure?",
                    () -> sessionService.resignForAppClose(Platform::exit),
                    null);
        });
    }

    /**
     * JavaFX lifecycle hook — runs once when the app is actually shutting
     * down (window closed, Platform.exit(), etc.), regardless of which of
     * those triggered it. Marks the user offline server-side immediately
     * rather than leaving friends waiting on the presence TTL to expire.
     * Best-effort: if this fails (no session, server unreachable), the app
     * still closes normally.
     *
     * Shuts down the realtime gateway FIRST (cancels the 30s ping scheduler,
     * disconnects the STOMP session) before marking offline — otherwise that
     * scheduler, an ordinary non-daemon thread pool with no idea the app is
     * closing, can fire one more /app/online right after presence was
     * cleared and flip the user's friends back to seeing them online.
     */
    @Override
    public void stop() {
        if (injector == null) return;
        try {
            injector.getInstance(RealtimeGateway.class).shutdown();
        } catch (Exception ignored) {
        }
        try {
            injector.getInstance(ApiClient.class).POST(null, "/api/v1/users/presence/offline", null);
        } catch (Exception ignored) {
        }
    }









   private final class DarkTitleBar {

        private interface Dwmapi extends StdCallLibrary {
            Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);
            int DwmSetWindowAttribute(Pointer hwnd, int attribute, Pointer value, int size);
        }

        private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
        private static final int DWMWA_CAPTION_COLOR = 35;

        private DarkTitleBar() {}

        public static void apply(Stage stage, int bgrColor) {
            try {
                long hwndLong = Window.getWindows().get(0).getNativeHandle();
                Pointer hwnd = new Pointer(hwndLong);

                Memory darkMode = new Memory(4);
                darkMode.setInt(0, 1);
                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, darkMode, 4);

                Memory caption = new Memory(4);
                caption.setInt(0, bgrColor);
                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, caption, 4);
            } catch (Throwable ignored) {
                // Not Windows, or an OS version that doesn't support it — title bar just stays default.
            }
        }
    }

    }

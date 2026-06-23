package org.Core.UI;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.sun.glass.ui.Window;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.application.Application;
import javafx.scene.Scene;

import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.Core.UI.OpeningScreens.GameController;
import org.Core.Shared.AppModule;




import javafx.scene.layout.StackPane;


public class ChessArenaEntry extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        Scene scene = new Scene(root, 1100, 700);
        scene.setFill(Color.web("#0a0a0a"));
        stage.setScene(scene);
        stage.setTitle("Chess Desktop");
        stage.show();
        DarkTitleBar.apply(stage, 0x000a0a0a);
        Injector injector=Guice.createInjector(new AppModule(root,getHostServices()));
        injector.getInstance(GameController.class).start();
    }


    public static void main(String[] args) {
        launch(args);
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

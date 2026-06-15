package org.Core.Internals;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Scene;

import javafx.scene.layout.*;

import javafx.stage.Stage;

import org.Core.Auth.AuthService;
import org.Core.Auth.TokenResponse;
import org.Core.Auth.TokenStorage;
import org.Core.Internals.Screens.AppController;
import org.Core.Shared.AppModule;




import javafx.scene.layout.StackPane;


public class ChessArenaGame extends Application {


    @Override
    public void start(Stage stage) {
       Injector injector=Guice.createInjector(new AppModule());
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 1100, 700);
        AuthService authService=injector.getInstance(AuthService.class);
        TokenStorage tokenStorage=injector.getInstance(TokenStorage.class);
        AppController controller =
                new AppController(root, getHostServices(),authService,tokenStorage);
        stage.setScene(scene);
        stage.setTitle("Chess Desktop");
        stage.show();

        controller.start();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
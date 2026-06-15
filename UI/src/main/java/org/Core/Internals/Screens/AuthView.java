package org.Core.Internals.Screens;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class AuthView {

    private final StackPane root = new StackPane();

    private static final String BG =
            "-fx-background-color: linear-gradient(to bottom right, #070b14, #0b1220);";

    private static final String CARD =
            "-fx-background-color: rgba(17, 24, 39, 0.85);" +
                    "-fx-background-radius: 22;" +
                    "-fx-padding: 36;" +
                    "-fx-border-radius: 22;" +
                    "-fx-border-color: rgba(148,163,184,0.12);" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 25, 0.2, 0, 10);";

    public AuthView(Runnable onContinue) {

        root.setStyle(BG);

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(440);
        card.setStyle(CARD);

        Label title = new Label("Welcome back");
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 24px;
            -fx-font-weight: bold;
        """);

        Label subtitle = new Label(
                "Sign in to access ranked matches,\nratings, and global leaderboard"
        );

        subtitle.setStyle("""
            -fx-text-fill: #94a3b8;
            -fx-font-size: 13px;
            -fx-text-alignment: center;
        """);

        Button btn = new Button("Continue");
        btn.setPrefWidth(240);

        btn.setStyle("""
            -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);
            -fx-text-fill: white;
            -fx-background-radius: 12;
            -fx-padding: 12 18;
            -fx-font-size: 13px;
            -fx-font-weight: bold;
        """);

        btn.setOnMouseEntered(e ->
                btn.setStyle("""
                    -fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8);
                    -fx-text-fill: white;
                    -fx-background-radius: 12;
                    -fx-padding: 12 18;
                    -fx-font-size: 13px;
                    -fx-font-weight: bold;
                """)
        );

        btn.setOnMouseExited(e ->
                btn.setStyle("""
                    -fx-background-color: linear-gradient(to right, #3b82f6, #2563eb);
                    -fx-text-fill: white;
                    -fx-background-radius: 12;
                    -fx-padding: 12 18;
                    -fx-font-size: 13px;
                    -fx-font-weight: bold;
                """)
        );

        btn.setOnAction(e -> onContinue.run());

        card.getChildren().addAll(title, subtitle, btn);
        root.getChildren().add(card);
    }

    public StackPane getView() {
        return root;
    }
}
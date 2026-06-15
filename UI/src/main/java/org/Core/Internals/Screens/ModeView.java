package org.Core.Internals.Screens;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ModeView {

    private final StackPane root = new StackPane();

    private static final String BG =
            "-fx-background-color: linear-gradient(to bottom right, #070b14, #0b1220);";

    private static final String CARD =
            "-fx-background-color: rgba(17, 24, 39, 0.75);" +
                    "-fx-background-radius: 20;" +
                    "-fx-padding: 20;" +
                    "-fx-border-radius: 20;" +
                    "-fx-border-color: rgba(148,163,184,0.12);";

    public ModeView(Runnable online, Runnable offline) {

        root.setStyle(BG);

        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(900);

        Label title = new Label("Play Chess");
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 34px;
            -fx-font-weight: bold;
        """);

        Label subtitle = new Label("Choose your battle mode");
        subtitle.setStyle("""
            -fx-text-fill: #94a3b8;
            -fx-font-size: 14px;
        """);

        HBox cards = new HBox(24);
        cards.setAlignment(Pos.CENTER);

        cards.getChildren().addAll(
                createCard("⚡ Online", "Ranked matches with real players", "#3b82f6", online),
                createCard("🤖 Offline", "Train against adaptive AI engines", "#22c55e", offline)
        );

        container.getChildren().addAll(title, subtitle, cards);
        root.getChildren().add(container);
    }

    private VBox createCard(String title, String desc, String accent, Runnable action) {

        VBox card = new VBox(10);
        card.setPrefSize(320, 180);
        card.setStyle(CARD);

        Label t = new Label(title);
        t.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """);

        Label d = new Label(desc);
        d.setStyle("""
            -fx-text-fill: #94a3b8;
            -fx-font-size: 13px;
        """);
        d.setWrapText(true);

        Region accentBar = new Region();
        accentBar.setPrefHeight(3);
        accentBar.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 10;");

        card.getChildren().addAll(t, accentBar, d);

        // Hover effect (lift + glow)
        card.setOnMouseEntered(e -> {
            card.setStyle(CARD +
                    "-fx-border-color: " + accent + ";" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 20, 0.2, 0, 8);"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle(CARD);

            ScaleTransition st = new ScaleTransition(Duration.millis(120), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    public StackPane getView() {
        return root;
    }
}
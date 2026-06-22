package org.Core.UI.LobbyScreens.Friends;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public final class FooterViews {

    private FooterViews() {}

    public static VBox buildBigLoader() {
        VBox spinner = new VBox(8);
        spinner.setAlignment(Pos.CENTER);
        spinner.setPadding(new Insets(40, 14, 40, 14));

        Label dots = new Label("Loading...");
        dots.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

        FadeTransition ft = new FadeTransition(Duration.millis(600), dots);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
        ft.play();

        spinner.getChildren().add(dots);
        spinner.setUserData("footer");
        return spinner;
    }

    public static HBox buildInlineLoader() {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(14));

        Label dots = new Label("Loading more...");
        dots.setStyle("-fx-text-fill: #444444; -fx-font-size: 11px;");

        row.getChildren().add(dots);
        row.setUserData("footer");
        return row;
    }

    public static HBox buildError(String message, Runnable onRetry) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));

        Label icon = new Label("⚠");
        icon.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 12px;");

        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        msg.setWrapText(true);

        row.getChildren().addAll(icon, msg);
        row.setUserData("footer");
        row.setStyle("-fx-cursor: hand;");
        row.setOnMouseClicked(e -> onRetry.run());
        return row;
    }

    public static VBox buildEmptyState(String message) {
        VBox empty = new VBox(8);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(40, 14, 40, 14));

        Label icon = new Label("♟");
        icon.setStyle("-fx-text-fill: #2a2a2a; -fx-font-size: 28px;");

        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

        empty.getChildren().addAll(icon, msg);
        return empty;
    }
}
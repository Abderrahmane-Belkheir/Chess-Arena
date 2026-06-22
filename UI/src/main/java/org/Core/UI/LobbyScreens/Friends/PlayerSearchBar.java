
package org.Core.UI.LobbyScreens.Friends;


import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.Core.Social.DTO.UserSummary;
import org.Core.Social.FriendShipClient;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.IntConsumer;


public class PlayerSearchBar {

    private final HBox wrap = new HBox(8);
    private final VBox results = new VBox(0);
    private final TextField field = new TextField();

    private final FriendShipClient client;
    private final IntConsumer onIncomingCountDelta;

    public PlayerSearchBar(FriendShipClient client, IntConsumer onIncomingCountDelta) {
        this.client = client;
        this.onIncomingCountDelta = onIncomingCountDelta;
        buildField();
        buildResultsContainer();
    }

    public HBox getFieldView()   { return wrap; }
    public VBox getResultsView() { return results; }

    private void buildField() {
        wrap.setAlignment(Pos.CENTER_LEFT);
        wrap.setPadding(new Insets(12, 14, 12, 14));
        wrap.setStyle("""
            -fx-border-color: transparent transparent #1a1a1a transparent;
            -fx-border-width: 1;
        """);

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(7, 10, 7, 10));
        searchBox.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-background-radius: 8;
            -fx-border-color: #252525;
            -fx-border-radius: 8;
            -fx-border-width: 1;
        """);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("⌕");
        searchIcon.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");

        field.setPromptText("Enter player ID...");
        field.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e0e0e0;
            -fx-prompt-text-fill: #444444;
            -fx-font-size: 12px;
            -fx-border-color: transparent;
            -fx-padding: 0;
        """);
        HBox.setHgrow(field, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, field);
        wrap.getChildren().add(searchBox);

        field.focusedProperty().addListener((obs, old, focused) ->
                searchBox.setStyle(searchBox.getStyle().replace(
                        focused ? "#252525" : "#81b64c",
                        focused ? "#81b64c" : "#252525")));

        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                handleSearch(field.getText().trim());
            if (e.getCode() == KeyCode.ESCAPE) {
                field.clear();
                hideResults();
            }
        });

        field.textProperty().addListener((obs, old, text) -> {
            if (text.isBlank()) hideResults();
        });
    }

    private void buildResultsContainer() {
        results.setStyle("""
            -fx-background-color: #161616;
            -fx-border-color: transparent transparent #1e1e1e transparent;
            -fx-border-width: 1;
        """);
        results.setVisible(false);
        results.setManaged(false);
    }

    private void handleSearch(String query) {
        if (query.isBlank()) {
            showError("Please enter a player ID.");
            return;
        }
        if (query.length() != 6) {
            showError("Player ID must be exactly 6 characters.");
            return;
        }

        showLoading();

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return client.search(Integer.parseInt(query));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(result -> Platform.runLater(() -> {
                    if (result != null && result.getId() != 0)
                        showResult(result);
                    else
                        showNotFound();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError(resolveError(ex)));
                    return null;
                });
    }

    private void showLoading() {
        results.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));

        Label dots = new Label("Searching...");
        dots.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

        FadeTransition ft = new FadeTransition(Duration.millis(600), dots);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
        ft.play();

        row.getChildren().add(dots);
        results.getChildren().add(row);
        results.setVisible(true);
        results.setManaged(true);
    }

    private void showResult(UserSummary result) {
        results.getChildren().clear();
        results.getChildren().add(SearchResultCard.build(
                result, client, onIncomingCountDelta, this::showError));
        reveal();
    }

    private void showNotFound() {
        results.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        Label icon = new Label("✕");
        icon.setStyle("-fx-text-fill: #e05555; -fx-font-size: 12px;");
        Label msg = new Label("No player found");
        msg.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        row.getChildren().addAll(icon, msg);

        results.getChildren().add(row);
        reveal();
    }

    private void showError(String message) {
        results.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        Label icon = new Label("⚠");
        icon.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 12px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        msg.setWrapText(true);
        row.getChildren().addAll(icon, msg);

        results.getChildren().add(row);
        reveal();
    }

    private void reveal() {
        results.setVisible(true);
        results.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(150), results);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideResults() {
        results.setVisible(false);
        results.setManaged(false);
        results.getChildren().clear();
    }

    private String resolveError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AuthenticationException)
            return "Session expired. Please log in again.";
        return "Request failed. Check your connection.";
    }
}
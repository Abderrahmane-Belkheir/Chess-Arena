
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
import java.util.function.IntConsumer;


public class PlayerSearchBar {

    private final HBox wrap = new HBox(8);
    private final VBox results = new VBox(0);
    private final TextField field = new TextField();

    private final FriendShipClient client;
    private final IntConsumer onIncomingCountDelta;
    private final IntConsumer onRequestRemoved;
    // Let the owner (FriendsPanel) swap its own "empty search" placeholder in
    // and out as this results panel becomes visible/hidden, without either
    // side needing to know the other's internal state.
    private final Runnable onResultsShown;
    private final Runnable onResultsHidden;

    public PlayerSearchBar(FriendShipClient client, IntConsumer onIncomingCountDelta,
                            IntConsumer onRequestRemoved,
                            Runnable onResultsShown, Runnable onResultsHidden) {
        this.client = client;
        this.onIncomingCountDelta = onIncomingCountDelta;
        this.onRequestRemoved = onRequestRemoved;
        this.onResultsShown = onResultsShown;
        this.onResultsHidden = onResultsHidden;
        buildField();
        buildResultsContainer();
    }

    public HBox getFieldView()   { return wrap; }
    public VBox getResultsView() { return results; }

    public void focusField() { field.requestFocus(); }

    /** Clears the query and hides any results — back to a blank search box. */
    public void reset() {
        field.clear();
        hideResults();
    }

    private void buildField() {
        wrap.setAlignment(Pos.CENTER_LEFT);
        wrap.setPadding(new Insets(8, 10, 8, 10));
        wrap.getStyleClass().add("search-wrap");

        HBox searchBox = new HBox(7);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5, 9, 5, 9));
        searchBox.getStyleClass().add("search-box");
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("⌕");
        searchIcon.getStyleClass().add("search-icon");

        field.setPromptText("Enter player ID...");
        field.getStyleClass().add("search-field");
        HBox.setHgrow(field, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, field);
        wrap.getChildren().add(searchBox);

        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) searchBox.getStyleClass().add("input-focused");
            else searchBox.getStyleClass().remove("input-focused");
        });

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
        results.getStyleClass().add("search-results");
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
        dots.getStyleClass().add("state-muted");

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
        if (onResultsShown != null) onResultsShown.run();
    }

    private void showResult(UserSummary result) {
        results.getChildren().clear();
        results.getChildren().add(SearchResultCard.build(
                result, client, onIncomingCountDelta, onRequestRemoved, this::showError));
        reveal();
    }

    private void showNotFound() {
        results.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        Label icon = new Label("✕");
        icon.getStyleClass().add("state-error-icon");
        Label msg = new Label("No player found");
        msg.getStyleClass().add("state-muted");
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
        icon.getStyleClass().add("state-warn-icon");
        Label msg = new Label(message);
        msg.getStyleClass().add("state-warn-text");
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
        if (onResultsShown != null) onResultsShown.run();
    }

    private void hideResults() {
        results.setVisible(false);
        results.setManaged(false);
        results.getChildren().clear();
        if (onResultsHidden != null) onResultsHidden.run();
    }

    private String resolveError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AuthenticationException)
            return "Session expired. Please log in again.";
        return "Request failed. Check your connection.";
    }
}
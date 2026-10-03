package org.Core.UI.Game;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.Core.Game.History.DTO.GameHistory;
import org.Core.Game.History.GameHistoryClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * RecentGames — a compact card section stacked below the friends list in
 * the lobby's central column.
 *
 * Breakdown:
 *  - Bounded-height dark card
 *  - Header: "View all" link (the Friends/Recent Games toggle above already
 *    labels the section, see LobbyView)
 *  - A list of game rows, each its own small bordered card (matching
 *    Chess-UI-Refactoring.png exactly):
 *      [pill accent bar] "vs OpponentName (elo)" / "3+2 Blitz • White • 14m ago"   [+14]
 *    No avatar — the reference row is name/metadata + a single colored
 *    delta pill (win = green, loss = red, draw = grey), not a separate
 *    "Win"/"Loss" text label.
 *
 * Data model: GameEntry record — fill from your match history service.
 * Visual styling lives in /css/lobby.css (scoped to the lobby root).
 */
public class RecentGames {

    // ── Data model ────────────────────────────────────────────────────
    public enum Result { WIN, LOSS, DRAW }

    public record GameEntry(
            String gameId,        // used when row is clicked for replay
            String opponentName,
            int    opponentElo,
            String timeControl,   // e.g. "3+2 Blitz", "5 min Rapid"
            String colorPlayed,   // e.g. "White", "Black"
            int    eloDelta,      // e.g. +12, -8, +1
            Result result,
            String timeAgo        // e.g. "2m ago", "Yesterday"
    ) {}

    // ── UI ────────────────────────────────────────────────────────────
    private final VBox root = new VBox(0);
    // Spacing (not 0) now that each row is its own bordered card rather than
    // a flat hover row sharing edges with its neighbors.
    private final VBox listContainer = new VBox(8);
    private final LobbyController controller;
    private final GameHistoryClient gameHistoryClient;
    private boolean loadedOnce = false;

    public RecentGames(LobbyController controller, GameHistoryClient gameHistoryClient) {
        this.controller = controller;
        this.gameHistoryClient = gameHistoryClient;

        // Friends and Recent Games share one slot in the Lobby, inside a card
        // whose header IS the Friends/Recent Games toggle (see LobbyView) —
        // this panel itself stays chrome-less (no border/background of its
        // own) so it reads as part of that one card, not a nested card.
        root.setMinHeight(150);

        // ── Header ────────────────────────────────────────────────────
        // Just "View all" — the toggle above already labels this as
        // "Recent Games", so repeating the title here would be redundant.
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(11, 12, 9, 12));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label viewAll = new Label("View all");
        viewAll.getStyleClass().add("view-all-link");

        header.getChildren().addAll(headerSpacer, viewAll);

        // ── Separator ─────────────────────────────────────────────────
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.getStyleClass().add("panel-separator");

        // ── Scrollable list ───────────────────────────────────────────
        listContainer.setFillWidth(true);
        listContainer.setPadding(new Insets(10, 12, 12, 12));

        ScrollPane scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, sep, scroll);

        // Load placeholder data — replace with controller.getRecentGames()
    }

    // ── public API ────────────────────────────────────────────────────

    public void setGames(List<GameEntry> games) {
        listContainer.getChildren().clear();
        for (GameEntry g : games) {
            listContainer.getChildren().add(buildRow(g));
        }
    }

    /** Fetches game history on the first call only — later calls (switching back to this tab) are no-ops. */
    public void loadIfNeeded() {
        if (loadedOnce) return;
        loadedOnce = true;
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return gameHistoryClient.fetchHistory();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(history -> Platform.runLater(() -> setGames(toGameEntries(history))))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadedOnce = false; // allow retry on next tab switch
                        System.out.println("[RecentGames] history fetch failed: " + ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * The server DTO only carries name/result/eloGained/date — gameId,
     * opponentElo, timeControl and colorPlayed aren't in it yet, so those
     * are placeholders (empty/zero) rather than invented values, until the
     * DTO actually grows those fields.
     */
    private List<GameEntry> toGameEntries(GameHistory history) {
        return history.getGames().stream()
                .map(g -> new GameEntry(
                        "",
                        g.getName(),
                        0,
                        "—",
                        "—",
                        g.getEloGained(),
                        Result.valueOf(g.getResult().name()),
                        formatTimeAgo(g.getDate())
                ))
                .toList();
    }

    private String formatTimeAgo(Instant date) {
        if (date == null) return "";
        Duration elapsed = Duration.between(date, Instant.now());
        if (elapsed.toMinutes() < 1)  return "just now";
        if (elapsed.toHours() < 1)    return elapsed.toMinutes() + "m ago";
        if (elapsed.toDays() < 1)     return elapsed.toHours() + "h ago";
        return elapsed.toDays() + "d ago";
    }

    public VBox getView() { return root; }

    // ── row builder ───────────────────────────────────────────────────

    private HBox buildRow(GameEntry g) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 10));
        // Each row is its own small bordered/rounded card now (see
        // .recent-row in lobby.css) rather than a flat row in a shared list.
        row.getStyleClass().add("recent-row");

        String resultClass = switch (g.result()) {
            case WIN  -> "result-win";
            case LOSS -> "result-loss";
            case DRAW -> "result-draw";
        };

        // A rounded pill (fully-rounded radius on a narrow bar), matching
        // the two-line info block's height, not a thin flat rectangle.
        Region accentBar = new Region();
        accentBar.setPrefWidth(4);
        accentBar.setPrefHeight(32);
        accentBar.getStyleClass().add(switch (g.result()) {
            case WIN  -> "accent-bar-win";
            case LOSS -> "accent-bar-loss";
            case DRAW -> "accent-bar-draw";
        });

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox titleLine = new HBox(5);
        titleLine.setAlignment(Pos.CENTER_LEFT);
        Label vsName = new Label("vs " + g.opponentName());
        vsName.getStyleClass().add("recent-name");
        Label opponentElo = new Label("(" + g.opponentElo() + ")");
        opponentElo.getStyleClass().add("recent-opponent-elo");
        titleLine.getChildren().addAll(vsName, opponentElo);

        Label meta = new Label(g.timeControl() + "  •  " + g.colorPlayed() + "  •  " + g.timeAgo());
        meta.getStyleClass().add("recent-time");

        info.getChildren().addAll(titleLine, meta);

        // Delta-only pill — the colored background already communicates
        // win/loss/draw, so there's no separate "Win"/"Loss" text label.
        String deltaStr = g.eloDelta() >= 0 ? "+" + g.eloDelta() : String.valueOf(g.eloDelta());
        Label deltaPill = new Label(deltaStr);
        deltaPill.getStyleClass().addAll("delta-pill", resultClass);

        // Fetches the per-game summary on click — no menu/UI wired up for the
        // result yet, just the request.
        Button menuBtn = new Button("⋯");
        menuBtn.getStyleClass().add("row-menu-btn");
        menuBtn.setOnAction(e -> fetchSummary(g.gameId()));
        menuBtn.setOnMouseClicked(javafx.event.Event::consume);

        row.getChildren().addAll(accentBar, info, deltaPill, menuBtn);

        row.setOnMouseClicked(e -> controller.onGameClicked(g.gameId())); // TODO: open replay

        return row;
    }

    private void fetchSummary(String gameId) {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return gameHistoryClient.fetchSummary(gameId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(raw -> Platform.runLater(() -> System.out.println("[RecentGames] summary " + gameId + ": " + raw)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> System.out.println("[RecentGames] summary fetch failed for " + gameId + ": " + ex.getMessage()));
                    return null;
                });
    }
}

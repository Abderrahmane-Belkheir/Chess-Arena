package org.Core.UI.LobbyScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * RecentGames — right sidebar.
 *
 * Screenshot breakdown:
 *  - Fixed-width dark card (~260px)
 *  - Header: "Recent Games" bold white
 *  - Scrollable list of game rows:
 *      [avatar] [opponent name bold / time-ago muted] [result badge + elo delta]
 *      Win  → green "Win"  label  +  green "+12"
 *      Loss → red   "Loss" label  +  red   "-8"
 *      Draw → gray  "Draw" label  +  gray  "+1"
 *
 * Data model: GameEntry record — fill from your match history service.
 */
public class RecentGames {

    // ── Data model ────────────────────────────────────────────────────
    public enum Result { WIN, LOSS, DRAW }

    public record GameEntry(
            String gameId,        // used when row is clicked for replay
            String opponentName,
            int    eloDelta,      // e.g. +12, -8, +1
            Result result,
            String timeAgo,       // e.g. "2m ago", "Yesterday"
            String avatarColor
    ) {}

    // ── UI ────────────────────────────────────────────────────────────
    private final VBox root = new VBox(0);
    private final VBox listContainer = new VBox(0);
    private final LobbyController controller;

    public RecentGames(LobbyController controller) {
        this.controller = controller;

        root.setPrefWidth(264);
        root.setMinWidth(264);
        root.setMaxWidth(264);
        root.setStyle("""
            -fx-background-color: #111111;
            -fx-border-color: transparent transparent transparent #1e1e1e;
            -fx-border-width: 1;
        """);

        // ── Header ────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 16, 14, 16));

        Label title = new Label("Recent Games");
        title.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 14px;
            -fx-font-weight: 800;
        """);
        header.getChildren().add(title);

        // ── Separator ─────────────────────────────────────────────────
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color: #1e1e1e;");

        // ── Scrollable list ───────────────────────────────────────────
        listContainer.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, sep, scroll);

        // Load placeholder data — replace with controller.getRecentGames()
        setGames(placeholderGames());
    }

    // ── public API ────────────────────────────────────────────────────

    public void setGames(List<GameEntry> games) {
        listContainer.getChildren().clear();
        for (GameEntry g : games) {
            listContainer.getChildren().add(buildRow(g));
        }
    }

    public VBox getView() { return root; }

    // ── row builder ───────────────────────────────────────────────────

    private HBox buildRow(GameEntry g) {
        HBox row = new HBox(11);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(11, 16, 11, 16));
        row.setStyle("-fx-cursor: hand;");

        // Avatar
        Label initLbl = new Label(initials(g.opponentName()));
        StackPane avatar = NavBar.buildAvatar(initLbl, g.avatarColor());

        // Name + time
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(g.opponentName());
        name.setStyle("""
            -fx-text-fill: #e0e0e0;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
        """);
        Label time = new Label(g.timeAgo());
        time.setStyle("-fx-text-fill: #444444; -fx-font-size: 11px;");
        info.getChildren().addAll(name, time);

        // Result + elo delta
        VBox resultBox = new VBox(2);
        resultBox.setAlignment(Pos.CENTER_RIGHT);

        String resultText;
        String resultColor;
        String deltaColor;
        switch (g.result()) {
            case WIN  -> { resultText = "Win";  resultColor = "#81b64c"; deltaColor = "#81b64c"; }
            case LOSS -> { resultText = "Loss"; resultColor = "#e05555"; deltaColor = "#e05555"; }
            default   -> { resultText = "Draw"; resultColor = "#888888"; deltaColor = "#888888"; }
        }

        Label resultLabel = new Label(resultText);
        resultLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-font-size: 12px;
            -fx-font-weight: 700;
        """, resultColor));

        String deltaStr = g.eloDelta() >= 0 ? "+" + g.eloDelta() : String.valueOf(g.eloDelta());
        Label deltaLabel = new Label(deltaStr);
        deltaLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
        """, deltaColor));

        resultBox.getChildren().addAll(resultLabel, deltaLabel);

        row.getChildren().addAll(avatar, info, resultBox);

        // Hover + click
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #1a1a1a; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));
        row.setOnMouseClicked(e -> controller.onGameClicked(g.gameId())); // TODO: open replay

        // Row separator
        VBox wrapper = new VBox(0);
        Region rowSep = new Region();
        rowSep.setPrefHeight(1);
        rowSep.setMaxWidth(Double.MAX_VALUE);
        rowSep.setStyle("-fx-background-color: #1a1a1a;");

        return row;
    }

    // ── placeholder data (replace with real match history service) ────

    private List<GameEntry> placeholderGames() {
        return List.of(
            new GameEntry("g001", "queens_gambit",  +12, Result.WIN,  "2m ago",    "#7c3e3e"),
            new GameEntry("g002", "rook_n_roll",     -8, Result.LOSS, "18m ago",   "#7c5c3e"),
            new GameEntry("g003", "pawnstar",        +9, Result.WIN,  "1h ago",    "#3e5c7c"),
            new GameEntry("g004", "endgame_emma",    +1, Result.DRAW, "3h ago",    "#3e7c7c"),
            new GameEntry("g005", "checkmate_chad", +14, Result.WIN,  "5h ago",    "#3e7c5c"),
            new GameEntry("g006", "bishop_bash",    -11, Result.LOSS, "Yesterday", "#7c3e5c"),
            new GameEntry("g007", "casual_carl",     +7, Result.WIN,  "Yesterday", "#5c7c3e")
        );
    }

    private String initials(String username) {
        if (username == null || username.isEmpty()) return "?";
        String[] parts = username.split("_");
        if (parts.length >= 2)
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }
}
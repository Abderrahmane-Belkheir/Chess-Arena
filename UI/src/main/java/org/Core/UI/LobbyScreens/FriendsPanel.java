package org.Core.UI.LobbyScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * FriendsPanel — left sidebar.
 *
 * Screenshot breakdown:
 *  - Fixed-width dark card (~260px)
 *  - Header: "Friends" bold white + green "● 4 online" badge top-right
 *  - Scrollable list of friend rows:
 *      [avatar initials circle] [username bold] [elo muted gray below]
 *      Green dot = online, Gray dot = offline
 *  - Hover highlight on each row
 *
 * Data model: FriendEntry record — fill from your server/db.
 */
public class FriendsPanel {

    // ── Data model ────────────────────────────────────────────────────
    public record FriendEntry(
            String username,   // display name
            int    elo,        // current rating
            boolean online,    // true = green dot
            String avatarColor // hex, e.g. "#7c5c3e" — derive from username hash
    ) {}

    // ── UI ────────────────────────────────────────────────────────────
    private final VBox root = new VBox(0);
    private final VBox listContainer = new VBox(0);
    private final Label onlineCountLabel = new Label("4 online");
    private final LobbyController controller;

    public FriendsPanel(LobbyController controller) {
        this.controller = controller;

        root.setPrefWidth(264);
        root.setMinWidth(264);
        root.setMaxWidth(264);
        root.setStyle("""
            -fx-background-color: #111111;
            -fx-border-color: transparent #1e1e1e transparent transparent;
            -fx-border-width: 1;
        """);

        // ── Header ────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 16, 14, 16));

        Label title = new Label("Friends");
        title.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 14px;
            -fx-font-weight: 800;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Online badge
        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(3, 9, 3, 9));
        badge.setStyle("""
            -fx-background-color: rgba(129,182,76,0.12);
            -fx-background-radius: 10;
        """);
        Region badgeDot = new Region();
        badgeDot.setPrefSize(6, 6);
        badgeDot.setStyle("-fx-background-color: #81b64c; -fx-background-radius: 3;");
        onlineCountLabel.setStyle("""
            -fx-text-fill: #81b64c;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
        """);
        badge.getChildren().addAll(badgeDot, onlineCountLabel);

        header.getChildren().addAll(title, spacer, badge);

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

        // Load placeholder data — replace with controller.getFriends()
        setFriends(placeholderFriends());
    }

    // ── public API ────────────────────────────────────────────────────

    public void setFriends(List<FriendEntry> friends) {
        listContainer.getChildren().clear();

        long onlineCount = friends.stream().filter(FriendEntry::online).count();
        onlineCountLabel.setText(onlineCount + " online");

        for (FriendEntry f : friends) {
            listContainer.getChildren().add(buildRow(f));
        }
    }

    public VBox getView() { return root; }

    // ── row builder ───────────────────────────────────────────────────

    private HBox buildRow(FriendEntry f) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-cursor: hand;");

        // Avatar with online dot overlay
        StackPane avatarWrap = new StackPane();
        avatarWrap.setMinSize(38, 38);
        avatarWrap.setPrefSize(38, 38);

        Label initials = new Label(initials(f.username()));
        StackPane avatar = NavBar.buildAvatar(initials, f.avatarColor());

        // Online status dot (bottom-right of avatar)
        Region statusDot = new Region();
        statusDot.setPrefSize(9, 9);
        statusDot.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 5;
            -fx-border-color: #111111;
            -fx-border-radius: 5;
            -fx-border-width: 1.5;
        """, f.online() ? "#81b64c" : "#555555"));
        StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);

        avatarWrap.getChildren().addAll(avatar, statusDot);

        // Name + elo
        VBox info = new VBox(2);
        Label name = new Label(f.username());
        name.setStyle("""
            -fx-text-fill: #e0e0e0;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
        """);
        Label elo = new Label(String.valueOf(f.elo()));
        elo.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
        info.getChildren().addAll(name, elo);

        row.getChildren().addAll(avatarWrap, info);

        // Hover
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #1a1a1a; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));
        row.setOnMouseClicked(e -> controller.onFriendClicked(f.username())); // TODO: open challenge dialog

        // Separator
        VBox wrapper = new VBox(0);
        Region rowSep = new Region();
        rowSep.setPrefHeight(1);
        rowSep.setMaxWidth(Double.MAX_VALUE);
        rowSep.setStyle("-fx-background-color: #1a1a1a;");
        wrapper.getChildren().addAll(row, rowSep);
        // We return row; caller adds wrapper — easier done inline:
        return row;
    }

    // ── placeholder data (replace with real API call) ─────────────────

    private List<FriendEntry> placeholderFriends() {
        return List.of(
            new FriendEntry("knight_rider",    1685, true,  "#5c3e7c"),
            new FriendEntry("queens_gambit",   1502, true,  "#7c3e3e"),
            new FriendEntry("pawnstar",        1340, true,  "#3e5c7c"),
            new FriendEntry("rook_n_roll",     1899, true,  "#7c5c3e"),
            new FriendEntry("checkmate_chad",  1210, true,  "#3e7c5c"),
            new FriendEntry("bishop_bash",     1455, false, "#7c3e5c"),
            new FriendEntry("endgame_emma",    2050, false, "#3e7c7c"),
            new FriendEntry("casual_carl",      980, false, "#5c7c3e")
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
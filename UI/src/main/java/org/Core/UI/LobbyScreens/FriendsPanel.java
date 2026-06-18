package org.Core.UI.LobbyScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

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


import javafx.animation.FadeTransition;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class FriendsPanel {

    public enum status{InLobby,InGame}

    // ── Data model ────────────────────────────────────────────────────
    public record FriendEntry(
            String username,
            int    elo,
            status status,
            String avatarUrl,
            String avatarColor
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

        setFriends(placeholderFriends());
    }

    // ── public API ────────────────────────────────────────────────────

    public void setFriends(List<FriendEntry> friends) {
        listContainer.getChildren().clear();


        onlineCountLabel.setText(friends.size()+ " online");

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

        // 1. Prepare Avatar Data
        String userInitials = initials(f.username());

        // Call the updated buildAvatar (passing imageUrl and initials)
        // Assuming your FriendEntry record has an avatarUrl field
        StackPane avatar = buildAvatar(f.avatarUrl(), userInitials, f.avatarColor());

        // 2. Container for Avatar + Status Dot side-by-side
        HBox avatarContainer = new HBox(8);
        avatarContainer.setAlignment(Pos.CENTER_LEFT);

        // 3. Status Dot
        Region statusDot = new Region();
        statusDot.setPrefSize(9, 9);
        statusDot.setStyle(String.format("""
        -fx-background-color: %s;
        -fx-background-radius: 5;
        -fx-border-color: #111111;
        -fx-border-radius: 5;
        -fx-border-width: 1.5;
    """, getStatusColor(f.status())));

        avatarContainer.getChildren().addAll(avatar, statusDot);

        // 4. Name + elo info
        VBox info = new VBox(2);
        Label name = new Label(f.username());
        name.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px; -fx-font-weight: 600;");
        Label elo = new Label(String.valueOf(f.elo()));
        elo.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
        info.getChildren().addAll(name, elo);

        row.getChildren().addAll(avatarContainer, info);

        // Interactions
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #1a1a1a; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));
        row.setOnMouseClicked(e -> controller.onFriendClicked(f.username()));

        return row;
    }
    private HBox buildRow2(FriendEntry f) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-cursor: hand;");

        // Container for Avatar + Status Dot side-by-side
        HBox avatarContainer = new HBox(8);
        avatarContainer.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        Label initials = new Label(initials(f.username()));
        StackPane avatar = NavBar.buildAvatar(initials, f.avatarUrl());

        // Status Dot
        Region statusDot = new Region();
        statusDot.setPrefSize(9, 9);
        statusDot.setStyle(String.format("""
        -fx-background-color: %s;
        -fx-background-radius: 5;
        -fx-border-color: #111111;
        -fx-border-radius: 5;
        -fx-border-width: 1.5;
    """, getStatusColor(f.status())));

        avatarContainer.getChildren().addAll(avatar, statusDot);

        // Name + elo
        VBox info = new VBox(2);
        Label name = new Label(f.username());
        name.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 13px; -fx-font-weight: 600;");
        Label elo = new Label(String.valueOf(f.elo()));
        elo.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
        info.getChildren().addAll(name, elo);

        row.getChildren().addAll(avatarContainer, info);

        // Interaction
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #1a1a1a; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));
        row.setOnMouseClicked(e -> controller.onFriendClicked(f.username()));

        return row;
    }

    // Helper method to keep your layout code clean
    private String getStatusColor(status status) {
        return switch (status) {
            case InGame  -> "#81b64c"; // Green
            case InLobby -> "#3498db"; // Blue
            default      -> "#555555"; // Grey
        };
    }
    public static StackPane buildAvatar(String imageUrl, String initials, String bgColor) {
        StackPane avatar = new StackPane();
        avatar.setMinSize(38, 38);
        avatar.setPrefSize(38, 38);

        // Create a circular clip to keep it a circle
        Circle clip = new Circle(19, 19, 19);
        avatar.setClip(clip);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load real image
            Image image = new Image(imageUrl, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(38);
            imageView.setFitHeight(38);
            imageView.setPreserveRatio(true);
            avatar.getChildren().add(imageView);
        } else {
            // Fallback to initials
            avatar.setStyle("-fx-background-color: " + bgColor + ";");
            avatar.getChildren().add(new Label(initials));
        }

        return avatar;
    }
    // ── placeholder data (replace with real API call) ─────────────────

    private List<FriendEntry> placeholderFriends() {
        return List.of(
                new FriendEntry("knight_rider",    1685, status.InLobby, "https://pfppjfsqznlcznlofyvx.supabase.co/storage/v1/object/public/Chess-Arena/Screenshot%202026-05-05%20170212.png" ,"#5c3e7c"),
                new FriendEntry("queens_gambit",   1502, status.InGame,  "","#7c3e3e"),
                new FriendEntry("pawnstar",        1340, status.InGame,  "","#3e5c7c"),
                new FriendEntry("rook_n_roll",     1899, status.InLobby,  "","#7c5c3e"),
                new FriendEntry("checkmate_chad",  1210, status.InGame,  "","#3e7c5c"),
                new FriendEntry("bishop_bash",     1455, status.InLobby, "","#7c3e5c"),
                new FriendEntry("endgame_emma",    2050, status.InLobby, "","#3e7c7c"),
                new FriendEntry("casual_carl",      980, status.InLobby, "","#5c7c3e")
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
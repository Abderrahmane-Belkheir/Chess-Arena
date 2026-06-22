package org.Core.UI.LobbyScreens.Friends;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.Core.Social.DTO.FriendsPage;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

public final class FriendRow {

    private FriendRow() {}

    public static HBox build(FriendsPage.FriendEntry f, LobbyController controller) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-cursor: hand;");

        if (f.getStatus() == FriendsPage.Status.Offline) row.setOpacity(0.5);

        HBox avatarContainer = new HBox(8);
        avatarContainer.setAlignment(Pos.CENTER_LEFT);

        var avatar = Avatar.build(f.getAvatarUrl(), Avatar.initials(f.getUsername()), f.getAvatarColor());

        Region statusDot = new Region();
        statusDot.setPrefSize(9, 9);
        statusDot.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 5;
            -fx-border-color: #111111;
            -fx-border-radius: 5;
            -fx-border-width: 1.5;
        """, statusColor(f.getStatus())));

        avatarContainer.getChildren().addAll(avatar, statusDot);

        VBox info = new VBox(2);
        Label name = new Label(f.getUsername());
        name.setStyle("""
            -fx-text-fill: #e0e0e0;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
        """);

        Label elo = new Label(f.getStatus() == FriendsPage.Status.InGame
                ? "In game · " + f.getElo()
                : String.valueOf(f.getElo()));
        elo.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-font-size: 11px;
        """, f.getStatus() == FriendsPage.Status.InGame ? "#e6b84a" : "#555555"));
        info.getChildren().addAll(name, elo);

        row.getChildren().addAll(avatarContainer, info);

        row.setOnMouseEntered(e -> {
            if (f.getStatus() != FriendsPage.Status.Offline)
                row.setStyle("-fx-background-color: #1a1a1a; -fx-cursor: hand;");
        });
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));
        row.setOnMouseClicked(e -> controller.onFriendClicked(f.getUsername()));
        return row;
    }

    private static String statusColor(FriendsPage.Status status) {
        return switch (status) {
            case InGame  -> "#e6b84a";
            case InLobby -> "#81b64c";
            case Offline -> "#333333";
        };
    }
}
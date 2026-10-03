package org.Core.UI.LobbyScreens.Center;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Polished placeholder for lobby sections that aren't implemented yet
 * (Rankings, Puzzles, Clubs) — same surface language as the rest of the
 * lobby so it reads as intentional rather than an unfinished screen.
 */
public class ComingSoonPanel {

    private final StackPane root = new StackPane();

    public ComingSoonPanel(String glyph, String sectionName) {
        root.setPadding(new Insets(24));
        VBox.setVgrow(root, Priority.ALWAYS);

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.setPadding(new Insets(48, 40, 48, 40));
        card.getStyleClass().add("coming-soon-card");

        Label icon = new Label(glyph);
        icon.getStyleClass().add("coming-soon-icon");

        Label badge = new Label(sectionName.toUpperCase());
        badge.getStyleClass().add("coming-soon-badge");
        badge.setPadding(new Insets(4, 12, 4, 12));

        Label title = new Label("Coming Soon");
        title.getStyleClass().add("coming-soon-title");

        Label subtitle = new Label("Stay tuned for future updates.");
        subtitle.getStyleClass().add("coming-soon-subtitle");

        card.getChildren().addAll(icon, badge, title, subtitle);
        root.getChildren().add(card);
    }

    public StackPane getView() { return root; }
}

package org.Core.UI.LobbyScreens.Center;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

/**
 * Slim quick-preferences strip shown just under the navbar (board theme /
 * piece set / sound, plus a shortcut into the existing profile/settings
 * card). Purely presentational — it does not introduce new board/piece/audio
 * switching behavior, since that lives with the chessboard and is out of
 * scope here.
 */
public class PreferencesBar {

    private final HBox root = new HBox(6);

    public PreferencesBar(LobbyController controller) {
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 14, 8, 14));
        root.getStyleClass().add("prefs-bar");

        root.getChildren().addAll(
                buildChip("#8a5a35", "Walnut"),
                buildChip("♞", "Classic"),
                buildChip("🔊", "SFX On")
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);

        Button gear = new Button("⚙");
        gear.setPrefSize(28, 28);
        gear.getStyleClass().add("prefs-gear-btn");
        gear.setOnAction(e -> controller.onProfileClicked());
        root.getChildren().add(gear);
    }

    private HBox buildChip(String swatchColorOrGlyph, String label) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(5, 9, 5, 8));
        chip.getStyleClass().add("prefs-chip");

        if (swatchColorOrGlyph.startsWith("#")) {
            Region swatch = new Region();
            swatch.setPrefSize(12, 12);
            swatch.getStyleClass().add("prefs-swatch");
            swatch.setStyle("-fx-background-color: " + swatchColorOrGlyph + ";");
            chip.getChildren().add(swatch);
        } else {
            Label icon = new Label(swatchColorOrGlyph);
            icon.getStyleClass().add("prefs-chip-icon");
            chip.getChildren().add(icon);
        }

        Label text = new Label(label);
        text.getStyleClass().add("prefs-chip-text");
        chip.getChildren().add(text);
        return chip;
    }

    public HBox getView() { return root; }
}

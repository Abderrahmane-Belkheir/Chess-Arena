package org.Core.UI.LobbyScreens.Center;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.IntConsumer;

/**
 * Bottom navigation strip: Arena (the existing lobby content) plus
 * Rankings / Puzzles / Clubs, which are presented as "Coming soon" panels.
 * Purely a presentation-level tab switch — no new backend/business logic.
 */
public class BottomNav {

    public enum Section { ARENA, RANKINGS, PUZZLES, CLUBS }

    private final HBox root = new HBox(0);
    private final VBox arenaItem     = buildItem("♛", "Arena");
    private final VBox rankingsItem  = buildItem("★", "Rankings");
    private final VBox puzzlesItem   = buildItem("♟", "Puzzles");
    private final VBox clubsItem     = buildItem("♜", "Clubs");

    private Section current = Section.ARENA;

    public BottomNav(IntConsumer onSectionSelected) {
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("bottom-nav");

        wire(arenaItem,    Section.ARENA,    onSectionSelected);
        wire(rankingsItem, Section.RANKINGS, onSectionSelected);
        wire(puzzlesItem,  Section.PUZZLES,  onSectionSelected);
        wire(clubsItem,    Section.CLUBS,    onSectionSelected);

        for (VBox item : new VBox[]{arenaItem, rankingsItem, puzzlesItem, clubsItem}) {
            HBox.setHgrow(item, Priority.ALWAYS);
        }
        root.getChildren().addAll(arenaItem, rankingsItem, puzzlesItem, clubsItem);

        refreshActiveState();
    }

    private void wire(VBox item, Section section, IntConsumer onSectionSelected) {
        item.setOnMouseClicked(e -> {
            current = section;
            refreshActiveState();
            onSectionSelected.accept(section.ordinal());
        });
    }

    private void refreshActiveState() {
        setActive(arenaItem,    current == Section.ARENA);
        setActive(rankingsItem, current == Section.RANKINGS);
        setActive(puzzlesItem,  current == Section.PUZZLES);
        setActive(clubsItem,    current == Section.CLUBS);
    }

    private void setActive(VBox item, boolean active) {
        var icon = (javafx.scene.control.Label) item.getChildren().get(0);
        var label = (javafx.scene.control.Label) item.getChildren().get(1);
        if (active) {
            if (!icon.getStyleClass().contains("active")) icon.getStyleClass().add("active");
            if (!label.getStyleClass().contains("active")) label.getStyleClass().add("active");
        } else {
            icon.getStyleClass().remove("active");
            label.getStyleClass().remove("active");
        }
    }

    private static VBox buildItem(String glyph, String text) {
        VBox item = new VBox(2);
        item.setAlignment(Pos.CENTER);
        item.setPadding(new Insets(7, 6, 7, 6));
        item.setMaxWidth(Double.MAX_VALUE);
        item.getStyleClass().add("bottom-nav-item");
        item.setStyle("-fx-cursor: hand;");

        var icon = new javafx.scene.control.Label(glyph);
        icon.getStyleClass().add("bottom-nav-icon");
        var label = new javafx.scene.control.Label(text);
        label.getStyleClass().add("bottom-nav-item");

        item.getChildren().addAll(icon, label);
        return item;
    }

    public HBox getView() { return root; }

    public Section getCurrentSection() { return current; }

    /** Programmatic selection — updates the highlight but does NOT invoke the selection callback. */
    public void select(Section s) {
        current = s;
        refreshActiveState();
    }
}

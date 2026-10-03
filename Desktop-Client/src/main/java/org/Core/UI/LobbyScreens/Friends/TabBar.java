package org.Core.UI.LobbyScreens.Friends;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import java.util.function.IntConsumer;

public class TabBar {

    private final HBox root = new HBox(6);

    private final Button inLobbyBtn  = build("In Lobby");
    private final Button inGameBtn   = build("In Game");
    private final Button offlineBtn  = build("Offline");
    private final Button requestsBtn = build("Requests");
    // Requests button sits in its own StackPane so RequestsSection's unseen
    // badge (see attachRequestsBadge) can be overlaid on its corner instead
    // of taking up its own slot in the row.
    private final StackPane requestsWrap = new StackPane(requestsBtn);

    private int currentTab = 0;
    private int pendingCount = 0;

    public TabBar(IntConsumer onTabSelected) {
        inLobbyBtn.setOnAction(e  -> select(0, onTabSelected));
        inGameBtn.setOnAction(e   -> select(1, onTabSelected));
        offlineBtn.setOnAction(e  -> select(2, onTabSelected));
        requestsBtn.setOnAction(e -> select(3, onTabSelected));

        HBox.setHgrow(inLobbyBtn,  Priority.ALWAYS);
        HBox.setHgrow(inGameBtn,   Priority.ALWAYS);
        HBox.setHgrow(offlineBtn,  Priority.ALWAYS);
        HBox.setHgrow(requestsWrap, Priority.ALWAYS);
        requestsBtn.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().addAll(inLobbyBtn, inGameBtn, offlineBtn, requestsWrap);

        refreshActiveState();
    }

    /**
     * Overlays the given badge on the Requests tab's top-right corner. The
     * badge node itself (RequestsSection.getUnseenBadge()) already controls
     * its own visibility/text — this just gives it somewhere in the scene
     * graph to actually render, which it had nowhere to do before.
     */
    public void attachRequestsBadge(Node badge) {
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        badge.setTranslateX(6);
        badge.setTranslateY(-6);
        requestsWrap.getChildren().add(badge);
    }

    public HBox getView() { return root; }

    public int getCurrentTab() { return currentTab; }

    private void select(int tab, IntConsumer onTabSelected) {
        currentTab = tab;
        refreshActiveState();
        onTabSelected.accept(tab);
    }

    private void refreshActiveState() {
        setActive(inLobbyBtn, currentTab == 0);
        setActive(inGameBtn, currentTab == 1);
        setActive(offlineBtn, currentTab == 2);
        setActive(requestsBtn, currentTab == 3);
    }

    private void setActive(Button btn, boolean active) {
        if (active) {
            if (!btn.getStyleClass().contains("active")) btn.getStyleClass().add("active");
        } else {
            btn.getStyleClass().remove("active");
        }
    }

    public void setInLobbyCount(int count) {
        inLobbyBtn.setText("In Lobby " + count);
    }

    public void setInGameCount(int count) {
        inGameBtn.setText("In Game " + count);
    }

    public void setOfflineCount(int count) {
        offlineBtn.setText("Offline " + count);
    }

    /**
     * Unlike the other three tabs, Requests never shows a numeric count —
     * just highlights the label when there's at least one pending request.
     */
    public void setPendingCount(int count) {
        this.pendingCount = Math.max(0, count);
        if (pendingCount > 0) {
            if (!requestsBtn.getStyleClass().contains("tab-pending")) requestsBtn.getStyleClass().add("tab-pending");
        } else {
            requestsBtn.getStyleClass().remove("tab-pending");
        }
    }

    private static Button build(String label) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("tab-btn");
        return btn;
    }
}

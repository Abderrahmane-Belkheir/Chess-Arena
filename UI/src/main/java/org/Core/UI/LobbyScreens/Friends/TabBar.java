package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.IntConsumer;

public class TabBar {

    private final HBox root = new HBox(6);

    private final Button onlineBtn   = build("Online",   true);
    private final Button offlineBtn  = build("Offline",  false);
    private final Button requestsBtn = build("Requests", false);

    private int currentTab = 0;
    private int pendingCount = 0;

    public TabBar(IntConsumer onTabSelected) {
        onlineBtn.setOnAction(e   -> select(0, onTabSelected));
        offlineBtn.setOnAction(e  -> select(1, onTabSelected));
        requestsBtn.setOnAction(e -> select(2, onTabSelected));

        HBox.setHgrow(onlineBtn,   Priority.ALWAYS);
        HBox.setHgrow(offlineBtn,  Priority.ALWAYS);
        HBox.setHgrow(requestsBtn, Priority.ALWAYS);
        root.getChildren().addAll(onlineBtn, offlineBtn, requestsBtn);
    }

    public HBox getView() { return root; }

    public int getCurrentTab() { return currentTab; }

    private void select(int tab, IntConsumer onTabSelected) {
        currentTab = tab;
        refreshStyles();
        onTabSelected.accept(tab);
    }

    private void refreshStyles() {
        onlineBtn.setStyle(currentTab == 0   ? Styles.activeTabStyle() : Styles.inactiveTabStyle());
        offlineBtn.setStyle(currentTab == 1  ? Styles.activeTabStyle() : Styles.inactiveTabStyle());
        requestsBtn.setStyle(currentTab == 2 ? Styles.activeTabStyle() : Styles.inactiveTabStyle());
        applyBadgeStyleIfNeeded();
    }

    public void setOnlineCount(int count) {
        onlineBtn.setText("Online " + count);
    }

    public void setOfflineCount(int count, boolean hasMore) {
        offlineBtn.setText("Offline " + count + (hasMore ? "+" : ""));
    }

    /** Sets the absolute pending count and refreshes the Requests label/badge. */
    public void setPendingCount(int count) {
        this.pendingCount = Math.max(0, count);
        if (pendingCount > 0) {
            requestsBtn.setText("Requests " + pendingCount);
        } else {
            requestsBtn.setText("Requests");
        }
        applyBadgeStyleIfNeeded();
    }

    private void applyBadgeStyleIfNeeded() {
        String base = currentTab == 2 ? Styles.activeTabStyle() : Styles.inactiveTabStyle();
        requestsBtn.setStyle(pendingCount > 0 ? base + "-fx-text-fill: #e05555;" : base);
    }

    private static Button build(String label, boolean active) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(active ? Styles.activeTabStyle() : Styles.inactiveTabStyle());
        return btn;
    }
}
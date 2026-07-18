package org.Core.UI.LobbyScreens.Friends;


import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

/**
 * Top-level Friends sidebar. Pure orchestration/layout now — all fetching,
 * pagination, error/empty handling, and row rendering live in the
 * friends.section / friends.row / friends.search / friends.ui packages.
 *
 * Owns pendingCount itself (the Requests badge number), since it's shared
 * state that both RequestsSection (loading new requests) and
 * PlayerSearchBar/SearchResultCard (accepting/rejecting from search) can
 * affect.
 */
public class FriendsPanel {

    private final VBox root = new VBox(0);
    private final VBox listContainer = new VBox(0);
    private ScrollPane scroll;

    private final TabBar tabBar;
    private final PlayerSearchBar searchBar;

    private final OnlineFriendsSection  online;
    private final OfflineFriendsSection offline;
    private final RequestsSection       requests;

    private int pendingCount = 0;

    public FriendsPanel(FriendShipClient friendShipClient, LobbyController controller) {
        root.setPrefWidth(264);
        root.setMinWidth(264);
        root.setMaxWidth(264);
        root.setStyle("""
            -fx-background-color: #111111;
            -fx-border-color: transparent #1e1e1e transparent transparent;
            -fx-border-width: 1;
        """);

        tabBar = new TabBar(this::switchTab);

        online   = new OnlineFriendsSection(friendShipClient,  controller,
                tabBar::setOnlineCount);
        offline  = new OfflineFriendsSection(friendShipClient,  controller,
                tabBar::setOfflineCount);
        requests = new RequestsSection(friendShipClient,
                this::adjustPendingCount,
                this::showGlobalError);

        searchBar = new PlayerSearchBar(friendShipClient, this::adjustPendingCount);

        var tabsRow = tabBar.getView();
        tabsRow.setPadding(new Insets(10, 14, 8, 14));

        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color: #1e1e1e;");

        listContainer.setFillWidth(true);
        listContainer.getChildren().add(online.getList());

        scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        scroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() > 0.85) {
                loadMoreForActiveTab();
            }
        });

        root.getChildren().addAll(
                searchBar.getFieldView(),
                searchBar.getResultsView(),
                tabsRow,
                sep,
                scroll
        );

        online.loadInitial();
    }

    public VBox getView() { return root; }

    private void switchTab(int tab) {
        listContainer.getChildren().clear();
        listContainer.getChildren().add(switch (tab) {
            case 1  -> offline.getList();
            case 2  -> requests.getList();
            default -> online.getList();
        });

        scroll.setVvalue(0);
        switch (tab) {
            case 1  -> offline.loadInitial();
            case 2  -> requests.loadInitial();
            default -> online.loadInitial();
        }
    }

    private void loadMoreForActiveTab() {
        switch (tabBar.getCurrentTab()) {
            case 1  -> offline.loadMore();
            case 2  -> requests.loadMore();
            default -> online.loadMore();
        }
    }

    /** Applied by RequestsSection (page loads) and SearchResultCard (accept/reject). */
    private void adjustPendingCount(int delta) {
        pendingCount = Math.max(0, pendingCount + delta);
        tabBar.setPendingCount(pendingCount);
    }

    private void showGlobalError(String message) {
        // Errors from RequestsSection row actions surface the same way the
        // original showError() did for the search dropdown. Reuse it here
        // too rather than introducing a second toast mechanism.
        // Wire this up to whatever app-wide error/toast channel you use, e.g.:
        // controller.showToast(message);
    }
}
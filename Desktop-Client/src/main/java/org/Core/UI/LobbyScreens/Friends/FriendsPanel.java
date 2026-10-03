package org.Core.UI.LobbyScreens.Friends;


import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.Core.Config.GameEventPublisher;
import org.Core.Social.DTO.FriendsList;
import org.Core.Social.Events.FriendAdded;
import org.Core.Social.Events.FriendRemoved;
import org.Core.Social.Events.FriendStatus;
import org.Core.Social.Events.Invitation;
import org.Core.Social.Events.InvitationRemoved;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.util.List;

/**
 * Top-level Friends sidebar. Pure orchestration/layout now — all fetching,
 * pagination, error/empty handling, and row rendering live in the
 * friends.section / friends.row / friends.search / friends.ui packages.
 *
 * Owns pendingCount itself (the Requests badge number), since it's shared
 * state that both RequestsSection (loading new requests) and
 * PlayerSearchBar/SearchResultCard (accepting/rejecting from search) can
 * affect.
 *
 * Search is collapsed behind a toggle icon rather than an always-open bar:
 * clicking it swaps the In Lobby/In Game/Offline/Requests list out for the
 * search field plus an empty placeholder, which stays up until a search is
 * actually confirmed (Enter) and results — or a "not found"/error state —
 * replace it.
 */
public class FriendsPanel {

    private final VBox root = new VBox(0);
    private final VBox listContainer = new VBox(8);
    private ScrollPane scroll;

    private final TabBar tabBar;
    private final PlayerSearchBar searchBar;
    private final Button searchToggle = new Button("⌕");
    private final VBox listSection = new VBox(0);
    private final VBox searchSection = new VBox(0);
    private final Node searchEmptyState = FooterViews.buildEmptyState("Search for a player by ID");

    private final InLobbyFriendsSection inLobby;
    private final InGameFriendsSection  inGame;
    private final OfflineFriendsSection offline;
    private final RequestsSection       requests;

    private int pendingCount = 0;
    private boolean searchMode = false;

    public FriendsPanel(FriendShipClient friendShipClient, LobbyController controller, GameEventPublisher gameEventPublisher) {
        // Friends and Recent Games share one slot in the Lobby, inside a card
        // whose header IS the Friends/Recent Games toggle (see LobbyView) —
        // this panel itself stays chrome-less (no border/background of its
        // own) so it reads as part of that one card, not a nested card.
        root.setMinHeight(150);

        // ── Header ────────────────────────────────────────────────────
        // Just a search toggle + "View all" — the toggle above already
        // labels this as "Friends", so repeating the title here would be
        // redundant.
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(11, 12, 0, 12));

        searchToggle.getStyleClass().add("search-toggle-btn");
        searchToggle.setOnAction(e -> toggleSearchMode());

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label viewAll = new Label("View all");
        viewAll.getStyleClass().add("view-all-link");

        header.getChildren().addAll(searchToggle, headerSpacer, viewAll);

        tabBar = new TabBar(this::switchTab);

        inLobby  = new InLobbyFriendsSection(friendShipClient, controller,
                tabBar::setInLobbyCount, this::removeFriendFromAnySection);
        inGame   = new InGameFriendsSection(friendShipClient, controller,
                tabBar::setInGameCount, this::removeFriendFromAnySection);
        offline  = new OfflineFriendsSection(friendShipClient,  controller,
                tabBar::setOfflineCount, this::removeFriendFromAnySection);
        requests = new RequestsSection(friendShipClient,
                this::adjustPendingCount,
                this::showGlobalError);
        tabBar.attachRequestsBadge(requests.getUnseenBadge());

        // While search is active: hide the "search for a player" placeholder
        // as soon as real results (or a loading/not-found/error state)
        // appear, and bring it back if those are cleared but search is
        // still open (e.g. the query was deleted back to blank).
        searchBar = new PlayerSearchBar(friendShipClient, this::adjustPendingCount,
                requests::removeByPublicId,
                this::hideSearchEmptyState,
                () -> { if (searchMode) showSearchEmptyState(); });

        var tabsRow = tabBar.getView();
        tabsRow.setPadding(new Insets(8, 10, 7, 10));

        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.getStyleClass().add("panel-separator");

        listContainer.setFillWidth(true);
        listContainer.setPadding(new Insets(10, 12, 12, 12));
        listContainer.getChildren().add(inLobby.getList());

        scroll = new ScrollPane(listContainer);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        listSection.getChildren().addAll(tabsRow, sep, scroll);
        VBox.setVgrow(listSection, Priority.ALWAYS);

        searchSection.getChildren().addAll(searchBar.getFieldView(), searchEmptyState, searchBar.getResultsView());
        VBox.setVgrow(searchSection, Priority.ALWAYS);
        searchSection.setVisible(false);
        searchSection.setManaged(false);
        searchEmptyState.setManaged(false);

        root.getChildren().addAll(header, listSection, searchSection);

        // Eager-load every tab up front, not just the one shown first.
        // InLobby/InGame/Offline all share one cached /social/friends call
        // (FriendShipClient.fetchFriendsList), so loading all three costs no
        // extra network round trip — only Requests adds a real second call,
        // which is worth it so live FriendStatus/Invitation updates never
        // land on a section that hasn't loaded yet and get silently dropped
        // (see FriendListSection/RequestsSection's isLoadedOnce() guards).
        inLobby.loadInitial();
        inGame.loadInitial();
        offline.loadInitial();
        requests.loadInitial();

        gameEventPublisher.register(this);
    }

    public VBox getView() { return root; }

    private void toggleSearchMode() {
        searchMode = !searchMode;
        searchToggle.getStyleClass().remove("active");
        if (searchMode) {
            searchToggle.getStyleClass().add("active");
            listSection.setVisible(false);
            listSection.setManaged(false);
            searchSection.setVisible(true);
            searchSection.setManaged(true);
            showSearchEmptyState();
            searchBar.focusField();
        } else {
            searchSection.setVisible(false);
            searchSection.setManaged(false);
            listSection.setVisible(true);
            listSection.setManaged(true);
            searchBar.reset();
            hideSearchEmptyState();
        }
    }

    private void showSearchEmptyState() {
        searchEmptyState.setVisible(true);
        searchEmptyState.setManaged(true);
    }

    private void hideSearchEmptyState() {
        searchEmptyState.setVisible(false);
        searchEmptyState.setManaged(false);
    }

    private void switchTab(int tab) {
        listContainer.getChildren().clear();
        listContainer.getChildren().add(switch (tab) {
            case 1  -> inGame.getList();
            case 2  -> offline.getList();
            case 3  -> requests.getList();
            default -> inLobby.getList();
        });

        scroll.setVvalue(0);
        switch (tab) {
            case 1  -> inGame.loadInitial();
            case 2  -> offline.loadInitial();
            case 3  -> { requests.loadInitial(); requests.resetUnseen(); }
            default -> inLobby.loadInitial();
        }
    }

    /** Applied by RequestsSection (loads) and SearchResultCard (accept/reject). */
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

    // Both handlers below arrive on the STOMP client's own callback thread
    // (GameRealtimeGatewayStub.handleFrame -> appEvents.post(...) is a plain
    // Guava EventBus.post(), which invokes @Subscribe methods synchronously
    // on whatever thread posted the event — never the FX thread). Every
    // mutation they trigger touches live scene-graph ObservableLists, so the
    // whole handler body has to run on the FX thread, same as
    // GameSessionService's Platform.runLater(...)-wrapped handlers.

    @Subscribe
    public void handleInvitation(Invitation invitation){
        Platform.runLater(() -> {
            requests.addInvitation(invitation.getEntry());
            // invite() pushes to both sides — the recipient (incoming) and
            // the sender, so their own outgoing request shows up live too.
            // Only the recipient should see the unseen bubble bump.
            if (invitation.getEntry().isIncoming()) {
                requests.increaseUnseen();
            }
        });
    }

    /**
     * The other side of a pending request declined it or unsent it — remove
     * the matching entry (by the acting user's publicId) from our Requests
     * list.
     */
    @Subscribe
    public void handleInvitationRemoved(InvitationRemoved invitationRemoved){
        Platform.runLater(() -> {
            requests.removeByPublicId(invitationRemoved.getPublicId());
            requests.decreaseUnseen();
        });
    }

    @Subscribe
    public void handleFriendRemoved(FriendRemoved friendRemoved){
        Platform.runLater(() -> removeFriendFromAnySection(friendRemoved.getPublicId()));
    }

    /**
     * A new friendship was formed (the other side accepted our request, or
     * we just accepted theirs) — insert the entry into whichever section
     * matches its current status, and drop the now-stale request tied to
     * that same publicId from the Requests list, on both the sender's and
     * the accepter's side (they each get their own FriendAdded push).
     */
    @Subscribe
    public void handleFriendAdded(FriendAdded friendAdded){
        Platform.runLater(() -> {
            FriendsList.FriendEntry entry = friendAdded.getEntry();
            switch (entry.getStatus()) {
                case InLobby -> inLobby.addFriend(entry, FriendsList.Status.InLobby);
                case InGame -> inGame.addFriend(entry, FriendsList.Status.InGame);
                case Offline -> offline.addFriend(entry, FriendsList.Status.Offline);
            }
            requests.removeByPublicId(entry.getId());
        });
    }

    /**
     * Tries each of the three friend-status sections in turn and removes the
     * friend from whichever one actually has them. Shared by the live
     * FriendRemoved push (the other side removed us) and FriendCard's own
     * "Remove friend" button (we removed them) — same removal, two triggers.
     */
    private void removeFriendFromAnySection(int publicId) {
        for (FriendListSection section : List.of(inLobby, inGame, offline)) {
            if (section.removeFriend(publicId) != null) break;
        }
    }

    @Subscribe
    public void handleStatusChange(FriendStatus friendStatus){
        Platform.runLater(() -> {
            FriendsList.FriendEntry entry = null;
            for (FriendListSection section : List.of(inLobby, inGame, offline)) {
                entry = section.removeFriend(friendStatus.getPublicId());
                if (entry != null) break;
            }

            if (entry == null) return;

            switch (friendStatus.getNewStatus()) {
                case InGame -> inGame.addFriend(entry, FriendsList.Status.InGame);
                case InLobby -> {
                    // newElo is only ever a real value when this InLobby
                    // transition came from a ranked game actually ending
                    // (GameOverHandler) — every other trigger (e.g. simply
                    // coming online) sends 0 as "not applicable", which must
                    // NOT overwrite the friend's real elo.
                    if (friendStatus.getNewElo() > 0) {
                        entry.setElo(friendStatus.getNewElo());
                    }
                    inLobby.addFriend(entry, FriendsList.Status.InLobby);
                }
                case Offline -> offline.addFriend(entry, FriendsList.Status.Offline);
            }
        });
    }

}

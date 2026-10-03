package org.Core.UI.LobbyScreens.Lobby;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.Core.Config.GameEventPublisher;
import org.Core.Game.History.GameHistoryClient;
import org.Core.Social.Events.Challenge;
import org.Core.Social.FriendShipClient;
import org.Core.UI.Game.RecentGames;
import org.Core.UI.LobbyScreens.Center.BottomNav;
import org.Core.UI.LobbyScreens.Center.ComingSoonPanel;
import org.Core.UI.LobbyScreens.Center.HeroPanel;
import org.Core.UI.LobbyScreens.Center.PreferencesBar;
import org.Core.UI.LobbyScreens.Friends.Avatar;
import org.Core.UI.LobbyScreens.Friends.FriendsPanel;
import org.Core.UI.LobbyScreens.Profile.NavBar;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.Core.UI.Shared.LobbySurfaceMetrics.SURFACE_HEIGHT_FRACTION;
import static org.Core.UI.Shared.LobbySurfaceMetrics.SURFACE_PADDING;
import static org.Core.UI.Shared.LobbySurfaceMetrics.SURFACE_WIDTH;

/**
 * The Lobby surface: a single vertically-stacked column (preferences,
 * hero/play, a Friends/Recent Games toggle) inside a bounded, centered
 * rectangle — matching the reference design's composition — floating in the
 * same warm wood-toned environment used by the chessboard. The reference is
 * a compact mobile-proportioned card, not a wide desktop dashboard, so the
 * surface is held to a fixed, narrow width (a "phone held up inside the
 * desktop window") while its height tracks the window, letting whichever of
 * Friends/Recent Games is selected fill the full view.
 */
public class LobbyView {

    // Sizing lives in LobbySurfaceMetrics (shared with any other screen that
    // transitions directly to/from the Lobby, e.g. MatchmakingView) so they
    // can't drift apart and visibly resize the surface on transition.

    private final StackPane root = new StackPane();
    @Getter
    private final StackPane overlay = new StackPane();
    private final NavBar navBar;
    private final FriendsPanel friendsPanel;
    private final FriendShipClient friendShipClient;
    private final HeroPanel heroPanel;
    private final RecentGames recentGames;
    private final StackPane contentHolder = new StackPane();
    private final StackPane socialHolder = new StackPane();
    private final VBox arenaBody;
    private final BorderPane layout = new BorderPane();
    private final VBox centerColumn = new VBox();
    private BottomNav bottomNav;
    private PreferencesBar preferencesBar;
    private StackPane activeGame;

    public LobbyView(LobbyController controller, FriendShipClient friendShipClient, GameHistoryClient gameHistoryClient,
                      GameEventPublisher gameEventPublisher) {

        root.getStylesheets().add(
                getClass().getResource("/css/lobby.css").toExternalForm());
        root.getStyleClass().add("lobby-environment");

        layout.getStyleClass().add("lobby-surface-frame");
        layout.maxWidthProperty().bind(Bindings.min(SURFACE_WIDTH, root.widthProperty().multiply(0.92)));
        // Horizontal-only gutter between the surface's own border and the
        // fixed-width content inside it (top/bottom unchanged) — this is
        // what makes the surface wider without resizing any inner card.
        layout.setPadding(new Insets(0, SURFACE_PADDING, 0, SURFACE_PADDING));
        // The surface takes most of the window's height; the hero card and
        // toggle keep their natural size and the active Friends/Recent Games
        // panel grows to fill the rest (see the vgrow calls below), so it
        // reads as a full-height view rather than a small boxed-in list.
        layout.maxHeightProperty().bind(root.heightProperty().multiply(SURFACE_HEIGHT_FRACTION));

        navBar       = new NavBar(controller);
        this.friendShipClient = friendShipClient;
        friendsPanel = new FriendsPanel(friendShipClient, controller, gameEventPublisher);
        heroPanel    = new HeroPanel(controller);
        recentGames  = new RecentGames(controller, gameHistoryClient);

        // A gap above the navbar (rather than flush against the surface's own
        // top edge) matches how BottomNav is inset from the bottom edge, so
        // it reads as its own rounded card floating inside the surface.
        BorderPane.setMargin(navBar.getView(), new Insets(10, 0, 0, 0));
        layout.setTop(navBar.getView());

        // Friends and Recent Games share one slot, switched by a toggle that
        // is itself the card's header (flush, same rounded corners, a hairline
        // separator below it) rather than two pill buttons floating above a
        // separate card — the toggle belongs to this card, not to whichever
        // panel happens to be showing inside it.
        Button friendsToggle = new Button("Friends");
        Button gamesToggle   = new Button("Recent Games");
        friendsToggle.getStyleClass().addAll("social-tab", "social-tab-left", "active");
        gamesToggle.getStyleClass().addAll("social-tab", "social-tab-right");
        friendsToggle.setMaxWidth(Double.MAX_VALUE);
        gamesToggle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(friendsToggle, Priority.ALWAYS);
        HBox.setHgrow(gamesToggle, Priority.ALWAYS);
        HBox socialToggle = new HBox(friendsToggle, gamesToggle);
        socialToggle.getStyleClass().add("social-toggle");

        Region socialSeparator = new Region();
        socialSeparator.setPrefHeight(1);
        socialSeparator.setMaxWidth(Double.MAX_VALUE);
        socialSeparator.getStyleClass().add("panel-separator");

        socialHolder.getChildren().add(friendsPanel.getView());
        friendsToggle.setOnAction(e -> {
            friendsToggle.getStyleClass().add("active");
            gamesToggle.getStyleClass().remove("active");
            socialHolder.getChildren().setAll(friendsPanel.getView());
        });
        gamesToggle.setOnAction(e -> {
            gamesToggle.getStyleClass().add("active");
            friendsToggle.getStyleClass().remove("active");
            socialHolder.getChildren().setAll(recentGames.getView());
            recentGames.loadIfNeeded();
        });

        // The toggle + whichever panel is active are one visual card (same
        // "compact-card" chrome the panels used to carry individually), so
        // they read as a single unit rather than a header floating above a
        // separate box.
        VBox socialCard = new VBox(socialToggle, socialSeparator, socialHolder);
        socialCard.getStyleClass().add("compact-card");
        VBox.setVgrow(socialHolder, Priority.ALWAYS);

        // No overall Lobby scroll: the hero card and toggle keep their
        // natural size, and the active Friends/Recent Games panel grows to
        // fill whatever's left — a full-height view for whichever one is
        // selected, rather than a small boxed-in list — relying on its own
        // internal ScrollPane once its own content overflows that space.
        arenaBody = new VBox(8);
        arenaBody.getChildren().addAll(
                heroPanel.getView(),
                socialCard
        );
        VBox.setVgrow(socialCard, Priority.ALWAYS);

        preferencesBar = new PreferencesBar(controller);

        contentHolder.getChildren().add(arenaBody);

        // Placed directly as the BorderPane's center (not wrapped/re-centered
        // in an extra StackPane) so it spans the same width as the nav bar
        // and bottom nav, edge to edge with the surface.
        centerColumn.getChildren().addAll(preferencesBar.getView(), contentHolder);
        VBox.setVgrow(contentHolder, Priority.ALWAYS);
        layout.setCenter(centerColumn);

        bottomNav = new BottomNav(this::onSectionSelected);
        // A real gap above (from whatever's active in contentHolder) and
        // below (from the surface's own bottom edge) — combined with its own
        // fully-rounded border in CSS, this reads as an independent floating
        // card rather than a footer bar fused to the surface's bottom edge.
        BorderPane.setMargin(bottomNav.getView(), new Insets(10, 0, 14, 0));
        layout.setBottom(bottomNav.getView());

        overlay.setVisible(false);
        overlay.setPickOnBounds(false);
        root.getChildren().addAll(layout, overlay);
        gameEventPublisher.register(this);
    }

    private void onSectionSelected(int sectionOrdinal) {
        BottomNav.Section section = BottomNav.Section.values()[sectionOrdinal];
        contentHolder.getChildren().setAll(switch (section) {
            case ARENA    -> activeGame != null ? activeGame : arenaBody;
            case RANKINGS -> new ComingSoonPanel("★", "Rankings").getView();
            case PUZZLES  -> new ComingSoonPanel("♟", "Puzzles").getView();
            case CLUBS    -> new ComingSoonPanel("♜", "Clubs").getView();
        });
    }

    /**
     * Swaps the Arena slot to show the live game board and strips the rest of
     * the Lobby's chrome (NavBar, PreferencesBar, BottomNav) so only the
     * board itself — plus whatever GameView renders around it — occupies the
     * card while a game is active.
     */
    public void showGame(StackPane gameContent) {
        activeGame = gameContent;
        bottomNav.select(BottomNav.Section.ARENA);
        layout.setTop(null);
        layout.setBottom(null);
        centerColumn.getChildren().remove(preferencesBar.getView());
        contentHolder.getChildren().setAll(gameContent);
    }

    /** Restores the Lobby's normal chrome and reverts the Arena slot back to the Hero/Friends content. */
    public void clearGame() {
        activeGame = null;
        layout.setTop(navBar.getView());
        layout.setBottom(bottomNav.getView());
        if (!centerColumn.getChildren().contains(preferencesBar.getView())) {
            centerColumn.getChildren().add(0, preferencesBar.getView());
        }
        if (bottomNav.getCurrentSection() == BottomNav.Section.ARENA) {
            contentHolder.getChildren().setAll(arenaBody);
        }
    }

    public void setUser(String username, int elo, String avatarInitials, String avatarUrl) {
        navBar.setUser(username, elo, avatarInitials, avatarUrl, Avatar.colorFromName(username));
    }

    public void setRecentGames(java.util.List<RecentGames.GameEntry> games) {
        recentGames.setGames(games);
    }

    public StackPane getView() { return root; }

    @Subscribe
    public void handleChallenge(Challenge challenge){
        Platform.runLater(() -> new ChallengeRequestCard(overlay, challenge,
                id -> runChallengeCall(() -> friendShipClient.acceptChallenge(id)),
                id -> runChallengeCall(() -> friendShipClient.rejectChallenge(id)),
                c -> runChallengeCall(() -> friendShipClient.rejectChallenge(c.getPublicId()))));
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException, InterruptedException;
    }

    private void runChallengeCall(ThrowingRunnable call) {
        CompletableFuture.runAsync(() -> {
            try {
                call.run();
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

}

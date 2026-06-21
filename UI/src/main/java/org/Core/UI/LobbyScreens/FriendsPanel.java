package org.Core.UI.LobbyScreens;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.Core.Social.DTO.UserSummary;
import org.Core.Social.FriendShipClient;
import org.Core.Social.DTO.FriendsPage.FriendEntry;
import org.Core.Social.DTO.InvitationsPage.InvitationEntry;
import org.Core.Social.DTO.FriendsPage.Status;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class FriendsPanel {



    private static final class SectionState<T> {
        final VBox    list  = new VBox(0);
        final VBox    items = new VBox(0);
        String  cursor   = null;
        boolean hasMore  = true;
        boolean loading  = false;
        boolean loadedOnce = false;
    }

    private final SectionState<FriendEntry>     onlineState   = new SectionState<>();
    private final SectionState<FriendEntry>     offlineState  = new SectionState<>();
    private final SectionState<InvitationEntry> requestsState = new SectionState<>();


    private final VBox      root              = new VBox(0);
    private final VBox      listContainer     = new VBox(0);
    private final VBox      searchResults     = new VBox(0);

    private final TextField searchField;
    private final LobbyController  controller;
    private final FriendShipClient friendShipClient;

    private Button onlineTabBtn;
    private Button offlineTabBtn;
    private Button requestsTabBtn;
    private int    currentTab   = 0;
    private int    pendingCount = 0;

    private ScrollPane scroll;


    public FriendsPanel(FriendShipClient friendShipClient, LobbyController controller) {
        this.controller       = controller;
        this.friendShipClient = friendShipClient;

        root.setPrefWidth(264);
        root.setMinWidth(264);
        root.setMaxWidth(264);
        root.setStyle("""
            -fx-background-color: #111111;
            -fx-border-color: transparent #1e1e1e transparent transparent;
            -fx-border-width: 1;
        """);


        HBox searchWrap = new HBox(8);
        searchWrap.setAlignment(Pos.CENTER_LEFT);
        searchWrap.setPadding(new Insets(12, 14, 12, 14));
        searchWrap.setStyle("""
            -fx-border-color: transparent transparent #1a1a1a transparent;
            -fx-border-width: 1;
        """);

        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(7, 10, 7, 10));
        searchBox.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-background-radius: 8;
            -fx-border-color: #252525;
            -fx-border-radius: 8;
            -fx-border-width: 1;
        """);
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        Label searchIcon = new Label("⌕");
        searchIcon.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");

        searchField = new TextField();
        searchField.setPromptText("Enter player ID...");
        searchField.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #e0e0e0;
            -fx-prompt-text-fill: #444444;
            -fx-font-size: 12px;
            -fx-border-color: transparent;
            -fx-padding: 0;
        """);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, searchField);
        searchWrap.getChildren().add(searchBox);

        searchField.focusedProperty().addListener((obs, old, focused) ->
                searchBox.setStyle(searchBox.getStyle().replace(
                        focused ? "#252525" : "#81b64c",
                        focused ? "#81b64c" : "#252525")));

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER)
                handleSearch(searchField.getText().trim());
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                searchField.clear();
                hideSearchResults();
            }
        });

        searchField.textProperty().addListener((obs, old, text) -> {
            if (text.isBlank()) hideSearchResults();
        });


        searchResults.setStyle("""
            -fx-background-color: #161616;
            -fx-border-color: transparent transparent #1e1e1e transparent;
            -fx-border-width: 1;
        """);
        searchResults.setVisible(false);
        searchResults.setManaged(false);


        HBox tabs = new HBox(6);
        tabs.setPadding(new Insets(10, 14, 8, 14));

        onlineTabBtn   = buildTabBtn("Online",   true);
        offlineTabBtn  = buildTabBtn("Offline",  false);
        requestsTabBtn = buildTabBtn("Requests", false);

        onlineTabBtn.setOnAction(e   -> switchTab(0));
        offlineTabBtn.setOnAction(e  -> switchTab(1));
        requestsTabBtn.setOnAction(e -> switchTab(2));

        HBox.setHgrow(onlineTabBtn,   Priority.ALWAYS);
        HBox.setHgrow(offlineTabBtn,  Priority.ALWAYS);
        HBox.setHgrow(requestsTabBtn, Priority.ALWAYS);
        tabs.getChildren().addAll(onlineTabBtn, offlineTabBtn, requestsTabBtn);

        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setStyle("-fx-background-color: #1e1e1e;");


        onlineState.list.setFillWidth(true);
        offlineState.list.setFillWidth(true);
        requestsState.list.setFillWidth(true);
        onlineState.list.getChildren().add(onlineState.items);
        offlineState.list.getChildren().add(offlineState.items);
        requestsState.list.getChildren().add(requestsState.items);

        listContainer.setFillWidth(true);
        listContainer.getChildren().add(onlineState.list);

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

        root.getChildren().addAll(searchWrap, searchResults, tabs, sep, scroll);


        loadInitial(onlineState);
    }

    public VBox getView() { return root; }

    private void switchTab(int tab) {
        currentTab = tab;

        onlineTabBtn.setStyle(tab == 0   ? activeTabStyle() : inactiveTabStyle());
        offlineTabBtn.setStyle(tab == 1  ? activeTabStyle() : inactiveTabStyle());
        requestsTabBtn.setStyle(tab == 2 ? activeTabStyle() : inactiveTabStyle());

        if (tab != 2 && pendingCount > 0)
            requestsTabBtn.setStyle(inactiveTabStyle() + "-fx-text-fill: #e05555;");

        listContainer.getChildren().clear();
        listContainer.getChildren().add(switch (tab) {
            case 1  -> offlineState.list;
            case 2  -> requestsState.list;
            default -> onlineState.list;
        });

        scroll.setVvalue(0);
        switch (tab) {
            case 1  -> loadInitial(offlineState);
            case 2  -> loadInitial(requestsState);
            default -> loadInitial(onlineState);
        }
    }

    private void updateRequestsBadge() {
        String base = currentTab == 2 ? activeTabStyle() : inactiveTabStyle();
        if (pendingCount > 0) {
            requestsTabBtn.setText("Requests " + pendingCount);
            requestsTabBtn.setStyle(base + "-fx-text-fill: #e05555;");
        } else {
            requestsTabBtn.setText("Requests");
            requestsTabBtn.setStyle(base);
        }
    }

    private Button buildTabBtn(String label, boolean active) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(active ? activeTabStyle() : inactiveTabStyle());
        return btn;
    }

    private String activeTabStyle() {
        return """
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #f0f0f0;
            -fx-background-radius: 7;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 10px;
            -fx-font-weight: 700;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }

    private String inactiveTabStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #555555;
            -fx-background-radius: 7;
            -fx-border-color: #1e1e1e;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 10px;
            -fx-font-weight: 700;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }


    /** Loads the first page for a section, only if it hasn't been loaded yet. */
    private void loadInitial(SectionState<?> state) {
        if (state.loadedOnce) return;
        state.loadedOnce = true;
        fetchPage(state, true);
    }

    /** Called by the scroll listener; routes to whichever tab is currently visible. */
    private void loadMoreForActiveTab() {
        SectionState<?> active = switch (currentTab) {
            case 1  -> offlineState;
            case 2  -> requestsState;
            default -> onlineState;
        };
        if (!active.loading && active.hasMore) {
            fetchPage(active, false);
        }
    }

    private void fetchPage(SectionState<?> rawState, boolean isInitial) {
        if (rawState.loading || !rawState.hasMore) return;
        rawState.loading = true;

        if (rawState == onlineState) {
            fetchOnline(isInitial);
        } else if (rawState == offlineState) {
            fetchOffline(isInitial);
        } else {
            fetchRequests(isInitial);
        }
    }

    private void fetchOnline(boolean isInitial) {
        showSectionLoader(onlineState, isInitial);
        String cursor = onlineState.cursor;

        CompletableFuture
                .supplyAsync(() -> {
                            try {
                                return friendShipClient.fetchOnlineFriends(cursor);
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .thenAccept(page -> Platform.runLater(() -> {
                    onlineState.loading = false;
                    onlineState.cursor  = page.nextCursor();
                    onlineState.hasMore = page.hasMore();

                    page.items().forEach(f ->
                            onlineState.items.getChildren().add(buildFriendRow(f)));

                    onlineTabBtn.setText("Online " + onlineState.items.getChildren().size()
                            + (onlineState.hasMore ? "+" : ""));

                    if (page.items().isEmpty() && onlineState.hasMore) {
                        fetchPage(onlineState, false);
                        return;
                    }

                    hideSectionLoader(onlineState);

                    if (onlineState.items.getChildren().isEmpty() && !onlineState.hasMore) {
                        onlineState.items.getChildren().add(
                                buildEmptyState("No one's online right now"));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        onlineState.loading = false;
                        hideSectionLoader(onlineState);
                        showSectionError(onlineState, resolveError(ex), isInitial);
                    });
                    return null;
                });
    }

    private void fetchOffline(boolean isInitial) {
        showSectionLoader(offlineState, isInitial);
        String cursor = offlineState.cursor;

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return friendShipClient.fetchOfflineFriends(cursor);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(page -> Platform.runLater(() -> {
                    offlineState.loading = false;
                    offlineState.cursor  = page.nextCursor();
                    offlineState.hasMore = page.hasMore();

                    page.items().forEach(f ->
                            offlineState.items.getChildren().add(buildFriendRow(f)));

                    offlineTabBtn.setText("Offline " + offlineState.items.getChildren().size()
                            + (offlineState.hasMore ? "+" : ""));

                    if (page.items().isEmpty() && offlineState.hasMore) {
                        fetchPage(offlineState, false);
                        return;
                    }

                    hideSectionLoader(offlineState);

                    if (offlineState.items.getChildren().isEmpty() && !offlineState.hasMore) {
                        offlineState.items.getChildren().add(
                                buildEmptyState("No offline friends"));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        offlineState.loading = false;
                        hideSectionLoader(offlineState);
                        showSectionError(offlineState, resolveError(ex), isInitial);
                    });
                    return null;
                });
    }

    private void fetchRequests(boolean isInitial) {
        showSectionLoader(requestsState, isInitial);
        String cursor = requestsState.cursor;

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return friendShipClient.fetchRequests(cursor);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(page -> Platform.runLater(() -> {
                    requestsState.loading = false;
                    requestsState.cursor  = page.nextCursor();
                    requestsState.hasMore = page.hasMore();

                    long newIncoming = page.items().stream()
                            .filter(InvitationEntry::isIncoming).count();
                    pendingCount += newIncoming;
                    updateRequestsBadge();

                    page.items().forEach(r ->
                            requestsState.items.getChildren().add(buildRequestRow(r)));

                    if (page.items().isEmpty() && requestsState.hasMore) {
                        fetchPage(requestsState, false);
                        return;
                    }

                    hideSectionLoader(requestsState);

                    if (requestsState.items.getChildren().isEmpty() && !requestsState.hasMore) {
                        requestsState.items.getChildren().add(buildEmptyRequests());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        requestsState.loading = false;
                        hideSectionLoader(requestsState);
                        showSectionError(requestsState, resolveError(ex), isInitial);
                    });
                    return null;
                });
    }

    // ── Section loader / error footers ──────────────────────────────────
    //
    // These live as the last child of `state.list` (after `state.items`),
    // so they always sit below whatever rows have already loaded.

    private void showSectionLoader(SectionState<?> state, boolean isInitial) {
        removeFooter(state);

        if (isInitial) {
            VBox spinner = new VBox(8);
            spinner.setAlignment(Pos.CENTER);
            spinner.setPadding(new Insets(40, 14, 40, 14));
            Label dots = new Label("Loading...");
            dots.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");
            FadeTransition ft = new FadeTransition(Duration.millis(600), dots);
            ft.setFromValue(0.3);
            ft.setToValue(1.0);
            ft.setAutoReverse(true);
            ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
            ft.play();
            spinner.getChildren().add(dots);
            spinner.setUserData("footer");
            state.list.getChildren().add(spinner);
        } else {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER);
            row.setPadding(new Insets(14));
            Label dots = new Label("Loading more...");
            dots.setStyle("-fx-text-fill: #444444; -fx-font-size: 11px;");
            row.getChildren().add(dots);
            row.setUserData("footer");
            state.list.getChildren().add(row);
        }
    }

    private void hideSectionLoader(SectionState<?> state) {
        removeFooter(state);
    }

    private void showSectionError(SectionState<?> state, String message, boolean isInitial) {
        removeFooter(state);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        Label icon = new Label("⚠");
        icon.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 12px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        msg.setWrapText(true);
        row.getChildren().addAll(icon, msg);
        row.setUserData("footer");
        // Allow retrying by tapping the error row.
        row.setStyle("-fx-cursor: hand;");
        row.setOnMouseClicked(e -> fetchPage(state, isInitial));
        state.list.getChildren().add(row);
    }

    private void removeFooter(SectionState<?> state) {
        state.list.getChildren().removeIf(n -> "footer".equals(n.getUserData()));
    }

    // ── Search ────────────────────────────────────────────────────────

    private void handleSearch(String query) {
        if (query.isBlank()) {
            showError("Please enter a player ID.");
            return;
        }

        if (query.length() != 6) {
            showError("Player ID must be exactly 6 characters.");
            return;
        }

        showLoading();

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return friendShipClient.search(Integer.parseInt(query));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(result -> Platform.runLater(() -> {
                    if (result != null && result.getId() != 0)
                        showSearchResult(result);
                    else
                        showNotFound();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showError(resolveError(ex)));
                    return null;
                });
    }

    private void showLoading() {
        searchResults.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));

        Label dots = new Label("Searching...");
        dots.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

        FadeTransition ft = new FadeTransition(Duration.millis(600), dots);
        ft.setFromValue(0.3);
        ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
        ft.play();

        row.getChildren().add(dots);
        searchResults.getChildren().add(row);
        searchResults.setVisible(true);
        searchResults.setManaged(true);
    }

    private void showSearchResult(UserSummary result) {
        searchResults.getChildren().clear();
        searchResults.getChildren().add(buildSearchCard(result));
        revealDropdown();
    }

    private void showNotFound() {
        searchResults.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        Label icon = new Label("✕");
        icon.setStyle("-fx-text-fill: #e05555; -fx-font-size: 12px;");
        Label msg = new Label("No player found");
        msg.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        row.getChildren().addAll(icon, msg);

        searchResults.getChildren().add(row);
        revealDropdown();
    }

    private void showError(String message) {
        searchResults.getChildren().clear();

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        Label icon = new Label("⚠");
        icon.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 12px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        msg.setWrapText(true);
        row.getChildren().addAll(icon, msg);

        searchResults.getChildren().add(row);
        revealDropdown();
    }

    private void revealDropdown() {
        searchResults.setVisible(true);
        searchResults.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(150), searchResults);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideSearchResults() {
        searchResults.setVisible(false);
        searchResults.setManaged(false);
        searchResults.getChildren().clear();
    }

    // ── Search card ───────────────────────────────────────────────────

    private VBox buildSearchCard(UserSummary r) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("""
            -fx-background-color: #141414;
            -fx-border-color: transparent transparent #1e1e1e transparent;
            -fx-border-width: 1;
        """);

        // avatar + name + id
        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = buildAvatar(
                r.getAvatarUrl(),
                initials(r.getUsername()),
                avatarColorFromName(r.getUsername()));

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(r.getUsername());
        name.setStyle("""
            -fx-text-fill: #f0f0f0;
            -fx-font-size: 13px;
            -fx-font-weight: 700;
        """);

        Label publicId = new Label(String.valueOf(r.getId()));
        publicId.setStyle("""
            -fx-text-fill: #444444;
            -fx-font-size: 10px;
            -fx-font-weight: 600;
        """);
        info.getChildren().addAll(name, publicId);
        top.getChildren().addAll(avatar, info);

        // elo
        HBox eloRow = new HBox(6);
        eloRow.setAlignment(Pos.CENTER_LEFT);
        Label star = new Label("★");
        star.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        Label eloVal = new Label(r.getElo() + " ELO");
        eloVal.setStyle("""
            -fx-text-fill: #888888;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
        """);
        eloRow.getChildren().addAll(star, eloVal);

        card.getChildren().addAll(top, eloRow);

        // ── action area — based on relationship state ─────────────────

        if (r.getIsFriend()) {
            // ── already friends ───────────────────────────────────────
            Button btn = new Button("✓ Already friends");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle(friendBtnStyle());
            btn.setDisable(true);
            card.getChildren().add(btn);

        } else if (r.getInvitationStatus() == UserSummary.InvitationStatus.SENT) {
            // ── we sent a request → Unsend ────────────────────────────
            Button unsendBtn = new Button("✕ Unsend request");
            unsendBtn.setMaxWidth(Double.MAX_VALUE);
            unsendBtn.setStyle(pendingBtnStyle());
            unsendBtn.setOnMouseEntered(e -> unsendBtn.setOpacity(0.80));
            unsendBtn.setOnMouseExited(e  -> unsendBtn.setOpacity(1.0));
            unsendBtn.setOnAction(e->handleUnSendRequest(r,unsendBtn));
            card.getChildren().add(unsendBtn);

        } else if (r.getInvitationStatus() == UserSummary.InvitationStatus.RECEIVED) {
            // ── they sent us a request → Accept / Reject ─────────────
            HBox actions = new HBox(6);

            Button acceptBtn = new Button("✓ Accept");
            acceptBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(acceptBtn, Priority.ALWAYS);
            acceptBtn.setStyle("""
                -fx-background-color: #81b64c;
                -fx-text-fill: #1a1a14;
                -fx-background-radius: 7;
                -fx-font-size: 11px;
                -fx-font-weight: 700;
                -fx-padding: 8 0;
                -fx-cursor: hand;
            """);
            acceptBtn.setOnMouseEntered(e -> acceptBtn.setOpacity(0.88));
            acceptBtn.setOnMouseExited(e  -> acceptBtn.setOpacity(1.0));
            acceptBtn.setOnAction(e -> {
                acceptBtn.setDisable(true);
                acceptBtn.setText("Accepting...");
                CompletableFuture
                        .runAsync(() -> {
                            try {
                                friendShipClient.accept(r.getId());
                            } catch (IOException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .thenAccept(v -> Platform.runLater(() -> {
                            card.getChildren().remove(actions);
                            Button friendBtn = new Button("✓ Already friends");
                            friendBtn.setMaxWidth(Double.MAX_VALUE);
                            friendBtn.setStyle(friendBtnStyle());
                            friendBtn.setDisable(true);
                            card.getChildren().add(friendBtn);
                            pendingCount = Math.max(0, pendingCount - 1);
                            updateRequestsBadge();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                acceptBtn.setDisable(false);
                                acceptBtn.setText("✓ Accept");
                                showError(resolveError(ex));
                            });
                            return null;
                        });
            });

            Button rejectBtn = new Button("✕");
            rejectBtn.setPrefWidth(38);
            rejectBtn.setStyle("""
                -fx-background-color: #1e1e1e;
                -fx-text-fill: #666666;
                -fx-background-radius: 7;
                -fx-border-color: #2a2a2a;
                -fx-border-radius: 7;
                -fx-border-width: 1;
                -fx-font-size: 11px;
                -fx-font-weight: 700;
                -fx-padding: 8 0;
                -fx-cursor: hand;
            """);
            rejectBtn.setOnMouseEntered(e -> rejectBtn.setStyle(
                    rejectBtn.getStyle().replace("#1e1e1e", "#2a2a2a")
                            .replace("#666666", "#ffffff")));
            rejectBtn.setOnMouseExited(e -> rejectBtn.setStyle(
                    rejectBtn.getStyle().replace("#2a2a2a;", """
                        #1e1e1e;
                        -fx-border-color: #2a2a2a;
                    """).replace("#ffffff", "#666666")));
            rejectBtn.setOnAction(e ->{
                CompletableFuture
                        .runAsync(() -> {
                            try {
                                friendShipClient.reject(r.getId());
                            } catch (IOException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .thenAccept(v -> Platform.runLater(() -> {
                            card.getChildren().remove(actions);
                            Button sendBtn = new Button("+ Send friend request");
                            sendBtn.setMaxWidth(Double.MAX_VALUE);
                            sendBtn.setStyle(addBtnStyle());
                            sendBtn.setOnAction(ev -> handleSendRequest(r, sendBtn));
                            card.getChildren().add(sendBtn);
                            pendingCount = Math.max(0, pendingCount - 1);
                            updateRequestsBadge();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                rejectBtn.setDisable(false);
                                showError(resolveError(ex));
                            });
                            return null;
                        });
            } );
            actions.getChildren().addAll(acceptBtn, rejectBtn);
            card.getChildren().add(actions);


        } else {
            // ── no relationship → send request ────────────────────────
            Button sendBtn = new Button("+ Send friend request");
            sendBtn.setMaxWidth(Double.MAX_VALUE);
            sendBtn.setStyle(addBtnStyle());
            sendBtn.setOnAction(e -> handleSendRequest(r, sendBtn));
            card.getChildren().add(sendBtn);
        }

        return card;
    }

    // ── Send request flow (reused across branches) ────────────────────

    private void handleSendRequest(UserSummary r, Button btn) {
        btn.setDisable(true);
        btn.setText("Sending...");
        CompletableFuture
                .runAsync(() -> {
                    try {
                        friendShipClient.invite(r.getId());
                    } catch (IOException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .thenAccept(v -> Platform.runLater(() -> {
                            btn.setText("✕ Unsend request");
                            btn.setMaxWidth(Double.MAX_VALUE);
                            btn.setStyle(pendingBtnStyle());
                            btn.setDisable(false);
                            btn.setOnMouseEntered(e -> btn.setOpacity(0.80));
                            btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
                            btn.setOnAction(ev -> handleUnSendRequest(r, btn));
                        })
                )
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btn.setDisable(false);
                        btn.setText("+ Send friend request");
                        showError(resolveError(ex));
                    });
                    return null;
                });
    }

    private void handleUnSendRequest(UserSummary r,Button unsendBtn){
        unsendBtn.setDisable(true);
        unsendBtn.setText("Unsending...");
        CompletableFuture
                .runAsync(() -> {
                    try {
                        friendShipClient.unSend(r.getId());
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(v -> Platform.runLater(() -> {
                    unsendBtn.setText("+ Send friend request");
                    unsendBtn.setMaxWidth(Double.MAX_VALUE);
                    unsendBtn.setStyle(addBtnStyle());
                    unsendBtn.setDisable(false);
                    unsendBtn.setOnAction(ev -> handleSendRequest(r, unsendBtn));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        unsendBtn.setDisable(false);
                        unsendBtn.setText("✕ Unsend request");
                        showError(resolveError(ex));
                    });
                    return null;
                });
    }

    // ── Button styles ─────────────────────────────────────────────────

    private String addBtnStyle() {
        return """
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #888888;
            -fx-background-radius: 7;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
            -fx-cursor: hand;
        """;
    }

    private String friendBtnStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #81b64c;
            -fx-background-radius: 7;
            -fx-border-color: #81b64c;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
        """;
    }

    private String pendingBtnStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #e6b84a;
            -fx-background-radius: 7;
            -fx-border-color: #e6b84a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
            -fx-cursor: hand;
        """;
    }

    // ── Friend row ────────────────────────────────────────────────────

    private HBox buildFriendRow(FriendEntry f) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-cursor: hand;");

        if (f.getStatus() == Status.Offline) row.setOpacity(0.5);

        HBox avatarContainer = new HBox(8);
        avatarContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = buildAvatar(f.getAvatarUrl(),
                initials(f.getUsername()), f.getAvatarColor());

        Region statusDot = new Region();
        statusDot.setPrefSize(9, 9);
        statusDot.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 5;
            -fx-border-color: #111111;
            -fx-border-radius: 5;
            -fx-border-width: 1.5;
        """, getStatusColor(f.getStatus())));

        avatarContainer.getChildren().addAll(avatar, statusDot);

        VBox info = new VBox(2);
        Label name = new Label(f.getUsername());
        name.setStyle("""
            -fx-text-fill: #e0e0e0;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
        """);
        Label elo = new Label(f.getStatus() == Status.InGame
                ? "In game · " + f.getElo()
                : String.valueOf(f.getElo()));
        elo.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-font-size: 11px;
        """, f.getStatus() == Status.InGame ? "#e6b84a" : "#555555"));
        info.getChildren().addAll(name, elo);

        row.getChildren().addAll(avatarContainer, info);

        row.setOnMouseEntered(e -> {
            if (f.getStatus() != Status.Offline)
                row.setStyle("-fx-background-color: #1a1a1a; -fx-cursor: hand;");
        });
        row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand;"));
        row.setOnMouseClicked(e -> controller.onFriendClicked(f.getUsername()));
        return row;
    }

    // ── Request row ───────────────────────────────────────────────────

    private VBox buildRequestRow(InvitationEntry r) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("""
            -fx-border-color: transparent transparent #1a1a1a transparent;
            -fx-border-width: 1;
        """);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = buildAvatar(r.getAvatarUrl(),
                initials(r.getUsername()), r.getAvatarColor());

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(r.getUsername());
        name.setStyle("""
            -fx-text-fill: #e0e0e0;
            -fx-font-size: 12px;
            -fx-font-weight: 700;
        """);
        Label eloLbl = new Label(r.getElo() + " ELO");
        eloLbl.setStyle("-fx-text-fill: #555555; -fx-font-size: 10px;");
        info.getChildren().addAll(name, eloLbl);
        top.getChildren().addAll(avatar, info);
        card.getChildren().add(top);

        if (r.isIncoming()) {
            HBox actions = new HBox(6);

            Button accept = new Button("✓ Accept");
            accept.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(accept, Priority.ALWAYS);
            accept.setStyle("""
                -fx-background-color: #81b64c;
                -fx-text-fill: #1a1a14;
                -fx-background-radius: 6;
                -fx-font-size: 11px;
                -fx-font-weight: 700;
                -fx-padding: 7 0;
                -fx-cursor: hand;
            """);
            accept.setOnMouseEntered(e -> accept.setOpacity(0.88));
            accept.setOnMouseExited(e  -> accept.setOpacity(1.0));
            accept.setOnAction(e -> {
                accept.setDisable(true);
                CompletableFuture
                        .runAsync(() -> {
                            try {
                                friendShipClient.accept(r.getPublicId() != null
                                        ? Integer.parseInt(r.getPublicId()) : 0);
                            } catch (IOException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .thenAccept(v -> Platform.runLater(() -> {
                            requestsState.items.getChildren().remove(card);
                            pendingCount = Math.max(0, pendingCount - 1);
                            updateRequestsBadge();
                            checkEmptyRequests();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                accept.setDisable(false);
                                showError(resolveError(ex));
                            });
                            return null;
                        });
            });

            Button reject = new Button("✕");
            reject.setPrefWidth(34);
            reject.setStyle("""
                -fx-background-color: #1e1e1e;
                -fx-text-fill: #666666;
                -fx-background-radius: 6;
                -fx-border-color: #2a2a2a;
                -fx-border-radius: 6;
                -fx-border-width: 1;
                -fx-font-size: 11px;
                -fx-font-weight: 700;
                -fx-padding: 7 0;
                -fx-cursor: hand;
            """);
            reject.setOnMouseEntered(e -> reject.setStyle(
                    reject.getStyle().replace("#1e1e1e", "#2a2a2a")
                            .replace("#666666", "#ffffff")));
            reject.setOnMouseExited(e  -> reject.setStyle(
                    reject.getStyle().replace("#2a2a2a;", """
                        #1e1e1e;
                        -fx-border-color: #2a2a2a;
                    """).replace("#ffffff", "#666666")));
            reject.setOnAction(e -> {
                reject.setDisable(true);
                CompletableFuture
                        .runAsync(() -> {
                            try {
                                friendShipClient.reject(r.getPublicId() != null
                                        ? Integer.parseInt(r.getPublicId()) : 0);
                            } catch (IOException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .thenAccept(v -> Platform.runLater(() -> {
                            requestsState.items.getChildren().remove(card);
                            pendingCount = Math.max(0, pendingCount - 1);
                            updateRequestsBadge();
                            checkEmptyRequests();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                reject.setDisable(false);
                                showError(resolveError(ex));
                            });
                            return null;
                        });
            });

            actions.getChildren().addAll(accept, reject);
            card.getChildren().add(actions);

        } else {
            Button cancel = new Button("Cancel request");
            cancel.setMaxWidth(Double.MAX_VALUE);
            cancel.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #666666;
                -fx-background-radius: 6;
                -fx-border-color: #2a2a2a;
                -fx-border-radius: 6;
                -fx-border-width: 1;
                -fx-font-size: 11px;
                -fx-font-weight: 600;
                -fx-padding: 7 0;
                -fx-cursor: hand;
            """);
            cancel.setOnMouseEntered(e -> cancel.setStyle(
                    cancel.getStyle().replace("#2a2a2a", "#444444")
                            .replace("#666666", "#888888")));
            cancel.setOnMouseExited(e  -> cancel.setStyle(
                    cancel.getStyle().replace("#444444", "#2a2a2a")
                            .replace("#888888", "#666666")));
            cancel.setOnAction(e -> {
                cancel.setDisable(true);
                CompletableFuture
                        .runAsync(() -> {
                            try {
                                friendShipClient.unSend(Integer.parseInt(r.getPublicId()));
                            } catch (IOException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .thenAccept(v -> Platform.runLater(() -> {
                            requestsState.items.getChildren().remove(card);
                            checkEmptyRequests();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                cancel.setDisable(false);
                                showError(resolveError(ex));
                            });
                            return null;
                        });
            });
            card.getChildren().add(cancel);
        }

        return card;
    }

    // ── Empty / loading helpers ─────────────────────────────────────────

    private VBox buildEmptyState(String message) {
        VBox empty = new VBox(8);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(40, 14, 40, 14));

        Label icon = new Label("♟");
        icon.setStyle("-fx-text-fill: #2a2a2a; -fx-font-size: 28px;");

        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #444444; -fx-font-size: 12px;");

        empty.getChildren().addAll(icon, msg);
        return empty;
    }

    private VBox buildEmptyRequests() {
        return buildEmptyState("No pending requests");
    }

    private void checkEmptyRequests() {
        boolean noRows = requestsState.items.getChildren().isEmpty();
        if (noRows && !requestsState.hasMore) {
            requestsState.items.getChildren().add(buildEmptyRequests());
        }
    }

    // ── buildAvatar (your original) ───────────────────────────────────

    public static StackPane buildAvatar(String imageUrl,
                                        String initials,
                                        String bgColor) {
        StackPane avatar = new StackPane();
        avatar.setMinSize(38, 38);
        avatar.setPrefSize(38, 38);

        Circle clip = new Circle(19, 19, 19);
        avatar.setClip(clip);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Image image = new Image(imageUrl, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(38);
            imageView.setFitHeight(38);
            imageView.setPreserveRatio(true);
            avatar.getChildren().add(imageView);
        } else {
            avatar.setStyle("-fx-background-color: " + bgColor + ";");
            Label lbl = new Label(initials);
            lbl.setStyle("""
                -fx-text-fill: #ffffff;
                -fx-font-size: 13px;
                -fx-font-weight: 700;
            """);
            avatar.getChildren().add(lbl);
        }
        return avatar;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String getStatusColor(Status status) {
        return switch (status) {
            case InGame  -> "#e6b84a";
            case InLobby -> "#81b64c";
            case Offline -> "#333333";
        };
    }

    private String initials(String username) {
        if (username == null || username.isEmpty()) return "?";
        String[] parts = username.split("_");
        if (parts.length >= 2)
            return (parts[0].substring(0, 1)
                    + parts[1].substring(0, 1)).toUpperCase();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }

    private String avatarColorFromName(String username) {
        String[] palette = {
                "#7c5c3e","#5c3e7c","#3e7c5c",
                "#7c3e5c","#3e5c7c","#5c7c3e"
        };
        return palette[Math.abs(username.hashCode()) % palette.length];
    }

    private String resolveError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AuthenticationException)
            return "Session expired. Please log in again.";
        return "Request failed. Check your connection.";
    }
}
package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.Node;
import org.Core.Social.DTO.FriendsPage;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.util.function.BiConsumer;

public class OfflineFriendsSection extends FriendSection<FriendsPage.FriendEntry> {

    private final FriendShipClient client;
    private final LobbyController controller;
    private final BiConsumer<Integer, Boolean> onCountChanged;

    public OfflineFriendsSection(FriendShipClient client,
                                  LobbyController controller,
                                  BiConsumer<Integer, Boolean> onCountChanged) {
        this.client = client;
        this.controller = controller;
        this.onCountChanged = onCountChanged;
        initList();
    }

    @Override
    protected PageResult<FriendsPage.FriendEntry> fetchPage(String cursor) throws Exception {
        return client.fetchOfflineFriends(cursor);
    }

    @Override
    protected Node buildRow(FriendsPage.FriendEntry f) {
        return FriendRow.build(f,client, controller);
    }

    @Override
    protected String emptyMessage() {
        return "No offline friends";
    }

    @Override
    protected void onItemsAppended(PageResult<FriendsPage.FriendEntry> page) {
        onCountChanged.accept(state.getItems().getChildren().size(), state.isHasMore());
    }
}
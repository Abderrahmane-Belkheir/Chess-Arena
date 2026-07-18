package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.Node;
import org.Core.Social.DTO.FriendsPage;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.util.function.IntConsumer;

public class OnlineFriendsSection extends FriendSection<FriendsPage.FriendEntry> {

    private final FriendShipClient client;
    private final LobbyController controller;
    private final IntConsumer onCountChanged;

    public OnlineFriendsSection(FriendShipClient client,
                                 LobbyController controller,
                                 IntConsumer onCountChanged) {
        this.client = client;
        this.controller = controller;
        this.onCountChanged = onCountChanged;
        initList();
    }

    @Override
    protected PageResult<FriendsPage.FriendEntry> fetchPage(String cursor) throws Exception {
        return client.fetchOnlineFriends(cursor);
    }

    @Override
    protected Node buildRow(FriendsPage.FriendEntry f) {
        return FriendRow.build(f, client,controller);
    }

    @Override
    protected String emptyMessage() {
        return "No friends online";
    }

    @Override
    protected void onItemsAppended(PageResult<FriendsPage.FriendEntry> page) {
        onCountChanged.accept(state.getItems().getChildren().size());
    }
}
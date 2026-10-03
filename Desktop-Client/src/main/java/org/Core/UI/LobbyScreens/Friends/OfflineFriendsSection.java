package org.Core.UI.LobbyScreens.Friends;

import org.Core.Social.DTO.FriendsList;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.util.List;
import java.util.function.IntConsumer;

public class OfflineFriendsSection extends FriendListSection {

    public OfflineFriendsSection(FriendShipClient client,
                                  LobbyController controller,
                                  IntConsumer onCountChanged,
                                  IntConsumer onFriendRemoved) {
        super(client, controller, onCountChanged, onFriendRemoved);
    }

    @Override
    protected List<FriendsList.FriendEntry> fetchAll() throws Exception {
        return client.fetchOfflineFriends();
    }

    @Override
    protected String emptyMessage() {
        return "No offline friends";
    }

    @Override
    protected void onItemsLoaded(List<FriendsList.FriendEntry> items) {
        onCountChanged.accept(items.size());
    }
}

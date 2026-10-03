package org.Core.UI.LobbyScreens.Friends;

import javafx.application.Platform;
import javafx.scene.Node;
import org.Core.Social.DTO.FriendsList;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.util.function.IntConsumer;

/**
 * Shared base for the three friend-status tabs (In Lobby / In Game /
 * Offline). Adds addFriend/removeFriend so a single friend row can later be
 * moved between sections (e.g. by a live FriendStatus update) without a full
 * refetch of the tab it lands in or leaves — both keep the tab's displayed
 * count (onCountChanged) in sync as they mutate the list.
 */
public abstract class FriendListSection extends FriendSection<FriendsList.FriendEntry> {

    protected final FriendShipClient client;
    protected final LobbyController controller;
    protected final IntConsumer onCountChanged;
    // Handed down to FriendCard (via FriendRow) so a successful "Remove
    // friend" there can drop the row from whichever section actually holds
    // it, without FriendCard needing to know about sections at all.
    protected final IntConsumer onFriendRemoved;

    protected FriendListSection(FriendShipClient client, LobbyController controller,
                                 IntConsumer onCountChanged, IntConsumer onFriendRemoved) {
        this.client = client;
        this.controller = controller;
        this.onCountChanged = onCountChanged;
        this.onFriendRemoved = onFriendRemoved;
        initList();
    }

    /**
     * Stamps userData with the FriendEntry itself so removeFriend can find
     * and return it later — this runs for every row regardless of whether it
     * came from the initial fetch (FriendSection.onLoaded calls buildRow
     * directly) or from addFriend, so both paths stay consistent.
     */
    @Override
    protected Node buildRow(FriendsList.FriendEntry f) {
        Node row = FriendRow.build(f, client, controller, onFriendRemoved);
        row.setUserData(f);
        return row;
    }

    /**
     * Stamps the friend with this section's status (the row must reflect
     * where it's landing, not wherever it was before), inserts it at the
     * top of the list — never appended at the bottom — and refreshes this
     * tab's displayed count.
     *
     * No-op if this section has never been loaded yet: its own first fetch
     * (whenever the user opens that tab) will already reflect the friend's
     * current status from the server, and onLoaded() unconditionally clears
     * the list before rebuilding — so a live-inserted row here would just
     * get wiped out the moment that first load runs, silently "losing" it.
     */
    public void addFriend(FriendsList.FriendEntry friend, FriendsList.Status status) {
        if (!state.isLoadedOnce()) return;
        friend.setStatus(status);
        insertAtTop(buildRow(friend));
        onCountChanged.accept(itemCount());
    }

    /**
     * Removes the friend (matched by id) from this section's list, if
     * present, and refreshes this tab's displayed count once the row is
     * actually gone. No-op (returns null) if this section has never been
     * loaded yet — same reasoning as addFriend, there's nothing to remove
     * from a list that hasn't been populated.
     *
     * @return the removed FriendEntry, or null if this section didn't have
     *         them — callers trying each section in turn know to stop once
     *         they get a non-null result back.
     */
    public FriendsList.FriendEntry removeFriend(int friendId) {
        if (!state.isLoadedOnce()) return null;
        FriendsList.FriendEntry removed = removeRowIf(node -> node.getUserData() instanceof FriendsList.FriendEntry entry
                && entry.getId() == friendId);
        if (removed != null) {
            // removeRowIf's actual removal is deferred (Platform.runLater),
            // so queue the count refresh behind it rather than reading a
            // stale itemCount() before the row is really gone.
            Platform.runLater(() -> onCountChanged.accept(itemCount()));
        }
        return removed;
    }

}

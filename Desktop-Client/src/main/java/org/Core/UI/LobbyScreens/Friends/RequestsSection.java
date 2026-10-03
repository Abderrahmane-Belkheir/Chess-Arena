
package org.Core.UI.LobbyScreens.Friends;


import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.Core.Social.DTO.InvitationsList;
import org.Core.Social.FriendShipClient;


import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;


public class RequestsSection extends FriendSection<InvitationsList.InvitationEntry> {

    private final FriendShipClient client;
    private final IntConsumer onIncomingCountDelta;
    private final Consumer<String> onError;

    // "You have unseen invitations" bubble — separate from the tab's total
    // pending count. Hidden while 0, shows the count once >0, and clicking
    // it resets back to 0. Not wired to anything yet — nothing calls
    // increaseUnseen(), and nowhere places getUnseenBadge() in the layout.
    private final Label unseenBadge = new Label();
    private int unseenCount = 0;

    public RequestsSection(FriendShipClient client,
                            IntConsumer onIncomingCountDelta,
                            Consumer<String> onError) {
        this.client = client;
        this.onIncomingCountDelta = onIncomingCountDelta;
        this.onError = onError;
        initList();

        unseenBadge.getStyleClass().add("unseen-badge");
        unseenBadge.setOnMouseClicked(e -> resetUnseen());
        updateUnseenBadge();
    }

    public Node getUnseenBadge() { return unseenBadge; }

    /**
     * Bumps the unseen-invitations count by one and refreshes the badge —
     * skipped while this section is the one currently on screen (only one
     * section's list is ever attached to FriendsPanel's listContainer at a
     * time, so a non-null parent means the user is already looking at it).
     */
    public void increaseUnseen() {
        if (!state.isLoadedOnce() || state.getList().getParent() != null) return;
        unseenCount++;
        updateUnseenBadge();
    }

    /** Clears the unseen-invitations count back to 0 — also runs when the badge itself is clicked. */
    public void resetUnseen() {
        unseenCount = 0;
        updateUnseenBadge();
    }

    /**
     * Mirrors increaseUnseen for a removed invitation — same visibility
     * guard: skipped while this section is on screen, since the bubble is
     * already at 0 there (either it was never bumped, or the user already
     * reset it by opening the tab).
     */
    public void decreaseUnseen() {
        if (!state.isLoadedOnce() || state.getList().getParent() != null) return;
        if (unseenCount > 0) unseenCount--;
        updateUnseenBadge();
    }

    private void updateUnseenBadge() {
        unseenBadge.setText(String.valueOf(unseenCount));
        boolean visible = unseenCount > 0;
        unseenBadge.setVisible(visible);
        unseenBadge.setManaged(visible);
    }

    @Override
    protected List<InvitationsList.InvitationEntry> fetchAll() throws Exception {
        return client.fetchInvitations();
    }

    @Override
    protected Node buildRow(InvitationsList.InvitationEntry r) {
        Node row = RequestRow.build(
                r,
                client,
                this::removeCard,
                () -> onIncomingCountDelta.accept(-1),
                onError
        );
        row.setUserData(r);
        return row;
    }

    @Override
    protected String emptyMessage() {
        return "No pending requests";
    }

    @Override
    protected void onItemsLoaded(List<InvitationsList.InvitationEntry> items) {
        long incoming = items.stream()
                .filter(InvitationsList.InvitationEntry::isIncoming)
                .count();
        if (incoming > 0) {
            onIncomingCountDelta.accept((int) incoming);
        }
    }

    private void removeCard(VBox card) {
        state.getItems().getChildren().remove(card);
        checkEmpty();
    }

    /**
     * Builds the row for this invitation and inserts it at the top of the
     * list. No-op if this section hasn't loaded yet — same reasoning as
     * FriendListSection.addFriend: its own first fetch will already include
     * this invitation, and onLoaded() unconditionally clears the list before
     * rebuilding, so a live-inserted row here would just get wiped out.
     */
    public void addInvitation(InvitationsList.InvitationEntry entry) {
        if (!state.isLoadedOnce()) return;
        insertAtTop(buildRow(entry));
    }

    /**
     * Removes whichever row (incoming or outgoing) matches publicId — used
     * when the other side of a pending request declines or unsends it, so
     * this client's Requests list drops the now-stale entry without a manual
     * refresh. Doesn't touch the pending-count badge.
     */
    public void removeByPublicId(int publicId) {
        if (!state.isLoadedOnce()) return;
        removeRowIf(node ->
                node.getUserData() instanceof InvitationsList.InvitationEntry entry
                        && entry.getPublicId() != null
                        && entry.getPublicId().equals(String.valueOf(publicId)));
    }
}

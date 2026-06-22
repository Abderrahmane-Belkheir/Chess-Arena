
package org.Core.UI.LobbyScreens.Friends;


import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.Core.Social.DTO.InvitationsPage;
import org.Core.Social.FriendShipClient;


import java.util.function.Consumer;
import java.util.function.IntConsumer;


public class RequestsSection extends FriendSection<InvitationsPage.InvitationEntry> {

    private final FriendShipClient client;
    private final IntConsumer onIncomingCountDelta;
    private final Consumer<String> onError;

    public RequestsSection(FriendShipClient client,
                            IntConsumer onIncomingCountDelta,
                            Consumer<String> onError) {
        this.client = client;
        this.onIncomingCountDelta = onIncomingCountDelta;
        this.onError = onError;
        initList();
    }

    @Override
    protected PageResult<InvitationsPage.InvitationEntry> fetchPage(String cursor) throws Exception {
        return client.fetchRequests(cursor);
    }

    @Override
    protected Node buildRow(InvitationsPage.InvitationEntry r) {
        return RequestRow.build(
                r,
                client,
                this::removeCard,
                () -> onIncomingCountDelta.accept(-1),
                onError
        );
    }

    @Override
    protected String emptyMessage() {
        return "No pending requests";
    }

    @Override
    protected void onItemsAppended(PageResult<InvitationsPage.InvitationEntry> page) {
        long newIncoming = page.items().stream()
                .filter(InvitationsPage.InvitationEntry::isIncoming)
                .count();
        if (newIncoming > 0) {
            onIncomingCountDelta.accept((int) newIncoming);
        }
    }

    private void removeCard(VBox card) {
        state.getItems().getChildren().remove(card);
        checkEmpty();
    }
}
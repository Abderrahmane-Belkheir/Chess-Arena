package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.layout.VBox;
import lombok.Data;


@Data
public class SectionState<T> {
    private final VBox    list  = new VBox(0);
    // Spacing (not 0) — each friend row is now its own bordered card (see
    // FriendRow / .friend-row in lobby.css), not a flat row sharing edges
    // with its neighbors, so it needs a real gap between rows.
    private final VBox    items = new VBox(8);
    private boolean loading  = false;
    private boolean loadedOnce = false;
    }
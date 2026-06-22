package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.layout.VBox;
import lombok.Data;


@Data
public class SectionState<T> {
    private final VBox    list  = new VBox(0);
    private final VBox    items = new VBox(0);
    private String  cursor   = null;
    private boolean hasMore  = true;
    private boolean loading  = false;
    private boolean loadedOnce = false;
    }
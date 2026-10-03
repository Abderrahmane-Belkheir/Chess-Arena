package org.Core.UI.OpeningScreens;

import javafx.scene.layout.StackPane;

public interface GameController {
    void transitionToLobby();
    void start();
    void showGame(StackPane gameContent);
    void exitGame();
    void startNewGame();
}

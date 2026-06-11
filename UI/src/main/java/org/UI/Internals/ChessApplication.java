package org.UI.Internals;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;



public class ChessApplication extends Application {

    private Scene scene;
    private ImageView globalBg;
    private StackPane uiLayer;
    private StackPane overlayLayer; // Dedicated flat layer for cinematic dimming (Zero-performance cost)

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        scene = new Scene(root, 1200, 800, Color.BLACK);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close(); // Closes the window
                System.exit(0);  // Cleanly terminates the JVM process
            }
        });
        globalBg = new ImageView();
        try {
            // Load with exact sizing hints to minimize runtime texture mapping costs
            globalBg.setImage(new Image(getClass().getResource("/chess-background.png").toExternalForm(), 1920, 1080, true, true, true));
        } catch (Exception e) {
            System.err.println("Background image asset missing. Using flat fallback.");
        }
        globalBg.fitWidthProperty().bind(scene.widthProperty());
        globalBg.fitHeightProperty().bind(scene.heightProperty());
        globalBg.setPreserveRatio(false);
        globalBg.setSmooth(false); // Scaled down filter cost for rendering performance

        overlayLayer = new StackPane();
        overlayLayer.setMouseTransparent(true);
        overlayLayer.setOpacity(0);
        // Rich dark-violet glass overlay replacing the expensive Gaussian Blur
        overlayLayer.setBackground(new Background(new BackgroundFill(
                Color.web("#09060B", 0.72), CornerRadii.EMPTY, Insets.EMPTY
        )));

        uiLayer = new StackPane();

        root.getChildren().addAll(globalBg, overlayLayer, uiLayer);

        stage.setScene(scene);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.setResizable(true);
        stage.show();

        showSplash(stage);
    }

    // ───────────────────────── SPLASH ─────────────────────────
    private void showSplash(Stage stage) {
        uiLayer.getChildren().clear();
        overlayLayer.setOpacity(0);


        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> fadeToNext(() -> showModeSelect(stage), true));
        pause.play();
    }

    // ───────────────────────── MODE SELECT ─────────────────────────
    private void showModeSelect(Stage stage) {
        uiLayer.getChildren().clear();

        Text title = new Text("CHOOSE ARENA");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 38px; -fx-font-weight: 900; -fx-letter-spacing: 2px;");

        StackPane offlineCard = modeCard("Offline Play", "Challenge our tactical neural network engine locally.", "#C9A84C", "#E8C97A", () -> showChessBoard(stage));
        StackPane onlineCard  = modeCard("Online Play", "Rank up against real-time global grandmasters.", "#7B6FD4", "#A89BF5", () -> showAuth(stage));

        HBox cards = new HBox(40, offlineCard, onlineCard);
        cards.setAlignment(Pos.CENTER);
        cards.maxWidthProperty().bind(scene.widthProperty().multiply(0.8));

        VBox layout = new VBox(45, title, cards);
        layout.setAlignment(Pos.CENTER);

        uiLayer.getChildren().add(layout);
    }

    // ───────────────────────── 60FPS FLUID MODE CARD ─────────────────────────
    private StackPane modeCard(String title, String desc, String colorA, String colorB, Runnable onClickAction) {
        StackPane container = new StackPane();
        container.setPrefSize(340, 240);
        container.setMaxSize(340, 240);

        // Native Hardware Caching
        container.setCache(true);
        container.setCacheHint(CacheHint.SPEED);

        Rectangle bg = new Rectangle(340, 240);
        bg.setArcWidth(24);
        bg.setArcHeight(24);
        bg.setFill(Color.web("#141118"));
        bg.setStroke(Color.web("#ffffff", 0.08));
        bg.setStrokeWidth(1.2);

        // Static shadow attached directly to a primitive instead of the whole layout tree
        bg.setEffect(new DropShadow(15, Color.color(0, 0, 0, 0.6)));

        Rectangle bottomLine = new Rectangle(300, 3);
        bottomLine.setArcWidth(3);
        bottomLine.setArcHeight(3);
        bottomLine.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(colorA)),
                new Stop(1, Color.web(colorB))
        ));
        bottomLine.setOpacity(0.3);

        Text t = new Text(title);
        t.setFill(Color.WHITE);
        t.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Text d = new Text(desc);
        d.setFill(Color.web("#A09CA6"));
        d.setStyle("-fx-font-size: 13px;");
        d.setWrappingWidth(260);
        d.setTextAlignment(TextAlignment.CENTER);

        Button btn = new Button("LAUNCH");
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, " + colorA + ", " + colorB + ");" +
                        "-fx-text-fill: #0F0A12;" +
                        "-fx-font-weight: 900;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 32;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnAction(e -> {
            e.consume();
            fadeToNext(onClickAction, true);
        });

        VBox content = new VBox(18, t, d, btn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));

        container.getChildren().addAll(bg, content, bottomLine);
        StackPane.setAlignment(bottomLine, Pos.BOTTOM_CENTER);
        StackPane.setMargin(bottomLine, new Insets(0, 0, 12, 0));

        // Scale Transition targeting purely the container matrix values
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(120), container);
        scaleIn.setToX(1.03);
        scaleIn.setToY(1.03);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(120), container);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        scaleOut.setInterpolator(Interpolator.EASE_OUT);

        container.setOnMouseEntered(e -> {
            scaleOut.stop();
            bottomLine.setOpacity(1.0);
            bg.setStroke(Color.web(colorB, 0.6));
            scaleIn.play();
        });

        container.setOnMouseExited(e -> {
            scaleIn.stop();
            bottomLine.setOpacity(0.3);
            bg.setStroke(Color.web("#ffffff", 0.08));
            scaleOut.play();
        });

        container.setOnMouseClicked(e -> {
            if (e.getPickResult().getIntersectedNode() != btn) {
                fadeToNext(onClickAction, true);
            }
        });
        container.setStyle("-fx-cursor: hand;");

        return container;
    }

    // ───────────────────────── AUTHENTICATION FORM ─────────────────────────
    private void showAuth(Stage stage) {
        uiLayer.getChildren().clear();

        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(45, 50, 45, 50));
        card.setMaxSize(420, 280);
        card.setStyle(
                "-fx-background-color: #14101A;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: rgba(232, 217, 181, 0.15);" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-width: 1.5;"
        );
        card.setEffect(new DropShadow(20, Color.color(0, 0, 0, 0.6)));

        Text title = new Text("Secure Gateway");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text subtitle = new Text("Sign in via Clerk to sync ratings and matchmaking.");
        subtitle.setFill(Color.web("#A09CA6"));
        subtitle.setStyle("-fx-font-size: 13px;");

        Button clerkBtn = new Button("Continue with Clerk");
        clerkBtn.setStyle(
                "-fx-background-color: #6366F1;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );
        clerkBtn.setOnAction(e -> fadeToNext(() -> showChessBoard(stage), false));

        Text back = new Text("← Go Back");
        back.setFill(Color.web("#C9A84C"));
        back.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        back.setOnMouseEntered(e -> back.setFill(Color.web("#E8C97A")));
        back.setOnMouseExited(e -> back.setFill(Color.web("#C9A84C")));
        back.setOnMouseClicked(e -> fadeToNext(() -> showModeSelect(stage), true));

        card.getChildren().addAll(title, subtitle, clerkBtn, back);
        uiLayer.getChildren().add(card);
    }

    // ───────────────────────── CHESS ARENA ─────────────────────────
    private void showChessBoard(Stage stage) {
        uiLayer.getChildren().clear();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        double size = 76;
        Color light = Color.web("#E8D9B5");
        Color dark  = Color.web("#7A5230");

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final boolean isLight = (r + c) % 2 == 0;
                Rectangle tile = new Rectangle(size, size, isLight ? light : dark);

                StackPane cell = new StackPane(tile);
                cell.setPrefSize(size, size);

                cell.setOnMouseEntered(e ->
                        tile.setFill(isLight ? Color.web("#D4C49A") : Color.web("#9B6B40"))
                );
                cell.setOnMouseExited(e ->
                        tile.setFill(isLight ? light : dark)
                );

                grid.add(cell, c, r);
            }
        }

        grid.setEffect(new DropShadow(25, Color.BLACK));
        uiLayer.getChildren().add(grid);
    }

    // ───────────────────────── FAST 60FPS TRANSITION SYSTEM ─────────────────────────
    private void fadeToNext(Runnable nextLayoutLoader, boolean showOverlay) {
        uiLayer.setCache(true);
        uiLayer.setCacheHint(CacheHint.SPEED);

        // Parallel transition to smoothly animate UI fade alongside overlay adjustments
        ParallelTransition ptOut = new ParallelTransition();

        FadeTransition uiFadeOut = new FadeTransition(Duration.millis(120), uiLayer);
        uiFadeOut.setToValue(0);
        ptOut.getChildren().add(uiFadeOut);

        if (showOverlay) {
            FadeTransition overlayFadeIn = new FadeTransition(Duration.millis(120), overlayLayer);
            overlayFadeIn.setToValue(1.0);
            ptOut.getChildren().add(overlayFadeIn);
        } else {
            FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(120), overlayLayer);
            overlayFadeOut.setToValue(0.0);
            ptOut.getChildren().add(overlayFadeOut);
        }

        ptOut.setOnFinished(e -> {
            nextLayoutLoader.run();

            ParallelTransition ptIn = new ParallelTransition();

            FadeTransition uiFadeIn = new FadeTransition(Duration.millis(150), uiLayer);
            uiFadeIn.setToValue(1);
            ptIn.getChildren().add(uiFadeIn);

            ptIn.play();
        });

        ptOut.play();
    }
    public static void main(String[] args){launch(args);}
}
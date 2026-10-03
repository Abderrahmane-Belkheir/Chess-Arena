package org.Core.UI.OpeningScreens;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class ModeView {

    private final StackPane root = new StackPane();

    public ModeView(Runnable online, Runnable offline) {

        root.setStyle("-fx-background-color: #0a0a0a;");

        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        page.setAlignment(Pos.TOP_CENTER);

        // ── Hero ─────────────────────────────────────────────────────
        StackPane hero = buildHero();

        // ── Cards row ────────────────────────────────────────────────
        HBox cards = new HBox(16);
        cards.setMaxWidth(860);
        HBox.setHgrow(cards, Priority.ALWAYS);

        VBox onlineCard  = buildOnlineCard(online);
        VBox offlineCard = buildOfflineCard(offline);
        HBox.setHgrow(onlineCard,  Priority.ALWAYS);
        HBox.setHgrow(offlineCard, Priority.ALWAYS);
        onlineCard.setMaxWidth(Double.MAX_VALUE);
        offlineCard.setMaxWidth(Double.MAX_VALUE);
        cards.getChildren().addAll(onlineCard, offlineCard);

        page.getChildren().addAll(hero, cards);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setFocusTraversable(false);
        scroll.setStyle("""
            -fx-background: #0a0a0a;
            -fx-background-color: #0a0a0a;
            -fx-control-inner-background: #0a0a0a;
            -fx-background-insets: 0;
            -fx-padding: 0;
        """);

        page.setMinHeight(Region.USE_COMPUTED_SIZE);
        page.setMaxHeight(Double.MAX_VALUE);

        root.getChildren().add(scroll);
    }

    // ── Hero section ─────────────────────────────────────────────────
    private StackPane buildHero() {

        StackPane hero = new StackPane();
        hero.setMaxWidth(860);
        hero.setMinHeight(260);
        hero.setStyle("""
            -fx-background-color: #141414;
            -fx-background-radius: 14;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 14;
            -fx-border-width: 1;
        """);

        // Chessboard texture
        GridPane board = new GridPane();
        board.setOpacity(0.18);
        board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 14; c++) {
                Region sq = new Region();
                sq.setPrefSize(72, 72);
                sq.setStyle("-fx-background-color: " +
                        ((r + c) % 2 == 0 ? "#ffffff" : "#000000") + ";");
                board.add(sq, c, r);
            }
        }
        StackPane.setAlignment(board, Pos.TOP_LEFT);

        // Vignette overlay — fades board edges
        Region vignette = new Region();
        vignette.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        vignette.setStyle("""
            -fx-background-color:
                radial-gradient(center 50% 50%, radius 70%,
                    transparent 30%, #141414 100%);
            -fx-background-radius: 14;
        """);

        // Hero content
        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(36, 40, 40, 40));

        Label title = new Label("Your next move\nstarts here");
        title.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 44px;
            -fx-font-weight: 900;
            -fx-text-alignment: center;
            -fx-alignment: center;
            
        """);
        title.setTextAlignment(TextAlignment.CENTER);

        Label sub = new Label("Jump into a live match or sharpen your skills against\nthe engine. Pick a mode and play in seconds.");
        sub.setStyle("""
            -fx-text-fill: #666666;
            -fx-font-size: 14px;
            -fx-text-alignment: center;
        """);
        sub.setTextAlignment(TextAlignment.CENTER);

        content.getChildren().addAll(title, sub);

        hero.getChildren().addAll(board, vignette, content);
        return hero;
    }

    // ── Online card ───────────────────────────────────────────────────
    private VBox buildOnlineCard(Runnable action) {

        VBox card = new VBox(0);
        card.setStyle("""
            -fx-background-color: #141414;
            -fx-background-radius: 14;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 14;
            -fx-border-width: 1;
        """);

        VBox inner = new VBox(0);
        inner.setPadding(new Insets(20, 20, 16, 20));

        // Globe icon box — green glow bg
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(52, 52);
        iconBox.setStyle("""
            -fx-background-color: radial-gradient(center 40% 40%, radius 60%,
                rgba(129,182,76,0.35), rgba(30,40,20,0.9));
            -fx-background-radius: 12;
        """);
        Label icon = new Label("⊕");
        icon.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px;");
        iconBox.getChildren().add(icon);
        VBox.setMargin(iconBox, new Insets(0, 0, 14, 0));

        Label title = new Label("Play Online");
        title.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 18px;
            -fx-font-weight: 900;
        """);
        VBox.setMargin(title, new Insets(0, 0, 6, 0));

        Label desc = new Label("Get matched with a real opponent at your skill level and play live, rated games.");
        desc.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        desc.setWrapText(true);
        VBox.setMargin(desc, new Insets(0, 0, 14, 0));

        // Tag pills
        HBox tags = new HBox(6);
        tags.getChildren().addAll(
                buildTag("● Rated"),
                buildTag("● Avg wait 8s")
        );
        VBox.setMargin(tags, new Insets(0, 0, 14, 0));

        // White "Find a Match" button
        Button btn = new Button("Find a Match");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("""
            -fx-background-color: #f0f0f0;
            -fx-text-fill: #0a0a0a;
            -fx-background-radius: 8;
            -fx-font-size: 13px;
            -fx-font-weight: 700;
            -fx-padding: 10 0;
            -fx-cursor: hand;
        """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#f0f0f0", "#ffffff")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("#ffffff", "#f0f0f0")));
        btn.setOnAction(e -> action.run());

        inner.getChildren().addAll(iconBox, title, desc, tags, btn);
        card.getChildren().add(inner);

        addCardHover(card, "#2a2a2a", "#3d3d3d");
        card.setOnMouseClicked(e -> action.run());
        return card;
    }

    // ── Offline card ──────────────────────────────────────────────────
    private VBox buildOfflineCard(Runnable action) {

        VBox card = new VBox(0);
        card.setStyle("""
            -fx-background-color: #141414;
            -fx-background-radius: 14;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 14;
            -fx-border-width: 1;
        """);

        VBox inner = new VBox(0);
        inner.setPadding(new Insets(20, 20, 16, 20));

        // CPU icon box — dark steel bg
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(52, 52);
        iconBox.setStyle("""
            -fx-background-color: radial-gradient(center 40% 40%, radius 60%,
                rgba(80,100,140,0.5), rgba(20,25,40,0.95));
            -fx-background-radius: 12;
        """);
        Label icon = new Label("⬡");
        icon.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 20px;");
        iconBox.getChildren().add(icon);
        VBox.setMargin(iconBox, new Insets(0, 0, 14, 0));

        Label title = new Label("vs Computer");
        title.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 18px;
            -fx-font-weight: 900;
        """);
        VBox.setMargin(title, new Insets(0, 0, 6, 0));

        Label desc = new Label("Train against an adjustable engine. Perfect for practicing openings and tactics.");
        desc.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        desc.setWrapText(true);
        VBox.setMargin(desc, new Insets(0, 0, 14, 0));

        HBox tags = new HBox(6);
        tags.getChildren().addAll(
                buildTag("Level 1-10"),
                buildTag("Offline ready"),
                buildTag("No clock")
        );
        VBox.setMargin(tags, new Insets(0, 0, 14, 0));

        // Dark "Start Game" button
        Button btn = new Button("Start Game");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("""
            -fx-background-color: #252525;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 8;
            -fx-font-size: 13px;
            -fx-font-weight: 700;
            -fx-padding: 10 0;
            -fx-cursor: hand;
        """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#252525", "#333333")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("#333333", "#252525")));
        btn.setOnAction(e -> action.run());

        inner.getChildren().addAll(iconBox, title, desc, tags, btn);
        card.getChildren().add(inner);

        addCardHover(card, "#2a2a2a", "#3d3d3d");
        card.setOnMouseClicked(e -> action.run());
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private Label buildTag(String text) {
        Label tag = new Label(text);
        tag.setStyle("""
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #888888;
            -fx-background-radius: 20;
            -fx-border-color: #333333;
            -fx-border-radius: 20;
            -fx-border-width: 1;
            -fx-font-size: 10px;
            -fx-font-weight: 600;
            -fx-padding: 3 8;
        """);
        return tag;
    }

    private void addCardHover(VBox card, String borderNormal, String borderHover) {
        String base = card.getStyle();
        card.setOnMouseEntered(e -> {
            card.setStyle(base.replace(borderNormal, borderHover));
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), card);
            tt.setToY(-3);
            tt.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(base);
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), card);
            tt.setToY(0);
            tt.play();
        });
    }

    public StackPane getView() { return root; }
}

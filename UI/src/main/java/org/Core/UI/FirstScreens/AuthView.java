package org.Core.UI.FirstScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;


public class AuthView {

    private final StackPane root = new StackPane();

    private static final String BG =
            "-fx-background-color: radial-gradient(center 50% 38%, radius 65%, #121212 0%, #0a0a0a 55%, #070707 100%);";

    private static final String CARD =
            "-fx-background-color: rgba(20,20,23,0.97);" +
                    "-fx-background-radius: 24;" +
                    "-fx-padding: 36;" +
                    "-fx-border-radius: 24;" +
                    "-fx-border-color: rgba(255,255,255,0.07);" +
                    "-fx-border-width: 1;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 35, 0.25, 0, 14);";

    private static final String BADGE_LABEL =
            "-fx-text-fill: #9ca3af;" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: 700;";

    private static final String TITLE_STYLE =
            "-fx-text-fill: white;" +
                    "-fx-font-size: 26px;" +
                    "-fx-font-weight: 800;";

    private static final String SUBTITLE_STYLE =
            "-fx-text-fill: #9aa3af;" +
                    "-fx-font-size: 13.5px;";

    private static final String SEPARATOR_STYLE =
            "-fx-background-color: rgba(255,255,255,0.08);";

    private static final String FEATURE_ROW_STYLE =
            "-fx-background-color: rgba(255,255,255,0.035);" +
                    "-fx-background-radius: 14;" +
                    "-fx-border-color: rgba(255,255,255,0.06);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 14;";

    private static final String FEATURE_TITLE_STYLE =
            "-fx-text-fill: white;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 700;";

    private static final String FEATURE_DESC_STYLE =
            "-fx-text-fill: #9aa3af;" +
                    "-fx-font-size: 12.5px;";

    private static final String BTN_STYLE =
            "-fx-background-color: linear-gradient(to right, #f7b733, #e08e0b);" +
                    "-fx-text-fill: #1a1100;" +
                    "-fx-background-radius: 14;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 800;" +
                    "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.45), 22, 0.3, 0, 6);";

    private static final String BTN_HOVER_STYLE =
            "-fx-background-color: linear-gradient(to right, #e6a82c, #c97c08);" +
                    "-fx-text-fill: #1a1100;" +
                    "-fx-background-radius: 14;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: 800;" +
                    "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.55), 26, 0.35, 0, 6);";

    public AuthView(Runnable onContinue) {

        root.setStyle(BG);

        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(440);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle(CARD);

        // ---- Header: lock icon + "ACCOUNT REQUIRED" + separator ----
        StackPane lockBox = iconBox(Color.web("#f5b942"), 0.16,
                lockIcon(Color.web("#f5b942")), 40);

        Label badge = new Label("ACCOUNT REQUIRED");
        badge.setStyle(BADGE_LABEL);

        HBox headerRow = new HBox(12, lockBox, badge);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.setStyle(SEPARATOR_STYLE);

        VBox headerSection = new VBox(16, headerRow, separator);

        // ---- Title + subtitle ----
        Label title = new Label("Sign in to play online");
        title.setStyle(TITLE_STYLE);
        title.setMaxWidth(Double.MAX_VALUE);

        Label subtitle = new Label(
                "Create or sign in to your account to compete against players around the world."
        );
        subtitle.setStyle(SUBTITLE_STYLE);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(Double.MAX_VALUE);

        // ---- Feature rows ----
        StackPane eloBox = iconBox(Color.web("#22c55e"), 0.16,
                trendUpIcon(Color.web("#4ade80")), 40);
        HBox eloRow = featureRow(eloBox, "Elo rating tracking",
                "Watch your rating climb after every match.");

        StackPane historyBox = iconBox(Color.web("#3b82f6"), 0.16,
                historyIcon(Color.web("#60a5fa")), 40);
        HBox historyRow = featureRow(historyBox, "Game history",
                "Review every move from your past games.");

        StackPane globeBox = iconBox(Color.web("#f59e0b"), 0.16,
                globeIcon(Color.web("#f5b942")), 40);
        HBox globalRow = featureRow(globeBox, "Global leaderboards",
                "See where you stand against players worldwide.");

        VBox featuresBox = new VBox(12, eloRow, historyRow, globalRow);

        // ---- Continue button ----
        Button btn = new Button("Continue with account   \u2192");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(50);
        btn.setStyle(BTN_STYLE);

        btn.setOnMouseEntered(e -> btn.setStyle(BTN_HOVER_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(BTN_STYLE));
        btn.setOnAction(e -> onContinue.run());

        card.getChildren().addAll(headerSection, title, subtitle, featuresBox, btn);
        root.getChildren().add(card);
    }

    public StackPane getView() {
        return root;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private StackPane iconBox(Color tint, double opacity, Node icon, double size) {
        StackPane box = new StackPane();
        box.setPrefSize(size, size);
        box.setMinSize(size, size);
        box.setMaxSize(size, size);
        box.setStyle(String.format(
                "-fx-background-color: rgba(%d,%d,%d,%.2f); -fx-background-radius: 10;",
                (int) (tint.getRed() * 255),
                (int) (tint.getGreen() * 255),
                (int) (tint.getBlue() * 255),
                opacity
        ));
        box.getChildren().add(icon);
        return box;
    }

    private HBox featureRow(StackPane icon, String titleText, String descText) {
        Label t = new Label(titleText);
        t.setStyle(FEATURE_TITLE_STYLE);

        Label d = new Label(descText);
        d.setStyle(FEATURE_DESC_STYLE);
        d.setWrapText(true);
        d.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(2, t, d);
        textBox.setMaxWidth(Double.MAX_VALUE);

        HBox row = new HBox(14, icon, textBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 16, 14, 16));
        row.setStyle(FEATURE_ROW_STYLE);
        row.setMaxWidth(Double.MAX_VALUE);

        return row;
    }

    // ---------------------------------------------------------------
    // Hand-drawn icons (16x16 logical grid, no external icon library)
    // ---------------------------------------------------------------

    private Group lockIcon(Color color) {
        Arc shackle = new Arc(8, 7, 3.6, 3.6, 0, 180);
        shackle.setType(ArcType.OPEN);
        shackle.setFill(null);
        shackle.setStroke(color);
        shackle.setStrokeWidth(1.6);

        Rectangle body = new Rectangle(3, 7, 10, 7);
        body.setArcWidth(2.5);
        body.setArcHeight(2.5);
        body.setFill(color);

        Circle keyhole = new Circle(8, 10.3, 0.9);
        keyhole.setFill(Color.rgb(0, 0, 0, 0.35));

        return new Group(shackle, body, keyhole);
    }

    private Group trendUpIcon(Color color) {
        Polyline line = new Polyline(2, 12, 6, 8, 9, 10.5, 14, 3);
        line.setStroke(color);
        line.setStrokeWidth(1.6);
        line.setFill(null);

        Polygon arrowHead = new Polygon(11, 3, 14, 3, 14, 6);
        arrowHead.setFill(color);

        return new Group(line, arrowHead);
    }

    private Group historyIcon(Color color) {
        Circle face = new Circle(8, 8, 5.5);
        face.setFill(null);
        face.setStroke(color);
        face.setStrokeWidth(1.4);

        Line hourHand = new Line(8, 8, 8, 5);
        hourHand.setStroke(color);
        hourHand.setStrokeWidth(1.4);

        Line minuteHand = new Line(8, 8, 10.5, 9);
        minuteHand.setStroke(color);
        minuteHand.setStrokeWidth(1.4);

        Arc swoosh = new Arc(8, 8, 7.2, 7.2, 150, 70);
        swoosh.setType(ArcType.OPEN);
        swoosh.setFill(null);
        swoosh.setStroke(color);
        swoosh.setStrokeWidth(1.1);

        return new Group(swoosh, face, hourHand, minuteHand);
    }

    private Group globeIcon(Color color) {
        Circle outer = new Circle(8, 8, 5.5);
        outer.setFill(null);
        outer.setStroke(color);
        outer.setStrokeWidth(1.4);

        Ellipse meridian = new Ellipse(8, 8, 2.2, 5.5);
        meridian.setFill(null);
        meridian.setStroke(color);
        meridian.setStrokeWidth(1.1);

        Line equator = new Line(2.5, 8, 13.5, 8);
        equator.setStroke(color);
        equator.setStrokeWidth(1.1);

        return new Group(outer, meridian, equator);
    }
}

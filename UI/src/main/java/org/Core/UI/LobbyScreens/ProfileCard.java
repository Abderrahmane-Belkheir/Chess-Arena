package org.Core.UI.LobbyScreens;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.Core.Auth.UserSession;

public class ProfileCard {

    private final StackPane overlay;
    private final VBox card;
    private final UserSession session;
    private final ProfileCardController controller;


    public ProfileCard(UserSession session,
                       ProfileCardController controller,
                       StackPane overlay) {
        this.session    = session;
        this.controller = controller;
        this.overlay    = overlay;

        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) dismiss(); });

        card = new VBox(0);
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        card.setStyle("""
            -fx-background-color: #141412;
            -fx-background-radius: 20;
            -fx-border-color: #252522;
            -fx-border-radius: 20;
            -fx-border-width: 1;
        """);
        card.setOnMouseClicked(e -> e.consume());

        card.getChildren().addAll(
                buildTopAccent(),
                buildHeader(),
                buildAvatarZone(),
                buildFields()
        );

        overlay.getChildren().add(card);
    }

    private Region buildTopAccent() {
        Region accent = new Region();
        accent.setPrefHeight(3);
        accent.setMaxWidth(Double.MAX_VALUE);
        accent.setStyle("-fx-background-color: #81b64c;");
        return accent;
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 0, 20));

        Label label = new Label("MY PROFILE");
        label.setStyle("""
            -fx-text-fill: #444444;
            -fx-font-size: 11px;
            -fx-font-weight: bold;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setPrefSize(26, 26);
        closeBtn.setStyle("""
            -fx-background-color: #1e1e1b;
            -fx-text-fill: #555555;
            -fx-background-radius: 6;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-cursor: hand;
            -fx-padding: 0;
        """);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                closeBtn.getStyle().replace("#1e1e1b","#2a2a2a").replace("#555555","#ffffff")));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                closeBtn.getStyle().replace("#2a2a2a;","""
                    #1e1e1b;
                    -fx-border-color: #2a2a2a;
                """).replace("#ffffff","#555555")));
        closeBtn.setOnAction(e -> dismiss());

        header.getChildren().addAll(label, spacer, closeBtn);
        return header;
    }

    private HBox buildAvatarZone() {
        HBox zone = new HBox(18);
        zone.setAlignment(Pos.CENTER_LEFT);
        zone.setPadding(new Insets(24, 20, 20, 20));
        zone.setStyle("""
            -fx-border-color: transparent transparent #1e1e1b transparent;
            -fx-border-width: 1;
        """);

        // Avatar
        StackPane avatarWrap = new StackPane();
        avatarWrap.setPrefSize(84, 84);
        avatarWrap.setMinSize(84, 84);
        avatarWrap.setMaxSize(84, 84);

        Region circle = new Region();
        circle.setPrefSize(80, 80);
        circle.setMinSize(80, 80);
        circle.setMaxSize(80, 80);
        circle.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 40;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 40;
            -fx-border-width: 2;
        """, avatarColor(session.getUsername())));

        Label initials = new Label(initials(session.getUsername()));
        initials.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 26px;
            -fx-font-weight: 900;
        """);

        StackPane editBadge = new StackPane();
        editBadge.setPrefSize(24, 24);
        editBadge.setMinSize(24, 24);
        editBadge.setMaxSize(24, 24);
        editBadge.setStyle("""
            -fx-background-color: #81b64c;
            -fx-background-radius: 12;
            -fx-border-color: #141412;
            -fx-border-radius: 12;
            -fx-border-width: 2;
            -fx-cursor: hand;
        """);
        Label pencil = new Label("✎");
        pencil.setStyle("-fx-text-fill: #1a1a14; -fx-font-size: 11px;");
        editBadge.getChildren().add(pencil);
        StackPane.setAlignment(editBadge, Pos.BOTTOM_RIGHT);
        editBadge.setOnMouseEntered(e -> editBadge.setStyle(
                editBadge.getStyle().replace("#81b64c","#6da63e")));
        editBadge.setOnMouseExited(e -> editBadge.setStyle(
                editBadge.getStyle().replace("#6da63e","#81b64c")));
        editBadge.setOnMouseClicked(e -> controller.onChangeAvatar());

        avatarWrap.getChildren().addAll(circle, initials, editBadge);


        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(session.getUsername());
        name.setStyle("""
            -fx-text-fill: #f0f0f0;
            -fx-font-size: 18px;
            -fx-font-weight: 900;
        """);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);


        HBox eloBadge = new HBox(5);
        eloBadge.setAlignment(Pos.CENTER);
        eloBadge.setPadding(new Insets(3, 9, 3, 9));
        eloBadge.setStyle("""
            -fx-background-color: rgba(230,184,74,0.1);
            -fx-background-radius: 6;
            -fx-border-color: rgba(230,184,74,0.2);
            -fx-border-radius: 6;
            -fx-border-width: 1;
        """);
        Label trophyIcon = new Label("★");
        trophyIcon.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        Label eloVal = new Label(String.valueOf(session.getElo()));
        eloVal.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 12px; -fx-font-weight: 700;");
        eloBadge.getChildren().addAll(trophyIcon, eloVal);

        // Status
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER);
        Region dot = new Region();
        dot.setPrefSize(6, 6);
        dot.setStyle("-fx-background-color: #81b64c; -fx-background-radius: 3;");
        Label statusTxt = new Label("Online");
        statusTxt.setStyle("-fx-text-fill: #81b64c; -fx-font-size: 11px; -fx-font-weight: 700;");
        statusRow.getChildren().addAll(dot, statusTxt);

        metaRow.getChildren().addAll(eloBadge, statusRow);
        info.getChildren().addAll(name, metaRow);

        zone.getChildren().addAll(avatarWrap, info);
        return zone;
    }

    private VBox buildFields() {
        VBox fields = new VBox(6);
        fields.setPadding(new Insets(16, 20, 8, 20));

        fields.getChildren().addAll(
                buildLockedField("USERNAME",session.getUsername(),false),
                buildLockedField("PLAYER ID",  session.getId(),true),
                buildLockedField("ELO RATING", session.getElo() + " · Beginner",false)
        );
        return fields;
    }


    private HBox buildLockedField(String label, String value, boolean copyable) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setOpacity(0.85);
        row.setStyle("""
        -fx-background-color: #0f0f0d;
        -fx-background-radius: 10;
        -fx-border-color: #222222;
        -fx-border-radius: 10;
        -fx-border-width: 1;
    """);

        VBox body = new VBox(2);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #666660; -fx-font-size: 10px; -fx-font-weight: bold;");

        // shorten display but keep full value for copying
        String display = value.length() > 22 ? value.substring(0, 22) + "…" : value;
        Label val = new Label(display);
        val.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;");

        body.getChildren().addAll(lbl, val);


        if (copyable) {
            Button copyBtn = new Button("⎘");
            copyBtn.setStyle("""
            -fx-background-color: #1e1e1b;
            -fx-text-fill: #555555;
            -fx-background-radius: 6;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-font-size: 12px;
            -fx-padding: 3 7;
            -fx-cursor: hand;
        """);
            copyBtn.setOnMouseEntered(e -> copyBtn.setStyle(
                    copyBtn.getStyle().replace("#1e1e1b","#2a2a2a").replace("#555555","#ffffff")));
            copyBtn.setOnMouseExited(e -> copyBtn.setStyle(
                    copyBtn.getStyle().replace("#2a2a2a;","""
                    #1e1e1b;
                    -fx-border-color: #2a2a2a;
                """).replace("#ffffff","#555555")));
            copyBtn.setOnAction(e -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(value);
                clipboard.setContent(content);

                copyBtn.setText("✓");
                copyBtn.setStyle(copyBtn.getStyle().replace("#555555", "#81b64c"));

                PauseTransition pause = new PauseTransition(Duration.millis(1200));
                pause.setOnFinished(ev -> {
                    copyBtn.setText("⎘");
                    copyBtn.setStyle(copyBtn.getStyle().replace("#81b64c", "#555555"));
                });
                pause.play();
            });
            row.getChildren().addAll(body, copyBtn);
        } else {
            Label lock = new Label("🔒");
            lock.setStyle("-fx-font-size: 11px; -fx-opacity: 0.4;");
            row.getChildren().addAll(body, lock);
        }

        return row;
    }

    public void show() {
        overlay.setVisible(true);
        overlay.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setToValue(1.0);
        ft.play();
        card.setScaleX(0.93); card.setScaleY(0.93);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    public void dismiss() {
        FadeTransition ft = new FadeTransition(Duration.millis(160), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            overlay.setVisible(false);
            overlay.getChildren().remove(card);
        });
        ft.play();
    }

    private String initials(String u) {
        if (u == null || u.isEmpty()) return "?";
        String[] p = u.split("_");
        return p.length >= 2
                ? (p[0].substring(0,1) + p[1].substring(0,1)).toUpperCase()
                : u.substring(0, Math.min(2, u.length())).toUpperCase();
    }

    private String avatarColor(String u) {
        String[] palette = {"#7c5c3e","#5c3e7c","#3e7c5c","#7c3e5c","#3e5c7c","#5c7c3e"};
        return palette[Math.abs(u.hashCode()) % palette.length];
    }
}
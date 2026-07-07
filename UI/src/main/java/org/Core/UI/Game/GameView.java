package org.Core.UI.Game;


import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.Core.Auth.DTO.UserSession;
import org.Core.Game.Events.GameFound;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.Core.Game.Events.GameOverInfo;
import org.Core.Game.Events.OpponentMove;
import org.Core.Game.Events.PlayerMove;


import java.util.*;
/**
 * GameView — full game screen.
 *
 * Layout (matches screenshot):
 * ┌──────────┬──────────────────────────────┬──────────┐
 * │ Left     │        Chess Board           │ Right    │
 * │ sidebar  │  8x8 squares + pieces        │ actions  │
 * │ opponent │                              │ ½ resign │
 * │ captured │                              │          │
 * │ clock    │                              │          │
 * │ my info  │                              │          │
 * └──────────┴──────────────────────────────┴──────────┘
 *
 * Usage:
 *   GameView view = new GameView(root, gameFound, currentSession, navigator);
 *   // move received from server:
 *   view.applyServerMove("e2", "e4", newFen);
 *   // reconnect:
 *   view.renderFromFen(currentFen);
 */
import com.github.bhlangonijr.chesslib.*;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.Core.UI.LobbyScreens.Friends.Avatar;


import java.util.*;

import static org.Core.UI.LobbyScreens.Friends.Avatar.initials;

public class GameView {

    private static final EnumMap<Piece, Image> PIECE_IMAGES = new EnumMap<>(Piece.class);

    static {
        for(Piece piece:Piece.values()){
            if(piece!=Piece.NONE){
                PIECE_IMAGES.put(piece,
                        new Image(Objects.requireNonNull(
                                GameView.class.getResourceAsStream("/Pieces/"+piece.name()+".png"))));
            }
        }

    }

    // ── Board palette (warm wood-matched) ──────────────────────────────
    private static final String LIGHT_SQUARE      = "#f2e3c8";
    private static final String DARK_SQUARE       = "#8b5e34";
    private static final String SELECT_COLOR      = "#f0d878";
    private static final String SELECT_DARK       = "#c9a548";
    private static final String LAST_MOVE_LIGHT   = "#e8cf9a";
    private static final String LAST_MOVE_DARK    = "#b9873f";

    // ── UI palette (warm wood-matched) ─────────────────────────────────
    private static final String BG_TOP            = "#2b2420";
    private static final String BG_BOTTOM         = "#181310";
    private static final String PANEL_TOP         = "#2a2018";
    private static final String PANEL_BOTTOM      = "#180f0a";
    private static final String CARD_BG           = "rgba(232,207,138,0.035)";
    private static final String CARD_BORDER       = "rgba(232,207,138,0.09)";
    private static final String ACCENT            = "#e8cf8a";
    private static final String ACCENT_DIM        = "#8a6a30";
    private static final String ACCENT_GLOW       = "rgba(232,207,138,0.5)";
    private static final String TEXT_PRIMARY      = "#f5ece0";
    private static final String TEXT_SECONDARY    = "#a08e78";
    private static final String CLOCK_BG_IDLE     = "#1c140d";
    private static final String CLOCK_BG_ACTIVE   = "#4a3a1c";
    private static final String CLOCK_BORDER_IDLE = "rgba(232,207,138,0.08)";
    private static final String BTN_BG            = "#332821";
    private static final String BTN_BG_HOVER      = "#443528";
    private static final String RESIGN_COLOR      = "#e0645a";
    private static final String DRAW_COLOR        = "#d8c8a8";

    private static final String WOOD_LIGHT        = "#9c6b3f";
    private static final String WOOD_MID          = "#7a4d29";
    private static final String WOOD_DARK         = "#3e2415";
    private static final String WOOD_HIGHLIGHT    = "#b98550";
    private static final String BRASS_LIGHT       = "#e8cf8a";
    private static final String BRASS_DARK        = "#8a6a30";

    // ── Precomputed style strings ──────────────────────────────────────
    // These used to be rebuilt via string concatenation on every square
    // repaint (i.e. on every click and every move). Since the underlying
    // colors never change at runtime, we build them once at class-load
    // time and just pick between the two cached instances afterward.
    private static final String SQ_STYLE_LIGHT       = "-fx-background-color: " + LIGHT_SQUARE + ";";
    private static final String SQ_STYLE_DARK        = "-fx-background-color: " + DARK_SQUARE + ";";
    private static final String SELECT_STYLE_LIGHT   = "-fx-background-color: " + SELECT_COLOR + ";";
    private static final String SELECT_STYLE_DARK    = "-fx-background-color: " + SELECT_DARK + ";";
    private static final String LASTMOVE_STYLE_LIGHT = "-fx-background-color: " + LAST_MOVE_LIGHT + ";";
    private static final String LASTMOVE_STYLE_DARK  = "-fx-background-color: " + LAST_MOVE_DARK + ";";

    private static final String RING_ACTIVE = "-fx-border-color: " + ACCENT
            + "; -fx-border-width: 2.5; -fx-border-radius: 100; -fx-effect: dropshadow(gaussian, "
            + ACCENT_GLOW + ", 10, 0.3, 0, 0);";
    private static final String RING_IDLE =
            "-fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1.5; -fx-border-radius: 100;";

    private static final String BTN_STYLE_IDLE =
            "-fx-background-color: " + BTN_BG + ";" +
                    "-fx-background-radius: 50%;" +
                    "-fx-border-color: rgba(255,255,255,0.06);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 50%;" +
                    "-fx-cursor: hand;";
    private static final String BTN_STYLE_HOVER =
            "-fx-background-color: " + BTN_BG_HOVER + ";" +
                    "-fx-background-radius: 50%;" +
                    "-fx-border-color: rgba(255,255,255,0.06);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 50%;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.2, 0, 2);";

    private static final String CLOCK_GLOW_ACTIVE =
            "-fx-effect: dropshadow(gaussian, " + ACCENT_GLOW + ", 12, 0.25, 0, 0);";
    private static final String CLOCK_GLOW_IDLE =
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 6, 0, 0, 2);";

    // ── Root wallpaper (wood-grain, fills the whole screen) ─────────────
    // Built from the exact same wood palette as boardFrame (WOOD_HIGHLIGHT →
    // WOOD_MID → WOOD_DARK) so the wallpaper reads as the same wood, just
    // darker/duller so the board still pops as the focal point. Layers: base
    // diagonal wood gradient, a dark overlay to recede it, fine grain streaks,
    // broader grain bands, a soft amber glow, and a warm vignette at the edges.
    // NOTE: uses JavaFX's own linear-gradient/radial-gradient syntax (point-based
    // direction, "repeat" cycle keyword) rather than CSS3 "Ndeg"/"repeating-linear-gradient"
    // syntax, which JavaFX's CSS parser does not support.
    private static final String WALLPAPER_STYLE =
            "-fx-background-color: " +
                    "linear-gradient(from 0% 0% to 100% 100%, " + WOOD_HIGHLIGHT + " 0%, " + WOOD_MID + " 45%, " + WOOD_DARK + " 100%)," +
                    "linear-gradient(to bottom, rgba(0,0,0,0.32), rgba(0,0,0,0.52))," +
                    "linear-gradient(from 0px 0px to 4px 4px, repeat, rgba(0,0,0,0.12) 0%, rgba(0,0,0,0.12) 50%, rgba(255,255,255,0.03) 50%, rgba(255,255,255,0.03) 100%)," +
                    "linear-gradient(from 0px 0px to 110px 110px, repeat, rgba(122,77,41,0.26) 0%, rgba(122,77,41,0.26) 50%, rgba(185,133,80,0.14) 50%, rgba(185,133,80,0.14) 100%)," +
                    "radial-gradient(center 50% 32%, radius 65%, rgba(232,207,138,0.04), rgba(232,207,138,0) 70%)," +
                    "radial-gradient(center 50% 42%, radius 78%, rgba(0,0,0,0) 42%, rgba(20,12,6,0.5) 100%);";

    private static final double LEFT_RATIO   = 0.26;
    private static final double RIGHT_RATIO  = 0.09;
    private static final int    MIN_SQUARE   = 48;
    private static final int    MAX_SQUARE   = 120;
    private static final double BOARD_SCALE  = 0.86; // shrinks board + pieces together

    private static final double MOVE_ANIM_MS = 160;
    private static final double MAX_CLOCK_GAP = 240; // cap so the gap between clocks doesn't blow up on big boards

    // ── State ─────────────────────────────────────────────────────────
    private String gameId;
    private final Board              board          = new Board();
    private final UserSession        session;
    private Square                   selectedSquare = null;
    private List<Square>             legalTargets   = new ArrayList<>();
    private String                   lastMoveFrom   = null;
    private String                   lastMoveTo     = null;
    private boolean                  myTurn;
    private String                   fen;
    private final GameFound.Opponent opponent;
    private final Side               mySide;

    private double squareSize = 96;

    // ── Clocks ────────────────────────────────────────────────────────
    private final Timeline clockTimeline = new Timeline();
    private int myTimeSeconds  = 600; // 10:00 default, matches initial labels
    private int oppTimeSeconds = 600;

    // Cached clock CSS — rebuilt only when size changes (applySize), not on every move.
    private String clockStyleActive;
    private String clockStyleIdle;

    // ── UI nodes ──────────────────────────────────────────────────────
    private final StackPane      root         = new StackPane();
    private final GridPane       boardGrid    = new GridPane();
    private final StackPane[][]  squares      = new StackPane[8][8];
    private final StackPane      boardWrap    = new StackPane();
    private final Pane           floatPane    = new Pane();

    private final  HBox centerRow=new HBox(0);
    private final StackPane      boardFrame   = new StackPane();
    private final StackPane      boardGroove  = new StackPane();
    private final StackPane      boardOuter   = new StackPane();
    private final Circle         pinTL = new Circle(4);
    private final Circle         pinTR = new Circle(4);
    private final Circle         pinBL = new Circle(4);
    private final Circle         pinBR = new Circle(4);
    private final Region         ambientGlow  = new Region();
    private final HBox           topBar       = new HBox(8);
    private final Circle         liveDot      = new Circle(4);
    // Sidebars
    private final VBox           leftSidebar  = new VBox(0);
    private final VBox           rightSidebar = new VBox(14);
    private final Region         clockSpacer  = new Region();

    // Player / clock widgets
    private final StackPane      oppAvatarPane;
    private final StackPane      myAvatarPane;
    private final Label          oppNameLbl;
    private final Label          myNameLbl;
    private final Label          oppEloLbl;
    private final Label          myEloLbl;
    private final Label          myClockLabel  = new Label("10:00");
    private final Label          oppClockLabel = new Label("10:00");
    private final HBox           oppCaptured   = new HBox(2);
    private final HBox           myCaptured    = new HBox(2);
    private VBox                 oppInfoCard;
    private VBox                 myInfoCard;

    // Action buttons
    private final StackPane      drawBtn;
    private final StackPane      resignBtn;

    private double currentBtnSize = 40;

    private final MatchmakingHandler matchmakingHandler;
    private final Runnable onReturnToLobby;
    private final Consumer<PlayerMove> onSendMove;

    // ── Constructor ───────────────────────────────────────────────────
    public GameView(String gameId, String fen, UserSession session, Side playerColor,
                    GameFound.Opponent opponent,MatchmakingHandler matchmakingHandler,Runnable onReturnToLobby, Consumer<PlayerMove> onSendMove) {
        this.gameId=gameId;
        this.fen      = fen;
        this.session  = session;
        this.opponent = opponent;
        this.mySide   = playerColor;
        this.myTurn   = mySide == Side.WHITE;
        this.matchmakingHandler=matchmakingHandler;
        this.onReturnToLobby=onReturnToLobby;
        this.onSendMove=onSendMove;
        System.out.println("avatar "+opponent.getAvatarUrl());

        oppAvatarPane = Avatar.build(opponent.getAvatarUrl(),
                initials(opponent.getUsername()), Avatar.colorFromName(opponent.getUsername()), 44);
        myAvatarPane  = Avatar.build(session.getAvatarUrl(),
                initials(session.getUsername()),  Avatar.colorFromName(session.getUsername()),  44);

        oppNameLbl = styledLabel(opponent.getUsername(), TEXT_PRIMARY, 14, 700);
        myNameLbl  = styledLabel(session.getUsername(),  TEXT_PRIMARY, 14, 700);
        oppEloLbl  = styledLabel(opponent.getElo() + " ELO", TEXT_SECONDARY, 12, 600);
        myEloLbl   = styledLabel(session.getElo()  + " ELO", TEXT_SECONDARY, 12, 600);

        drawBtn   = buildActionBtn("½", DRAW_COLOR,   "Offer draw", () -> System.out.println("Draw offered"));
        resignBtn = buildActionBtn("⚑", RESIGN_COLOR, "Resign",     () -> System.out.println("Resigned"));

        buildLayout();
        attachResizeListeners();
        renderFromFen(fen);
        refreshTurnIndicators();
        startClockTicking();
    }

    // ── Public API ────────────────────────────────────────────────────

    public void renderFromFen(String fen) {
        board.loadFromFen(fen);
        drawAllPieces();
        clearHighlights();
        selectedSquare = null;
        legalTargets.clear();
    }

    public void applyOpponentMove(OpponentMove gameMove) {
        Platform.runLater(() -> {
            Square from = Square.fromValue(gameMove.getFrom().toUpperCase());
            Square to   = Square.fromValue(gameMove.getTo().toUpperCase());

            animateMove(from, to, () -> {
                Move move = new Move(from, to);
                board.doMove(move);

                lastMoveFrom = from.value().toLowerCase();
                lastMoveTo   = to.value().toLowerCase();
                redrawSquare(lastMoveFrom);
                redrawSquare(lastMoveTo);
                if(gameMove.getGameOverInfo()!=null){
                    showGameOverCard(gameMove.getGameOverInfo());
                };

                myTurn = true;
                refreshTurnIndicators();
            });
        });
    }

    public void gameOver(GameOverInfo gameOverInfo){
        showGameOverCard(gameOverInfo);
    }
    private void showGameOverCard(GameOverInfo info) {
        Platform.runLater(() -> {
            stopClocks();
            session.setElo(info.getNewElo());
            GameOverCard card = new GameOverCard(info, session, boardWrap,()->matchmakingHandler.startGameSearching(root), onReturnToLobby);
        });
    }

    public void updateClocks(String myTime, String oppTime) {
        Platform.runLater(() -> {
            myTimeSeconds  = parseTime(myTime);
            oppTimeSeconds = parseTime(oppTime);
            myClockLabel.setText(myTime);
            oppClockLabel.setText(oppTime);
        });
    }

    /** Stops both clocks from ticking, e.g. on checkmate/resignation/game end. */
    public void stopClocks() {
        clockTimeline.stop();
    }

    public StackPane getView() { return root; }

    // ── Clock ticking ─────────────────────────────────────────────────

    private void startClockTicking() {
        clockTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1), e -> tickClock())
        );
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    private void tickClock() {
        if (myTurn) {
            if (myTimeSeconds > 0) myTimeSeconds--;
            myClockLabel.setText(formatTime(myTimeSeconds));
        } else {
            if (oppTimeSeconds > 0) oppTimeSeconds--;
            oppClockLabel.setText(formatTime(oppTimeSeconds));
        }
    }

    // Runs every second while the game is live, so avoid String.format's
    // locale/parsing overhead here — plain concatenation is meaningfully cheaper.
    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return s < 10 ? (m + ":0" + s) : (m + ":" + s);
    }

    private int parseTime(String time) {
        try {
            String[] parts = time.split(":");
            int m = Integer.parseInt(parts[0].trim());
            int s = Integer.parseInt(parts[1].trim());
            return m * 60 + s;
        } catch (Exception e) {
            return 0;
        }
    }

    // ── Turn indicators (new) ──────────────────────────────────────────

    /** Updates clock + avatar styling to reflect whose turn it is. Call whenever myTurn changes. */
    private void refreshTurnIndicators() {
        // Clock CSS only depends on size (font/width), which is cached and rebuilt
        // solely by applySize(). Here we just swap between the two cached strings
        // instead of recomputing/formatting them on every move.
        if (clockStyleActive == null) {
            recomputeClockStyles(currentClockFont(), currentClockWidth());
        } else {
            myClockLabel.setStyle(myTurn ? clockStyleActive : clockStyleIdle);
            oppClockLabel.setStyle(!myTurn ? clockStyleActive : clockStyleIdle);
        }
        styleAvatarRing(myAvatarPane,  myTurn);
        styleAvatarRing(oppAvatarPane, !myTurn);
    }

    private double currentClockFont() {
        return Math.max(14, Math.round(squareSize * 0.27));
    }

    private double currentClockWidth() {
        double boardPx   = squareSize * 8;
        double leftWidth = Math.round(boardPx * LEFT_RATIO);
        double innerPad  = Math.max(8, leftWidth * 0.08);
        return leftWidth - innerPad * 2;
    }

    private void styleAvatarRing(StackPane avatar, boolean active) {
        Object bg = avatar.getProperties().get("bgStyle");
        avatar.setStyle((bg != null ? bg.toString() : "") + (active ? RING_ACTIVE : RING_IDLE));
    }

    // ── Layout ────────────────────────────────────────────────────────

    private void buildLayout() {
        root.setStyle(WALLPAPER_STYLE);
        root.setMinSize(0, 0);

        // Ambient glow sitting behind everything, centered on the board area
        ambientGlow.setMouseTransparent(true);
        ambientGlow.setStyle(
                "-fx-background-color: radial-gradient(center 50% 46%, radius 55%, " +
                        "rgba(129,182,76,0.10), rgba(129,182,76,0.0) 70%);"
        );
        ambientGlow.prefWidthProperty().bind(root.widthProperty());
        ambientGlow.prefHeightProperty().bind(root.heightProperty());

        boardGrid.setGridLinesVisible(false);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane sq = new StackPane();
                boolean isLight = (row + col) % 2 != 0;
                sq.setStyle(isLight ? SQ_STYLE_LIGHT : SQ_STYLE_DARK);
                final int r = row, c = col;
                sq.setOnMouseClicked(e -> handleSquareClick(r, c));
                squares[row][col] = sq;
                boardGrid.add(sq, col, row);
            }
        }

        addCoordinateLabels();

        floatPane.setMouseTransparent(true);
        floatPane.setStyle("-fx-background-color: transparent;");

        boardWrap.getChildren().addAll(boardGrid, floatPane);
        boardWrap.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(0,0,0,0.6); -fx-border-width: 1;");
        boardWrap.setSnapToPixel(true);
        floatPane.prefWidthProperty().bind(boardWrap.widthProperty());
        floatPane.prefHeightProperty().bind(boardWrap.heightProperty());

        // Beveled wooden frame wrapping the board:
        // boardFrame  = outer wood molding (thick, warm gradient + faux grain streaks)
        // boardGroove = a darker recessed "channel" so the board looks set INTO the wood
        // boardWrap   = the actual chess board, sitting inside the groove
        boardGroove.getChildren().add(boardWrap);
        boardGroove.setStyle(
                "-fx-background-color: " + WOOD_DARK + ";" +
                        "-fx-background-radius: 3;" +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.8), 12, 0.5, 0, 3);"
        );

        boardFrame.getChildren().add(boardGroove);
        boardFrame.setStyle(
                "-fx-background-color: " +
                        "linear-gradient(from 0% 0% to 100% 100%, " + WOOD_HIGHLIGHT + " 0%, " + WOOD_MID + " 45%, " + WOOD_DARK + " 100%)," +
                        "linear-gradient(from 0px 0px to 4px 4px, repeat, rgba(0,0,0,0.10) 0%, rgba(0,0,0,0.10) 50%, rgba(255,255,255,0.035) 50%, rgba(255,255,255,0.035) 100%);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-color: linear-gradient(from 0% 0% to 100% 100%, " + WOOD_HIGHLIGHT + ", " + WOOD_DARK + ");"
        );
        boardFrame.setEffect(new DropShadow(34, Color.color(0, 0, 0, 0.65)));

        // Brass corner pins — small decorative touch, sit on top of the wood molding
        for (Circle pin : new Circle[]{pinTL, pinTR, pinBL, pinBR}) {
            pin.setFill(new RadialGradient(0, 0, 0.35, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web(BRASS_LIGHT)), new Stop(1, Color.web(BRASS_DARK))));
            pin.setStroke(Color.color(0, 0, 0, 0.4));
            pin.setStrokeWidth(0.6);
            pin.setEffect(new DropShadow(3, Color.color(0, 0, 0, 0.6)));
            pin.setMouseTransparent(true);
        }
        double pinMargin = 8;
        StackPane.setAlignment(pinTL, Pos.TOP_LEFT);
        StackPane.setAlignment(pinTR, Pos.TOP_RIGHT);
        StackPane.setAlignment(pinBL, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(pinBR, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(pinTL, new Insets(pinMargin));
        StackPane.setMargin(pinTR, new Insets(pinMargin));
        StackPane.setMargin(pinBL, new Insets(pinMargin));
        StackPane.setMargin(pinBR, new Insets(pinMargin));

        boardOuter.getChildren().addAll(boardFrame, pinTL, pinTR, pinBL, pinBR);

        // Left sidebar
        leftSidebar.setAlignment(Pos.CENTER);
        leftSidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + PANEL_TOP + ", " + PANEL_BOTTOM + ")," +
                        "radial-gradient(center 15% 0%, radius 70%, rgba(232,207,138,0.045), rgba(232,207,138,0) 60%);" +
                        "-fx-background-radius: 0 14 14 0;" +
                        "-fx-border-color: linear-gradient(to bottom, " + WOOD_HIGHLIGHT + ", " + WOOD_DARK + ");" +
                        "-fx-border-width: 0 2 0 0;" +
                        "-fx-border-radius: 0 14 14 0;"
        );

        oppInfoCard = buildPlayerInfoBox(oppAvatarPane, oppNameLbl, oppEloLbl);
        oppCaptured.setPadding(new Insets(2, 0, 4, 0));
        oppCaptured.setAlignment(Pos.CENTER_LEFT);
        VBox oppClockBox = wrapClock(oppClockLabel);

        VBox topGroup = new VBox(6);
        topGroup.getChildren().addAll(oppInfoCard, oppCaptured, oppClockBox);

        clockSpacer.setMaxHeight(MAX_CLOCK_GAP);
        VBox.setVgrow(clockSpacer, Priority.ALWAYS);

        VBox myClockBox  = wrapClock(myClockLabel);
        myCaptured.setPadding(new Insets(4, 0, 2, 0));
        myCaptured.setAlignment(Pos.CENTER_LEFT);
        myInfoCard = buildPlayerInfoBox(myAvatarPane, myNameLbl, myEloLbl);

        VBox bottomGroup = new VBox(6);
        bottomGroup.getChildren().addAll(myClockBox, myCaptured, myInfoCard);

        leftSidebar.getChildren().addAll(topGroup, clockSpacer, bottomGroup);

        // Right sidebar
        rightSidebar.setAlignment(Pos.CENTER);
        rightSidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + PANEL_TOP + ", " + PANEL_BOTTOM + ")," +
                        "radial-gradient(center 85% 0%, radius 70%, rgba(232,207,138,0.045), rgba(232,207,138,0) 60%);" +
                        "-fx-background-radius: 14 0 0 14;" +
                        "-fx-border-color: linear-gradient(to bottom, " + WOOD_HIGHLIGHT + ", " + WOOD_DARK + ");" +
                        "-fx-border-width: 0 0 0 2;" +
                        "-fx-border-radius: 14 0 0 14;"
        );
        rightSidebar.getChildren().addAll(drawBtn, resignBtn);

        // Accent divider strips along the top edge of each sidebar panel
        Region leftAccent = accentStrip("0 8 0 0");
        StackPane leftPanelWrap = new StackPane(leftSidebar, leftAccent);
        StackPane.setAlignment(leftAccent, Pos.TOP_CENTER);

        Region rightAccent = accentStrip("8 0 0 0");
        StackPane rightPanelWrap = new StackPane(rightSidebar, rightAccent);
        StackPane.setAlignment(rightAccent, Pos.TOP_CENTER);

        centerRow.setAlignment(Pos.CENTER);
        centerRow.setMinSize(0, 0);
        centerRow.setSpacing(0);
        centerRow.getChildren().addAll(leftPanelWrap, boardOuter, rightPanelWrap);

        // Top status bar — pulsing "LIVE" indicator + fading accent divider,
        // width-matched to the board+sidebars row beneath it.
        liveDot.setFill(Color.web(ACCENT));
        liveDot.setEffect(new DropShadow(8, Color.web(ACCENT_GLOW)));
        FadeTransition pulse = new FadeTransition(Duration.millis(900), liveDot);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.35);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        Label liveLbl = styledLabel("LIVE", ACCENT, 11, 800);
        Region topDivider = new Region();
        topDivider.setPrefHeight(1);
        topDivider.setMaxHeight(1);
        topDivider.setStyle("-fx-background-color: linear-gradient(to right, rgba(129,182,76,0.35), rgba(129,182,76,0));");
        HBox.setHgrow(topDivider, Priority.ALWAYS);

        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 10, 4));
        topBar.getChildren().addAll(liveDot, liveLbl, topDivider);
        topBar.prefWidthProperty().bind(centerRow.widthProperty());
        topBar.setMaxWidth(Region.USE_PREF_SIZE);

        VBox mainColumn = new VBox(0);
        mainColumn.setAlignment(Pos.CENTER);
        mainColumn.getChildren().addAll(topBar, centerRow);

        root.getChildren().addAll(ambientGlow, mainColumn);
        StackPane.setAlignment(mainColumn, Pos.CENTER);
    }

    /** Thin accent-gradient strip pinned to the top edge of a sidebar panel. */
    private Region accentStrip(String radii) {
        Region strip = new Region();
        strip.setMouseTransparent(true);
        strip.setPrefHeight(3);
        strip.setMaxHeight(3);
        strip.setStyle(
                "-fx-background-color: linear-gradient(to right, transparent, " + ACCENT + ", transparent);" +
                        "-fx-background-radius: " + radii + ";"
        );
        return strip;
    }

    private void addCoordinateLabels() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isLight = (row + col) % 2 != 0;
                String textColor = isLight ? DARK_SQUARE : LIGHT_SQUARE;

                if (col == 0) {
                    int rankNum = (mySide == Side.WHITE) ? (8 - row) : (row + 1);
                    Label rankLbl = new Label(String.valueOf(rankNum));
                    rankLbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 10px; -fx-font-weight: 800;");
                    rankLbl.setMouseTransparent(true);
                    rankLbl.setUserData("coord");
                    StackPane.setAlignment(rankLbl, Pos.TOP_LEFT);
                    StackPane.setMargin(rankLbl, new Insets(3, 0, 0, 3));
                    squares[row][col].getChildren().add(rankLbl);
                }

                if (row == 7) {
                    char fileLetter = (mySide == Side.WHITE) ? (char)('a' + col) : (char)('h' - col);
                    Label fileLbl = new Label(String.valueOf(fileLetter));
                    fileLbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 10px; -fx-font-weight: 800;");
                    fileLbl.setMouseTransparent(true);
                    fileLbl.setUserData("coord");
                    StackPane.setAlignment(fileLbl, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(fileLbl, new Insets(0, 3, 3, 0));
                    squares[row][col].getChildren().add(fileLbl);
                }
            }
        }
    }

    // ── Resize ────────────────────────────────────────────────────────

    private void attachResizeListeners() {
        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> recomputeSize();
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener(sizeListener);
                newScene.heightProperty().addListener(sizeListener);
                recomputeSize();
            }
        });
        root.widthProperty().addListener(sizeListener);
        root.heightProperty().addListener(sizeListener);
    }

    private void recomputeSize() {
        double w, h;
        if (root.getScene() != null) {
            w = root.getScene().getWidth();
            h = root.getScene().getHeight();
        } else {
            w = root.getWidth();
            h = root.getHeight();
        }
        if (w <= 0 || h <= 0) return;

        double maxFromWidth  = w / (1.0 + LEFT_RATIO + RIGHT_RATIO);
        double maxFromHeight = h;
        double boardPx = Math.min(maxFromWidth, maxFromHeight);

        double sq = Math.floor((boardPx / 8.0) * BOARD_SCALE);
        sq = Math.max(MIN_SQUARE, Math.min(MAX_SQUARE, sq));

        if (Math.abs(sq - squareSize) < 0.1) return;
        squareSize = sq;
        applySize(squareSize);
    }

    private void applySize(double sq) {
        double boardPx      = sq * 8;
        double leftWidth    = Math.round(boardPx * LEFT_RATIO);
        double rightWidth   = Math.round(boardPx * RIGHT_RATIO);
        double innerPad     = Math.max(8, leftWidth * 0.08);
        double avatarSize   = Math.max(28, Math.round(sq * 0.46));
        double nameFontSize = Math.max(10, Math.round(sq * 0.145));
        double eloFontSize  = Math.max(9,  Math.round(sq * 0.125));
        double clockFont    = Math.max(14, Math.round(sq * 0.27));
        double btnSize      = Math.max(36, Math.round(sq * 0.46));
        double frameThick   = Math.max(10, Math.round(sq * 0.18));
        double grooveThick  = Math.max(3,  Math.round(sq * 0.045));
        double pinRadius    = Math.max(3,  sq * 0.05);

        boardFrame.setPadding(new Insets(frameThick));
        boardGroove.setPadding(new Insets(grooveThick));
        for (Circle pin : new Circle[]{pinTL, pinTR, pinBL, pinBR}) {
            pin.setRadius(pinRadius);
        }

        boardWrap.setPrefSize(boardPx, boardPx);
        boardWrap.setMinSize(boardPx, boardPx);
        boardWrap.setMaxSize(boardPx, boardPx);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                squares[r][c].setPrefSize(sq, sq);
                squares[r][c].setMinSize(sq, sq);
                squares[r][c].setMaxSize(sq, sq);
            }
        }

        leftSidebar.setPrefSize(leftWidth, boardPx);
        leftSidebar.setMinSize(leftWidth, boardPx);
        leftSidebar.setMaxSize(leftWidth, boardPx);
        leftSidebar.setPadding(new Insets(innerPad, innerPad, innerPad, innerPad));

        resizeAvatar(oppAvatarPane, (int) avatarSize);
        resizeAvatar(myAvatarPane,  (int) avatarSize);

        oppNameLbl.setStyle(playerNameStyle(nameFontSize));
        myNameLbl .setStyle(playerNameStyle(nameFontSize));
        oppEloLbl .setStyle(eloStyle(eloFontSize));
        myEloLbl  .setStyle(eloStyle(eloFontSize));

        double clockWidth = leftWidth - innerPad * 2;
        recomputeClockStyles(clockFont, clockWidth);

        rightSidebar.setPrefSize(rightWidth, boardPx);
        rightSidebar.setMinSize(rightWidth, boardPx);
        rightSidebar.setMaxSize(rightWidth, boardPx);

        currentBtnSize = btnSize;
        resizeActionBtn(drawBtn,   btnSize);
        resizeActionBtn(resignBtn, btnSize);

        clearHighlights();
    }

    // ── Piece rendering ───────────────────────────────────────────────

    private void drawAllPieces() {
        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;
            int[] rc = squareToRowCol(square);
            clearSquareUI(rc[0], rc[1]);
            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) placePiece(piece, rc[0], rc[1]);
        }
    }

    /**
     * Redraws a single square: resets its background color then redraws piece.
     * Resetting color here is the key fix — clearSquareUI only removes children,
     * it never touches the -fx-background-color style.
     */
    private void redrawSquare(String squareName) {
        Square sq = Square.fromValue(squareName.toUpperCase());
        if (sq == Square.NONE) return;
        int[] rc = squareToRowCol(sq);
        resetSquareColor(rc[0], rc[1]);   // ← fix: reset color before redrawing
        clearSquareUI(rc[0], rc[1]);
        Piece piece = board.getPiece(sq);
        if (piece != Piece.NONE) placePiece(piece, rc[0], rc[1]);
    }

    private void placePiece(Piece piece, int row, int col) {
        final int PIECE_LOAD_SIZE = 96;
        try {
            Image img = PIECE_IMAGES.get(piece);
            ImageView iv=new ImageView(img);
            iv.setFitHeight(PIECE_LOAD_SIZE);
            iv.setFitWidth(PIECE_LOAD_SIZE);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setMouseTransparent(true);

//            DropShadow shadow = new DropShadow();
//            shadow.setColor(Color.color(0, 0, 0, 0.45));
//            shadow.setRadius(6);
//            shadow.setOffsetY(2);
//            iv.setEffect(shadow);

            StackPane sq = squares[row][col];
            iv.fitWidthProperty().bind(sq.widthProperty().subtract(8));
            iv.fitHeightProperty().bind(sq.heightProperty().subtract(8));
            sq.getChildren().add(iv);
        } catch (Exception e) {
            Label fallback = new Label(piece.getFenSymbol());
            fallback.setStyle("-fx-text-fill: white; -fx-font-size: 28px;");
            fallback.setMouseTransparent(true);
            squares[row][col].getChildren().add(fallback);
        }
    }

    private void clearSquareUI(int row, int col) {
        squares[row][col].getChildren().removeIf(n -> !"coord".equals(n.getUserData()));
    }

    // ── Color helpers (targeted — never loop all 64) ──────────────────

    /** Resets a single square to its natural light/dark board color. */
    private void resetSquareColor(int row, int col) {
        boolean isLight = (row + col) % 2 != 0;
        squares[row][col].setStyle(isLight ? SQ_STYLE_LIGHT : SQ_STYLE_DARK);
    }

    private void resetSquareColorByName(String squareName) {
        if (squareName == null) return;
        Square sq = Square.fromValue(squareName.toUpperCase());
        if (sq == Square.NONE) return;
        int[] rc = squareToRowCol(sq);
        resetSquareColor(rc[0], rc[1]);
    }

    /**
     * Clears only the squares that were actually highlighted:
     * the selected square, legal-move dots, and the previous last-move tint.
     * Never loops all 64.
     */
    private void clearHighlights() {
        // 1. Reset selected square
        if (selectedSquare != null) {
            int[] rc = squareToRowCol(selectedSquare);
            resetSquareColor(rc[0], rc[1]);
        }

        // 2. Reset legal-move target squares and remove dots/rings
        for (Square target : legalTargets) {
            int[] rc = squareToRowCol(target);
            resetSquareColor(rc[0], rc[1]);
            squares[rc[0]][rc[1]].getChildren().removeIf(n -> "dot".equals(n.getUserData()));
        }

        // 3. Reset previous last-move tint
        resetSquareColorByName(lastMoveFrom);
        resetSquareColorByName(lastMoveTo);

//        // 4. Re-apply last-move gold tint (keeps it visible after selection clears)
//        refreshLastMoveHighlight();
    }

    private void refreshLastMoveHighlight() {
        if (lastMoveFrom != null) tintLastMove(lastMoveFrom);
        if (lastMoveTo   != null) tintLastMove(lastMoveTo);
    }

    private void tintLastMove(String squareName) {
        Square sq = Square.fromValue(squareName.toUpperCase());
        if (sq == Square.NONE) return;
        int[] rc = squareToRowCol(sq);
        boolean isLight = (rc[0] + rc[1]) % 2 != 0;
        squares[rc[0]][rc[1]].setStyle(isLight ? LASTMOVE_STYLE_LIGHT : LASTMOVE_STYLE_DARK);
    }

    // ── Animated move ─────────────────────────────────────────────────

    private void animateMove(Square from, Square to, Runnable onDone) {
        int[] fromRC = squareToRowCol(from);
        int[] toRC   = squareToRowCol(to);

        Piece piece = board.getPiece(from);
        if (piece == Piece.NONE) { onDone.run(); return; }

        ImageView iv = buildPieceImageView(piece, (int) squareSize);
        if (iv == null) { onDone.run(); return; }

        double startX = fromRC[1] * squareSize;
        double startY = fromRC[0] * squareSize;
        double endX   = toRC[1]   * squareSize;
        double endY   = toRC[0]   * squareSize;

        iv.setLayoutX(startX);
        iv.setLayoutY(startY);
        iv.setFitWidth(squareSize - 8);
        iv.setFitHeight(squareSize - 8);

        DropShadow flyingShadow = new DropShadow();
        flyingShadow.setColor(Color.color(0, 0, 0, 0.55));
        flyingShadow.setRadius(14);
        flyingShadow.setOffsetY(5);
        iv.setScaleX(1.12);
        iv.setScaleY(1.12);

        floatPane.getChildren().add(iv);

        clearSquareUI(fromRC[0], fromRC[1]);
        resetSquareColor(fromRC[0], fromRC[1]);  // ← add this line

        TranslateTransition tt = new TranslateTransition(Duration.millis(MOVE_ANIM_MS), iv);
        tt.setFromX(0);
        tt.setFromY(0);
        tt.setToX(endX - startX);
        tt.setToY(endY - startY);
        tt.setInterpolator(Interpolator.EASE_BOTH);

        tt.setOnFinished(evt -> {
            floatPane.getChildren().remove(iv);
            onDone.run();
        });

        tt.play();
    }

    private ImageView buildPieceImageView(Piece piece, int size) {
        Image image = PIECE_IMAGES.get(piece);
        if (image == null) {
            return null;
        }

        ImageView iv = new ImageView(image);
        iv.setFitWidth(size - 8);
        iv.setFitHeight(size - 8);
        iv.setPreserveRatio(true);

        return iv;
    }

    // ── Click & move logic ────────────────────────────────────────────

    private void handleSquareClick(int row, int col) {
        Square clicked = rowColToSquare(row, col);

        if (selectedSquare == null) {
            // first click — select piece
            Piece piece = board.getPiece(clicked);
            if (piece == Piece.NONE) return;
            if (!myTurn) return;
            if (piece.getPieceSide() != mySide) return;

            selectedSquare = clicked;
            legalTargets = board.legalMoves().stream()
                    .filter(m -> m.getFrom() == clicked)
                    .map(Move::getTo)
                    .collect(Collectors.toList());

            highlightSelected(row, col);
            highlightLegalMoves();

        } else {
            // second click
            if (legalTargets.contains(clicked)) {
                // ← THIS WAS MISSING — commit the move
                commitMove(selectedSquare, clicked);
            } else {
                // deselect or re-select another piece
                clearHighlights();
                selectedSquare = null;
                legalTargets.clear();

                Piece piece = board.getPiece(clicked);
                if (piece != Piece.NONE && myTurn && piece.getPieceSide() == mySide) {
                    handleSquareClick(row, col);
                }
            }
        }
    }
    private void commitMove(Square fromSq, Square toSq){
        clearHighlights();
        selectedSquare = null;
        legalTargets.clear();
        myTurn=false;
        animateMove(fromSq, toSq, () -> {
            Move move = new Move(fromSq, toSq);
            board.doMove(move);
            String from = fromSq.value().toLowerCase();
            String to   = toSq.value().toLowerCase();
            lastMoveFrom = from;
            lastMoveTo   = to;
            redrawSquare(from);
            redrawSquare(to);
            refreshTurnIndicators();
            onSendMove.accept(new PlayerMove(gameId,from,to));
        });
    }
    // ── Highlights ────────────────────────────────────────────────────

    private void highlightSelected(int row, int col) {
        boolean isLight = (row + col) % 2 != 0;
        squares[row][col].setStyle(isLight ? SELECT_STYLE_LIGHT : SELECT_STYLE_DARK);
    }

    private void highlightLegalMoves() {
        for (Square target : legalTargets) {
            int[] rc = squareToRowCol(target);
            Piece piece = board.getPiece(target);

            if (piece != Piece.NONE) {
                Circle ring = new Circle(squareSize * 0.48);
                ring.setFill(Color.TRANSPARENT);
                ring.setStroke(Color.color(0, 0, 0, 0.22));
                ring.setStrokeWidth(squareSize * 0.09);
                ring.setMouseTransparent(true);
                ring.setUserData("dot");
                squares[rc[0]][rc[1]].getChildren().add(ring);
            } else {
                double radius = Math.max(6, squareSize * 0.14);
                Circle dot = new Circle(radius);
                dot.setFill(Color.color(0, 0, 0, 0.20));
                dot.setMouseTransparent(true);
                dot.setUserData("dot");
                squares[rc[0]][rc[1]].getChildren().add(dot);
            }
        }
    }

    // ── Coordinate helpers ────────────────────────────────────────────

    private int[] squareToRowCol(Square square) {
        int file = square.getFile().ordinal();
        int rank = square.getRank().ordinal();
        if (mySide == Side.WHITE) return new int[]{7 - rank, file};
        return new int[]{rank, 7 - file};
    }

    private Square rowColToSquare(int row, int col) {
        int rank, file;
        if (mySide == Side.WHITE) { rank = 7 - row; file = col; }
        else                       { rank = row;     file = 7 - col; }
        String name = "" + (char)('a' + file) + (rank + 1);
        return Square.fromValue(name.toUpperCase());
    }

    // ── Widget builders ───────────────────────────────────────────────

    private void resizeAvatar(StackPane avatar, int size) {
        avatar.setPrefSize(size, size);
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);
        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        avatar.setClip(clip);
        avatar.getChildren().forEach(n -> {
            if (n instanceof ImageView iv) {
                iv.setFitWidth(size);
                iv.setFitHeight(size);
            } else if (n instanceof Label lbl) {
                lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: "
                        + (size / 3) + "px; -fx-font-weight: 700;");
            }
        });
    }

    private void resizeActionBtn(StackPane btn, double size) {
        btn.setPrefSize(size, size);
        btn.setMinSize(size, size);
        btn.setMaxSize(size, size);
    }

    /**
     * Rebuilds the two clock CSS variants (active/idle) for the current size,
     * caches them, sets the fixed layout properties once, and applies the
     * correct variant to each label immediately. Called only from applySize()
     * (on resize) and lazily on first use — never per move.
     */
    private void recomputeClockStyles(double fontSize, double width) {
        myClockLabel.setPrefWidth(width);
        oppClockLabel.setPrefWidth(width);
        myClockLabel.setAlignment(Pos.CENTER);
        oppClockLabel.setAlignment(Pos.CENTER);

        clockStyleActive = buildClockCss(fontSize, true);
        clockStyleIdle   = buildClockCss(fontSize, false);

        myClockLabel.setStyle(myTurn ? clockStyleActive : clockStyleIdle);
        oppClockLabel.setStyle(!myTurn ? clockStyleActive : clockStyleIdle);
    }

    private String buildClockCss(double fontSize, boolean active) {
        String fg     = active ? TEXT_PRIMARY : "#d8d4c8";
        String bg     = active ? CLOCK_BG_ACTIVE : CLOCK_BG_IDLE;
        String border = active ? ACCENT_DIM : CLOCK_BORDER_IDLE;
        String glow   = active ? CLOCK_GLOW_ACTIVE : CLOCK_GLOW_IDLE;
        int fs = (int) fontSize;
        return "-fx-text-fill: " + fg + ";" +
                "-fx-font-size: " + fs + "px;" +
                "-fx-font-weight: 900;" +
                "-fx-font-family: 'Consolas', 'Menlo', monospace;" +
                "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 7 0;" +
                glow;
    }

    private String playerNameStyle(double fontSize) {
        return "-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: " + (int) fontSize + "px; -fx-font-weight: 700;";
    }

    private String eloStyle(double fontSize) {
        return "-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: " + (int) fontSize + "px; -fx-font-weight: 600;";
    }

    private VBox buildPlayerInfoBox(StackPane avatar, Label nameLbl, Label eloLbl) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox nameBlock = new VBox(2);
        nameBlock.setAlignment(Pos.CENTER_LEFT);
        nameBlock.getChildren().addAll(nameLbl, eloLbl);
        row.getChildren().addAll(avatar, nameBlock);

        VBox box = new VBox(0);
        box.setPadding(new Insets(8, 10, 8, 10));
        box.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + CARD_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;"
        );
        box.getChildren().add(row);
        return box;
    }

    private VBox wrapClock(Label clockLabel) {
        VBox box = new VBox(0);
        box.setAlignment(Pos.CENTER_LEFT);
        clockLabel.setAlignment(Pos.CENTER);
        box.getChildren().add(clockLabel);
        return box;
    }

    private StackPane buildActionBtn(String symbol, String color, String tooltip, Runnable action) {
        StackPane btn = new StackPane();
        btn.setStyle(actionBtnStyle(false));
        Label lbl = new Label(symbol);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 700;");
        btn.getChildren().add(lbl);
        Tooltip.install(btn, new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> {
            btn.setStyle(actionBtnStyle(true));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setToX(1.08);
            st.setToY(1.08);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(actionBtnStyle(false));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        btn.setOnMouseClicked(e -> action.run());
        return btn;
    }

    private String actionBtnStyle(boolean hover) {
        return hover ? BTN_STYLE_HOVER : BTN_STYLE_IDLE;
    }


    private Label styledLabel(String text, String color, double fontSize, int weight) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-size: %.0fpx; -fx-font-weight: %d;",
                color, fontSize, weight));
        return lbl;

    }

}

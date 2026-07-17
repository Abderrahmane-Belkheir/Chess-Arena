package org.Core.UI.Game;


import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.Core.Auth.DTO.UserSession;
import org.Core.Game.Events.*;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import com.github.bhlangonijr.chesslib.*;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.Core.Game.Services.GameActions;
import org.Core.UI.LobbyScreens.Friends.Avatar;
import static org.Core.UI.LobbyScreens.Friends.Avatar.initials;
import javafx.scene.control.*;
import org.Core.Game.Events.*;
import javafx.scene.layout.*;
import java.util.*;
import com.github.bhlangonijr.chesslib.*;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import java.util.*;

/**
 * GameView — full game screen.
 *
 * Two ways to construct it:
 *   - Player mode:     GameView(gameId, fen, session, playerColor, opponent, matchmakingHandler, onReturnToLobby)
 *                       Draw/Resign buttons present, board is interactive on your turn.
 *   - Spectator mode:   GameView(gameId, fen, spectatedPlayerSide, spectatedPlayer, otherPlayer,
 *                                 spectatedTimeMs, otherTimeMs, onReturnToLobby)
 *                       No Draw/Resign buttons at all (not just hidden — never built), board is
 *                       never interactive, moves arrive via applyMoveConfirmation()'s from/to.
 *                       Board orientation follows the spectated player's side.
 */
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

    private static final String LIGHT_SQUARE      = "#f2e3c8";
    private static final String DARK_SQUARE       = "#8b5e34";
    private static final String SELECT_COLOR      = "#f0d878";
    private static final String SELECT_DARK       = "#c9a548";
    private static final String LAST_MOVE_LIGHT   = "#e8cf9a";
    private static final String LAST_MOVE_DARK    = "#b9873f";


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
    private static final String WOOD_MID          = "#7a4d29";
    private static final String WOOD_DARK         = "#3e2415";
    private static final String WOOD_HIGHLIGHT    = "#b98550";
    private static final String BRASS_LIGHT       = "#e8cf8a";
    private static final String BRASS_DARK        = "#8a6a30";

    // Spectate badge colors — muted, distinct from the LIVE accent gold so the
    // top-bar reads at a glance as "watching" rather than "playing".
    private static final String SPECTATE_COLOR       = TEXT_SECONDARY;
    private static final String SPECTATE_GLOW        = "rgba(160,142,120,0.45)";
    private static final String SPECTATE_DIVIDER_FROM = "rgba(160,142,120,0.30)";

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
    private static final double BOARD_SCALE  = 0.86;

    private static final double MOVE_ANIM_MS = 160;
    private static final double MAX_CLOCK_GAP = 240;

    private static final int DEFAULT_TIME_SECONDS = 600;

    private record PlayerInfo(String username, int elo, String avatarUrl) {
        static PlayerInfo from(UserSession session) {
            return new PlayerInfo(session.getUsername(), session.getElo(), session.getAvatarUrl());
        }
        static PlayerInfo from(GameFound.Player opponent) {
            return new PlayerInfo(opponent.getUsername(), opponent.getElo(), opponent.getAvatarUrl());
        }
    }

    private final String             gameId;
    private final Board              board          = new Board();
    private final UserSession        session;
    private Square                   selectedSquare = null;
    private List<Square>             legalTargets   = new ArrayList<>();
    private String                   lastMoveFrom   = null;
    private String                   lastMoveTo     = null;
    private volatile boolean         myTurn;
    private String                   fen;
    private final Side               mySide;

    private double squareSize = 96;

    private final Timeline clockTimeline = new Timeline();
    private int myTimeSeconds;
    private int oppTimeSeconds;

    private String clockStyleActive;
    private String clockStyleIdle;

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
    private final Label          liveLbl      = new Label();
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
    private final Label          myClockLabel  = new Label();
    private final Label          oppClockLabel = new Label();
    private final HBox           oppCaptured   = new HBox(2);
    private final HBox           myCaptured    = new HBox(2);
    private VBox                 oppInfoCard;
    private VBox                 myInfoCard;

    private StackPane      drawBtn;
    private StackPane      resignBtn;

    private double currentBtnSize = 40;

    private boolean drawCooldownActive;
    private boolean boardLocked = false;
    private final MatchmakingHandler matchmakingHandler; // null in spectator mode
    private final Runnable onReturnToLobby;

    private final boolean spectatorMode;
    private GameFound.Player spectatedPlayer;
    /** Player mode — full interactive game with Draw/Resign. */
    public GameView(String gameId, String fen, UserSession session, Side playerColor,
                    GameFound.Player opponent, MatchmakingHandler matchmakingHandler, Runnable onReturnToLobby) {
        this(gameId, fen, false, playerColor,
                PlayerInfo.from(session), PlayerInfo.from(opponent),
                DEFAULT_TIME_SECONDS, DEFAULT_TIME_SECONDS,
                session, matchmakingHandler, onReturnToLobby, null);
    }

    /**
     * Spectator mode — read-only. No Draw/Resign buttons are ever built. Board
     * orientation follows spectatedPlayerSide, so you see the board the way
     * that player sees it. Clock values are supplied directly since there's
     * no local play driving them until moves start arriving.
     *
     * @param currentTurn whose turn it actually is right now in the live game.
     *                    Unlike player mode — where a brand-new game always
     *                    opens on White, so "is it my turn" can be inferred
     *                    from playerColor == WHITE — a spectator can join a
     *                    game already in progress, so the real side-to-move
     *                    must be passed in explicitly and compared against
     *                    spectatedPlayerSide to decide which clock/avatar
     *                    renders as active.
     */
    public GameView(String fen, Side spectatedPlayerSide,
                    GameFound.Player spectatedPlayer, GameFound.Player otherPlayer,
                    long spectatedTimeMs, long otherTimeMs, Side currentTurn, Runnable onReturnToLobby) {
        this(null, fen, true, spectatedPlayerSide,
                PlayerInfo.from(spectatedPlayer), PlayerInfo.from(otherPlayer),
                (int) (spectatedTimeMs / 1000), (int) (otherTimeMs / 1000),
                null, null, onReturnToLobby, currentTurn);
        this.spectatedPlayer=spectatedPlayer;
    }

    /** Shared setup for both modes. bottomPlayer = "my" row, topPlayer = "opponent" row. */
    private GameView(String gameId, String fen, boolean spectatorMode, Side mySide,
                     PlayerInfo bottomPlayer, PlayerInfo topPlayer,
                     int myTimeSeconds, int oppTimeSeconds,
                     UserSession session, MatchmakingHandler matchmakingHandler, Runnable onReturnToLobby,
                     Side sideToMove) {
        this.gameId = gameId;
        this.fen = fen;
        this.spectatorMode = spectatorMode;
        this.mySide = mySide;
        this.session = session;
        this.matchmakingHandler = matchmakingHandler;
        this.onReturnToLobby = onReturnToLobby;
        this.myTimeSeconds = myTimeSeconds;
        this.oppTimeSeconds = oppTimeSeconds;
        // Player mode (sideToMove == null): a fresh game always starts on White's
        // turn, so mySide == WHITE tells us whether it's "my" turn — unchanged
        // from before. Spectator mode: the game may already be in progress, so
        // we compare the actual side-to-move (sideToMove) against the side we're
        // spectating (mySide, i.e. spectatedPlayerSide) instead of assuming White opens.
        this.myTurn = (sideToMove != null) ? (sideToMove == mySide) : (mySide == Side.WHITE);

        myAvatarPane  = Avatar.build(bottomPlayer.avatarUrl(), initials(bottomPlayer.username()),
                Avatar.colorFromName(bottomPlayer.username()), 44);
        oppAvatarPane = Avatar.build(topPlayer.avatarUrl(), initials(topPlayer.username()),
                Avatar.colorFromName(topPlayer.username()), 44);

        myNameLbl  = styledLabel(bottomPlayer.username(), TEXT_PRIMARY, 14, 700);
        oppNameLbl = styledLabel(topPlayer.username(), TEXT_PRIMARY, 14, 700);
        myEloLbl   = styledLabel(bottomPlayer.elo() + " ELO", TEXT_SECONDARY, 12, 600);
        oppEloLbl  = styledLabel(topPlayer.elo() + " ELO", TEXT_SECONDARY, 12, 600);

        myClockLabel.setText(formatTime(myTimeSeconds));
        oppClockLabel.setText(formatTime(oppTimeSeconds));

        if (!spectatorMode) {
            drawBtn   = buildActionBtn("½", DRAW_COLOR,   "Offer draw", this::offerDraw);
            resignBtn = buildActionBtn("⚑", RESIGN_COLOR, "Resign",     this::confirmResign);
            if (mySide == Side.WHITE) {
                drawBtn.setDisable(true);
            }
        }

        buildLayout();
        attachResizeListeners();
        renderFromFen(fen);
        refreshTurnIndicators();
        startClockTicking();
    }


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
                this.fen = board.getFen();
                refreshTurnIndicators();
                myTurn = true;
                if (!spectatorMode) {
                    drawBtn.setDisable(true);
                    drawBtn.setOpacity(0.4);
                }
                if (gameMove.getGameOverInfo() != null) {
                    showGameOverCard(gameMove.getGameOverInfo());
                }
            });
        });
    }

    /**
     * Player mode: applies clock sync / fen reconciliation / game-over after
     * the server confirms a move you already animated locally in commitMove().
     *
     * Spectator mode: nothing was animated locally, so this is what actually
     * moves the piece — uses confirmation.getFrom()/getTo() to animate + apply
     * the move, then updates both clocks the same way. Requires MoveConfirmation
     * to expose from/to (add them server-side if it doesn't yet).
     */
    public void applyMoveConfirmation(MoveConfirmation confirmation) {
        Platform.runLater(() -> {
            if (spectatorMode) {
                Square from = Square.fromValue(confirmation.getFrom().toUpperCase());
                Square to   = Square.fromValue(confirmation.getTo().toUpperCase());

                animateMove(from, to, () -> {
                    Move move = new Move(from, to);
                    board.doMove(move);

                    lastMoveFrom = from.value().toLowerCase();
                    lastMoveTo   = to.value().toLowerCase();
                    redrawSquare(lastMoveFrom);
                    redrawSquare(lastMoveTo);
                    this.fen = board.getFen();

                    myTurn = board.getSideToMove() == mySide;
                    refreshTurnIndicators();
                });
            } else {
                drawBtn.setDisable(false);
                drawBtn.setOpacity(1.0);
                if (confirmation.getFen() != null && !confirmation.getFen().equals(fen)) {
                    renderFromFen(confirmation.getFen());
                }
            }
            syncClocks(confirmation.getMyRemainingMs(), confirmation.getOppRemainingMs());
            if (confirmation.getGameOverInfo() != null) {
                showGameOverCard(confirmation.getGameOverInfo());
            }
        });
    }

    private void syncClocks(long myRemainingMs, long oppRemainingMs) {
        myTimeSeconds  = (int) (myRemainingMs / 1000);
        oppTimeSeconds = (int) (oppRemainingMs / 1000);
        myClockLabel.setText(formatTime(myTimeSeconds));
        oppClockLabel.setText(formatTime(oppTimeSeconds));
    }

    public void showSpectateRequest(SpectatedResponse response){
        if(spectatorMode) return;
        Platform.runLater(() ->
                new SpectateRequestCard(root,response,GameActions.onAcceptSpectate, null));
    }

    public void showDrawOffered() {
        if (spectatorMode) return;
        Platform.runLater(() -> new DrawOfferReceivedCard(
                root, () -> {
            GameActions.onAcceptDraw.accept(gameId);
            boardLocked = true;
            disableButtons();
        }, null));
    }

    public void gameOver(GameOverInfo gameOverInfo) {
        showGameOverCard(gameOverInfo);
    }

    private void showGameOverCard(GameOverInfo info) {
        Platform.runLater(() -> {
            if (!spectatorMode) disableButtons();
            stopClocks();
            if (session != null) session.setElo(info.getNewElo());
            Runnable onRematch = spectatorMode
                    ? onReturnToLobby
                    : () -> matchmakingHandler.startGameSearching(root);
            GameOverCard card = new GameOverCard(info, session, boardWrap, onRematch, onReturnToLobby);
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

    // ── Turn indicators ──────────────────────────────────────────────

    /** Updates clock + avatar styling to reflect whose turn it is. Call whenever myTurn changes. */
    private void refreshTurnIndicators() {
        if (clockStyleActive == null) {
            recomputeClockStyles(currentClockFont(), currentClockWidth());
        } else {
            myClockLabel.setStyle(myTurn ? clockStyleActive : clockStyleIdle);
            oppClockLabel.setStyle(!myTurn ? clockStyleActive : clockStyleIdle);
        }
        styleAvatarRing(myAvatarPane,  myTurn);
        styleAvatarRing(oppAvatarPane, !myTurn);
        if (!spectatorMode) updateDrawButtonState(); // no draw button exists in spectator mode
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

        // Right sidebar — width reserved for symmetry either way; buttons only
        // exist (and only get added) in player mode.
        rightSidebar.setAlignment(Pos.CENTER);
        rightSidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + PANEL_TOP + ", " + PANEL_BOTTOM + ")," +
                        "radial-gradient(center 85% 0%, radius 70%, rgba(232,207,138,0.045), rgba(232,207,138,0) 60%);" +
                        "-fx-background-radius: 14 0 0 14;" +
                        "-fx-border-color: linear-gradient(to bottom, " + WOOD_HIGHLIGHT + ", " + WOOD_DARK + ");" +
                        "-fx-border-width: 0 0 0 2;" +
                        "-fx-border-radius: 14 0 0 14;"
        );
        if (!spectatorMode) {
            rightSidebar.getChildren().addAll(drawBtn, resignBtn);
        }

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

        // Top-bar status dot + label: gold "LIVE" pulse in player mode,
        // muted "SPECTATING" badge in spectator mode — same widgets, styled
        // per mode so the two views are visually distinguishable at a glance.
        liveDot.setFill(Color.web(spectatorMode ? SPECTATE_COLOR : ACCENT));
        liveDot.setEffect(new DropShadow(8, Color.web(spectatorMode ? SPECTATE_GLOW : ACCENT_GLOW)));
        FadeTransition pulse = new FadeTransition(Duration.millis(900), liveDot);
        pulse.setFromValue(1.0);
        pulse.setToValue(0.35);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        liveLbl.setText(spectatorMode ? "SPECTATING" : "LIVE");
        liveLbl.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-size: %.0fpx; -fx-font-weight: %d;",
                spectatorMode ? SPECTATE_COLOR : ACCENT, 11.0, 800));

        Region topDivider = new Region();
        topDivider.setPrefHeight(1);
        topDivider.setMaxHeight(1);
        topDivider.setStyle("-fx-background-color: linear-gradient(to right, "
                + (spectatorMode ? SPECTATE_DIVIDER_FROM : "rgba(129,182,76,0.35)") + ", rgba(129,182,76,0));");
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
        if (!spectatorMode) {
            resizeActionBtn(drawBtn,   btnSize);
            resizeActionBtn(resignBtn, btnSize);
        }

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

    private void redrawSquare(String squareName) {
        Square sq = Square.fromValue(squareName.toUpperCase());
        if (sq == Square.NONE) return;
        int[] rc = squareToRowCol(sq);
        resetSquareColor(rc[0], rc[1]);
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

    // ── Color helpers ───────────────────────────────────────────────

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

    private void clearHighlights() {
        if (selectedSquare != null) {
            int[] rc = squareToRowCol(selectedSquare);
            resetSquareColor(rc[0], rc[1]);
        }

        for (Square target : legalTargets) {
            int[] rc = squareToRowCol(target);
            resetSquareColor(rc[0], rc[1]);
            squares[rc[0]][rc[1]].getChildren().removeIf(n -> "dot".equals(n.getUserData()));
        }

        resetSquareColorByName(lastMoveFrom);
        resetSquareColorByName(lastMoveTo);
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
        resetSquareColor(fromRC[0], fromRC[1]);

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
        if (spectatorMode) return; // fixed typo (was "specatorMode") — spectators can never move pieces
        if (boardLocked) return;
        Square clicked = rowColToSquare(row, col);

        if (selectedSquare == null) {
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
            if (legalTargets.contains(clicked)) {
                commitMove(selectedSquare, clicked);
            } else {
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

    private void commitMove(Square fromSq, Square toSq) {
        clearHighlights();
        selectedSquare = null;
        legalTargets.clear();
        myTurn = false;
        animateMove(fromSq, toSq, () -> {
            Move move = new Move(fromSq, toSq);
            board.doMove(move);
            String from = fromSq.value().toLowerCase();
            String to   = toSq.value().toLowerCase();
            lastMoveFrom = from;
            lastMoveTo   = to;
            redrawSquare(from);
            redrawSquare(to);
            this.fen = board.getFen();
            refreshTurnIndicators();
            GameActions.onMove.accept(new PlayerMove(gameId, from, to));
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

    private void disableButtons() {
        if (spectatorMode) return; // buttons don't exist
        drawBtn.setDisable(true);
        resignBtn.setDisable(true);
        drawBtn.setOpacity(0.4);
        resignBtn.setOpacity(0.4); // fixed: was setting drawBtn's opacity twice, resignBtn never dimmed
    }

    private void updateDrawButtonState() {
        boolean disabled = myTurn || drawCooldownActive;
        drawBtn.setDisable(disabled);
        drawBtn.setOpacity(disabled ? 0.4 : 1.0);
    }

    private void startDrawCooldown() {
        drawCooldownActive = true;
        updateDrawButtonState();
        PauseTransition cooldown = new PauseTransition(Duration.seconds(60));
        cooldown.setOnFinished(e -> {
            drawCooldownActive = false;
            updateDrawButtonState();
        });
        cooldown.play();
    }

    private void offerDraw() {
        new DrawOfferConfirmCard(root, () -> {
            GameActions.onOfferDraw.accept(gameId);
            startDrawCooldown();
        });
    }

    private void confirmResign() {
        new ResignConfirmCard(root, () -> {
            stopClocks();
            GameActions.onResign.accept(gameId);
            boardLocked = true;
            disableButtons();
        }, () -> {
            if (myTurn && boardLocked) boardLocked = false;
        });
    }

    private StackPane buildActionBtn(String symbol, String color, String tooltip, Runnable action) {
        StackPane btn = new StackPane();
        btn.setStyle(actionBtnStyle(false));
        Label lbl = new Label(symbol);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 700;");
        btn.getChildren().add(lbl);
        Tooltip.install(btn, new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> {
            if (btn.isDisabled()) return;
            btn.setStyle(actionBtnStyle(true));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setToX(1.08);
            st.setToY(1.08);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            if (btn.isDisabled()) return; // fixed: previously always restyled, ignoring disabled state
            btn.setStyle(actionBtnStyle(false));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        btn.setOnMouseClicked(e -> {
            if (btn.isDisabled()) return; // fixed: previously always ran, disabled or not
            action.run();
        });
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

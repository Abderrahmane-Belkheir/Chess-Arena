package org.Core.UI.Game;


import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.Core.Auth.DTO.UserSession;
import org.Core.Game.Events.GameFound;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.Core.Game.Events.OpponentMove;
import org.Core.Game.Events.PlayerMove;
import org.Core.Game.Services.GameSessionService;

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


public class GameView {

    // ── Board palette (chess.com-inspired) ────────────────────────────
    private static final String LIGHT_SQUARE      = "#eeeed2";
    private static final String DARK_SQUARE       = "#769656";
    private static final String SELECT_COLOR      = "#f6f669";
    private static final String SELECT_DARK       = "#baca2b";
    private static final String LAST_MOVE_LIGHT   = "#cdd16e";
    private static final String LAST_MOVE_DARK    = "#aaa23a";

    private static final double LEFT_RATIO   = 0.26;
    private static final double RIGHT_RATIO  = 0.09;
    private static final int    MIN_SQUARE   = 48;
    private static final int    MAX_SQUARE   = 120;

    private static final double MOVE_ANIM_MS = 160;

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

    // ── UI nodes ──────────────────────────────────────────────────────
    private final StackPane      root         = new StackPane();
    private final GridPane       boardGrid    = new GridPane();
    private final StackPane[][]  squares      = new StackPane[8][8];
    private final StackPane      boardWrap    = new StackPane();
    private final Pane           floatPane    = new Pane();

    // Sidebars
    private final VBox           leftSidebar  = new VBox(0);
    private final VBox           rightSidebar = new VBox(12);

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

    // Action buttons
    private final StackPane      drawBtn;
    private final StackPane      resignBtn;

    private final GameSessionService gameSessionService;

    // ── Constructor ───────────────────────────────────────────────────
    public GameView(String gameId,String fen, UserSession session, Side playerColor,
                    GameFound.Opponent opponent, GameSessionService gameSessionService) {
        this.gameId=gameId;
        this.fen      = fen;
        this.session  = session;
        this.opponent = opponent;
        this.mySide   = playerColor;
        this.myTurn   = mySide == Side.WHITE;
        this.gameSessionService = gameSessionService;

        oppAvatarPane = buildAvatar(opponent.getAvatarUrl(),
                initials(opponent.getUsername()), avatarColor(opponent.getUsername()), 44);
        myAvatarPane  = buildAvatar(session.getAvatarUrl(),
                initials(session.getUsername()),  avatarColor(session.getUsername()),  44);

        oppNameLbl = styledLabel(opponent.getUsername(), "#e8e4dc", 14, 700);
        myNameLbl  = styledLabel(session.getUsername(),  "#e8e4dc", 14, 700);
        oppEloLbl  = styledLabel(opponent.getElo() + " ELO", "#7a7672", 12, 600);
        myEloLbl   = styledLabel(session.getElo()  + " ELO", "#7a7672", 12, 600);

        drawBtn   = buildActionBtn("½", "#a0a090", () -> System.out.println("Draw offered"));
        resignBtn = buildActionBtn("⚑", "#e05555", () -> System.out.println("Resigned"));

        buildLayout();
        attachResizeListeners();
        renderFromFen(fen);
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
                myTurn = true;
            });
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

    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%d:%02d", m, s);
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

    // ── Layout ────────────────────────────────────────────────────────

    private void buildLayout() {
        root.setStyle("-fx-background-color: #262421;");
        root.setMinSize(0, 0);

        boardGrid.setGridLinesVisible(false);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane sq = new StackPane();
                boolean isLight = (row + col) % 2 != 0;
                sq.setStyle("-fx-background-color: " + (isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");
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
        boardWrap.setStyle("-fx-background-color: transparent;");

        floatPane.prefWidthProperty().bind(boardWrap.widthProperty());
        floatPane.prefHeightProperty().bind(boardWrap.heightProperty());

        // Left sidebar
        leftSidebar.setAlignment(Pos.TOP_LEFT);
        leftSidebar.setStyle("-fx-background-color: #1e1c1a;");

        VBox oppInfo     = buildPlayerInfoBox(oppAvatarPane, oppNameLbl, oppEloLbl);
        oppCaptured.setPadding(new Insets(2, 0, 4, 0));
        oppCaptured.setAlignment(Pos.CENTER_LEFT);
        VBox oppClockBox = wrapClock(oppClockLabel);

        VBox topGroup = new VBox(6);
        topGroup.getChildren().addAll(oppInfo, oppCaptured, oppClockBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox myClockBox  = wrapClock(myClockLabel);
        myCaptured.setPadding(new Insets(4, 0, 2, 0));
        myCaptured.setAlignment(Pos.CENTER_LEFT);
        VBox myInfo      = buildPlayerInfoBox(myAvatarPane, myNameLbl, myEloLbl);

        VBox bottomGroup = new VBox(6);
        bottomGroup.getChildren().addAll(myClockBox, myCaptured, myInfo);

        leftSidebar.getChildren().addAll(topGroup, spacer, bottomGroup);

        // Right sidebar
        rightSidebar.setAlignment(Pos.CENTER);
        rightSidebar.setStyle("-fx-background-color: #1e1c1a;");
        rightSidebar.getChildren().addAll(drawBtn, resignBtn);

        HBox centerRow = new HBox(0);
        centerRow.setAlignment(Pos.CENTER);
        centerRow.setMinSize(0, 0);
        centerRow.getChildren().addAll(leftSidebar, boardWrap, rightSidebar);

        root.getChildren().add(centerRow);
        StackPane.setAlignment(centerRow, Pos.CENTER);
    }

    private void addCoordinateLabels() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isLight = (row + col) % 2 != 0;
                String textColor = isLight ? DARK_SQUARE : LIGHT_SQUARE;

                if (col == 0) {
                    int rankNum = (mySide == Side.WHITE) ? (8 - row) : (row + 1);
                    Label rankLbl = new Label(String.valueOf(rankNum));
                    rankLbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 10px; -fx-font-weight: 700;");
                    rankLbl.setMouseTransparent(true);
                    rankLbl.setUserData("coord");
                    StackPane.setAlignment(rankLbl, Pos.TOP_LEFT);
                    StackPane.setMargin(rankLbl, new Insets(2, 0, 0, 2));
                    squares[row][col].getChildren().add(rankLbl);
                }

                if (row == 7) {
                    char fileLetter = (mySide == Side.WHITE) ? (char)('a' + col) : (char)('h' - col);
                    Label fileLbl = new Label(String.valueOf(fileLetter));
                    fileLbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 10px; -fx-font-weight: 700;");
                    fileLbl.setMouseTransparent(true);
                    fileLbl.setUserData("coord");
                    StackPane.setAlignment(fileLbl, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(fileLbl, new Insets(0, 2, 2, 0));
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

        double sq = Math.floor(boardPx / 8.0);
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
        double btnSize      = Math.max(32, Math.round(sq * 0.46));

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
        styleClockLabel(oppClockLabel, clockFont, clockWidth);
        styleClockLabel(myClockLabel,  clockFont, clockWidth);

        rightSidebar.setPrefSize(rightWidth, boardPx);
        rightSidebar.setMinSize(rightWidth, boardPx);
        rightSidebar.setMaxSize(rightWidth, boardPx);

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
            Image img = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/Pieces/" + piece.name() + ".png")),
                    PIECE_LOAD_SIZE, PIECE_LOAD_SIZE, true, true);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setMouseTransparent(true);

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.color(0, 0, 0, 0.45));
            shadow.setRadius(6);
            shadow.setOffsetY(2);
            iv.setEffect(shadow);

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
        squares[row][col].setStyle(
                "-fx-background-color: " + (isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");
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
        squares[rc[0]][rc[1]].setStyle(
                "-fx-background-color: " + (isLight ? LAST_MOVE_LIGHT : LAST_MOVE_DARK) + ";");
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
        iv.setEffect(flyingShadow);

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
        try {
            Image img = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/Pieces/" + piece.name() + ".png")),
                    size, size, true, true);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setMouseTransparent(true);
            return iv;
        } catch (Exception e) {
            return null;
        }
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

        animateMove(fromSq, toSq, () -> {
            Move move = new Move(fromSq, toSq);
            board.doMove(move);
            String from = fromSq.value().toLowerCase();
            String to   = toSq.value().toLowerCase();
            lastMoveFrom = from;
            lastMoveTo   = to;
            redrawSquare(from);
            redrawSquare(to);
            myTurn = false;

            gameSessionService.sendPlayerMove(new PlayerMove(gameId,from,to));

        });
    }
    // ── Highlights ────────────────────────────────────────────────────

    private void highlightSelected(int row, int col) {
        boolean isLight = (row + col) % 2 != 0;
        squares[row][col].setStyle(
                "-fx-background-color: " + (isLight ? SELECT_COLOR : SELECT_DARK) + ";");
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

    private void styleClockLabel(Label lbl, double fontSize, double width) {
        lbl.setPrefWidth(width);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle(String.format("""
            -fx-text-fill: #f0f0e8;
            -fx-font-size: %.0fpx;
            -fx-font-weight: 900;
            -fx-background-color: #141412;
            -fx-background-radius: 6;
            -fx-padding: 6 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 6, 0, 0, 2);
        """, fontSize));
    }

    private String playerNameStyle(double fontSize) {
        return String.format("""
            -fx-text-fill: #e8e4dc;
            -fx-font-size: %.0fpx;
            -fx-font-weight: 700;
        """, fontSize);
    }

    private String eloStyle(double fontSize) {
        return String.format("""
            -fx-text-fill: #7a7672;
            -fx-font-size: %.0fpx;
            -fx-font-weight: 600;
        """, fontSize);
    }

    private VBox buildPlayerInfoBox(StackPane avatar, Label nameLbl, Label eloLbl) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox nameBlock = new VBox(2);
        nameBlock.setAlignment(Pos.CENTER_LEFT);
        nameBlock.getChildren().addAll(nameLbl, eloLbl);
        row.getChildren().addAll(avatar, nameBlock);
        VBox box = new VBox(0);
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

    private StackPane buildActionBtn(String symbol, String color, Runnable action) {
        StackPane btn = new StackPane();
        btn.setStyle("""
            -fx-background-color: #2a2826;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);
        Label lbl = new Label(symbol);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: 700;");
        btn.getChildren().add(lbl);

        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #3a3836;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """));
        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: #2a2826;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """));
        btn.setOnMouseClicked(e -> action.run());
        return btn;
    }

    private StackPane buildAvatar(String imageUrl, String initials,
                                  String bgColor, int size) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(size, size);
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);

        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        avatar.setClip(clip);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image img = new Image(imageUrl, size, size, true, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                avatar.getChildren().add(iv);
                return avatar;
            } catch (Exception ignored) {}
        }

        avatar.setStyle("-fx-background-color: " + bgColor + ";");
        Label lbl = new Label(initials);
        lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: "
                + (size / 3) + "px; -fx-font-weight: 700;");
        avatar.getChildren().add(lbl);
        return avatar;
    }

    private Label styledLabel(String text, String color, double fontSize, int weight) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-size: %.0fpx; -fx-font-weight: %d;",
                color, fontSize, weight));
        return lbl;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String initials(String username) {
        return Avatar.initials(username);
    }

    private String avatarColor(String username) {
        String[] palette = {
                "#7c5c3e", "#5c3e7c", "#3e7c5c",
                "#7c3e5c", "#3e5c7c", "#5c7c3e"
        };
        return palette[Math.abs(username.hashCode()) % palette.length];
    }
}
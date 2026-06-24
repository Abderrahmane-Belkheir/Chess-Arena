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
import javafx.scene.shape.Rectangle;
import org.Core.Auth.DTO.UserSession;
import org.Core.Game.Events.GameFound;
import java.util.List;
import java.util.stream.Collectors;
import com.github.bhlangonijr.chesslib.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import org.Core.UI.Shared.ViewNavigator;

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
public class GameView {

    // ── Color constants (never change) ────────────────────────────────
    private static final String LIGHT_SQUARE = "#f0d9b5";
    private static final String DARK_SQUARE  = "#779952";
    private static final String HIGHLIGHT    = "#f6f669";
    private static final String SELECT_COLOR = "#81b64c";

    // Sidebar proportions relative to board size
    private static final double LEFT_RATIO   = 0.26;  // left sidebar = 26% of board size
    private static final double RIGHT_RATIO  = 0.09;  // right sidebar = 9% of board size
    private static final int    MIN_SQUARE   = 48;
    private static final int    MAX_SQUARE   = 120;

    // ── State ─────────────────────────────────────────────────────────
    private final Board              board        = new Board();
    private final UserSession        session;
    private Square                   selectedSquare = null;
    private List<Square>             legalTargets   = new ArrayList<>();
    private String                   lastMoveFrom   = null;
    private String                   lastMoveTo     = null;
    private boolean                  myTurn;
    private String                   fen;
    private final GameFound.Opponent opponent;

    // Current computed square size (pixels) — updated on resize
    private double squareSize = 96;


    // ── UI nodes (kept as fields so applySize can reach them) ─────────
    private final StackPane      root         = new StackPane();
    private final GridPane       boardGrid    = new GridPane();
    private final StackPane[][]  squares      = new StackPane[8][8];
    private final StackPane      boardWrap    = new StackPane();

    // Sidebars
    private final VBox           leftSidebar  = new VBox(0);
    private final VBox           rightSidebar = new VBox(12);

    // Player / clock widgets — refs needed for resize
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

    // ── Constructor ───────────────────────────────────────────────────
    public GameView(String fen, UserSession session, GameFound.Opponent opponent) {
        this.fen      = fen;
        this.session  = session;
        this.opponent = opponent;
        this.myTurn   = true; // TODO: replace with real color assignment

        // Pre-build label/avatar refs so buildLayout can wire them in
        oppAvatarPane = buildAvatar(opponent.getAvatarUrl(),
                initials(opponent.getUsername()), avatarColor(opponent.getUsername()), 44);
        myAvatarPane  = buildAvatar(session.getAvatarUrl(),
                initials(session.getUsername()),  avatarColor(session.getUsername()),  44);

        oppNameLbl = styledLabel(opponent.getUsername(), "#e8e4dc", 14, 700);
        myNameLbl  = styledLabel(session.getUsername(),  "#e8e4dc", 14, 700);
        oppEloLbl  = styledLabel(opponent.getElo() + " ELO", "#7a7672", 12, 600);
        myEloLbl   = styledLabel(session.getElo()  + " ELO", "#7a7672", 12, 600);

        drawBtn   = buildActionBtn("½", "#888888", () -> System.out.println("Draw offered"));
        resignBtn = buildActionBtn("⚑", "#e05555", () -> System.out.println("Resigned"));

        buildLayout();
        attachResizeListeners();
        renderFromFen(fen);
    }

    // ── Public API ────────────────────────────────────────────────────

    public void renderFromFen(String fen) {
        board.loadFromFen(fen);
        drawAllPieces();
        clearHighlights();
        selectedSquare = null;
        legalTargets.clear();
    }

    public void applyServerMove(String from, String to, String newFen) {
        Platform.runLater(() -> {
            Move move = new Move(
                    Square.fromValue(from.toUpperCase()),
                    Square.fromValue(to.toUpperCase()));
            board.doMove(move);

            if (!board.getFen().equals(newFen)) {
                board.loadFromFen(newFen);
                drawAllPieces();
            } else {
                redrawSquare(from);
                redrawSquare(to);
            }

            lastMoveFrom = from;
            lastMoveTo   = to;
            applyLastMoveHighlight();
            myTurn = board.getSideToMove() == Side.WHITE; // TODO: use real color
        });
    }

    public void updateClocks(String myTime, String oppTime) {
        Platform.runLater(() -> {
            myClockLabel.setText(myTime);
            oppClockLabel.setText(oppTime);
        });
    }

    public StackPane getView() { return root; }

    // ── Layout (structure only — no hard sizes here) ──────────────────

    private void buildLayout() {
        root.setStyle("-fx-background-color: #262421;");
        root.setMinSize(0, 0);

        // Board squares grid
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
        boardWrap.getChildren().add(boardGrid);
        boardWrap.setStyle("-fx-background-color: transparent;");

        // Left sidebar structure
        leftSidebar.setAlignment(Pos.TOP_LEFT);
        leftSidebar.setStyle("-fx-background-color: #1e1c1a;");

        VBox oppInfo = buildPlayerInfoBox(oppAvatarPane, oppNameLbl, oppEloLbl);
        oppCaptured.setPadding(new Insets(2, 0, 4, 0));
        oppCaptured.setAlignment(Pos.CENTER_LEFT);
        VBox oppClockBox = wrapClock(oppClockLabel);

        VBox topGroup = new VBox(6);
        topGroup.getChildren().addAll(oppInfo, oppCaptured, oppClockBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox myClockBox = wrapClock(myClockLabel);
        myCaptured.setPadding(new Insets(4, 0, 2, 0));
        myCaptured.setAlignment(Pos.CENTER_LEFT);
        VBox myInfo = buildPlayerInfoBox(myAvatarPane, myNameLbl, myEloLbl);

        VBox bottomGroup = new VBox(6);
        bottomGroup.getChildren().addAll(myClockBox, myCaptured, myInfo);

        leftSidebar.getChildren().addAll(topGroup, spacer, bottomGroup);

        // Right sidebar structure
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

    // ── Resize logic ──────────────────────────────────────────────────

    /**
     * Hook into the Scene's dimensions so shrinking the window actually fires.
     * root.widthProperty() never shrinks because StackPane grows to fit content;
     * scene.widthProperty() / heightProperty() always reflect the real window size.
     */
    private void attachResizeListeners() {
        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> recomputeSize();

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener(sizeListener);
                newScene.heightProperty().addListener(sizeListener);
                recomputeSize(); // run once immediately with real scene dimensions
            }
        });

        // Also listen on root in case the scene is already attached at build time
        root.widthProperty().addListener(sizeListener);
        root.heightProperty().addListener(sizeListener);
    }

    private void recomputeSize() {
        // Prefer scene dimensions (shrink-aware); fall back to root if scene not ready
        double w, h;
        if (root.getScene() != null) {
            w = root.getScene().getWidth();
            h = root.getScene().getHeight();
        } else {
            w = root.getWidth();
            h = root.getHeight();
        }
        if (w <= 0 || h <= 0) return;

        // Board must fit in: width minus both sidebars, and full height
        // sidebars are a ratio of board size, so: boardPx + board*LEFT + board*RIGHT <= w
        // => boardPx <= w / (1 + LEFT_RATIO + RIGHT_RATIO)
        double maxFromWidth  = w / (1.0 + LEFT_RATIO + RIGHT_RATIO);
        double maxFromHeight = h;  // board can use full height
        double boardPx = Math.min(maxFromWidth, maxFromHeight);

        double sq = Math.floor(boardPx / 8.0);
        sq = Math.max(MIN_SQUARE, Math.min(MAX_SQUARE, sq));

        if (Math.abs(sq - squareSize) < 0.1) return; // no meaningful change
        squareSize = sq;

        applySize(squareSize);
    }

    /**
     * Apply a new square size to every sized element imperatively.
     * Layout/colors update every frame; piece images reload only after
     * the resize gesture settles (150 ms debounce) so image decoding
     * never blocks the drag.
     */
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

        // ── Board squares ─────────────────────────────────────────────
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

        // ── Left sidebar ──────────────────────────────────────────────
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

        // ── Right sidebar ─────────────────────────────────────────────
        rightSidebar.setPrefSize(rightWidth, boardPx);
        rightSidebar.setMinSize(rightWidth, boardPx);
        rightSidebar.setMaxSize(rightWidth, boardPx);

        resizeActionBtn(drawBtn,   btnSize);
        resizeActionBtn(resignBtn, btnSize);

        clearHighlights();
        // Pieces scale automatically via property bindings — no redraw needed here.
    }

    // ── Resize helpers ────────────────────────────────────────────────

    private void resizeAvatar(StackPane avatar, int size) {
        avatar.setPrefSize(size, size);
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);
        javafx.scene.shape.Circle clip =
                new javafx.scene.shape.Circle(size / 2.0, size / 2.0, size / 2.0);
        avatar.setClip(clip);
        // update image if present
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
            -fx-text-fill: #f0f0f0;
            -fx-font-size: %.0fpx;
            -fx-font-weight: 900;
            -fx-background-color: #141412;
            -fx-background-radius: 6;
            -fx-padding: 6 0;
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

    // ── Board rendering ───────────────────────────────────────────────

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
        clearSquareUI(rc[0], rc[1]);
        Piece piece = board.getPiece(sq);
        if (piece != Piece.NONE) placePiece(piece, rc[0], rc[1]);
    }

    private void placePiece(Piece piece, int row, int col) {
        String imageName = piece.name();
        // Load once at a fixed small size; ImageView scales up/down with the square.
        final int PIECE_LOAD_SIZE = 56;
        try {
            Image img = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/Pieces/" + imageName + ".png")),
                    PIECE_LOAD_SIZE, PIECE_LOAD_SIZE, true, true);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setMouseTransparent(true);
            // Bind display size to the square's actual size minus a small margin
            StackPane sq = squares[row][col];
            iv.fitWidthProperty().bind(sq.widthProperty().subtract(6));
            iv.fitHeightProperty().bind(sq.heightProperty().subtract(6));
            sq.getChildren().add(iv);
        } catch (Exception e) {
            Label fallback = new Label(piece.getFenSymbol());
            fallback.setStyle("-fx-text-fill: white; -fx-font-size: 28px;");
            fallback.setMouseTransparent(true);
            squares[row][col].getChildren().add(fallback);
        }
    }

    private void clearSquareUI(int row, int col) {
        squares[row][col].getChildren().removeIf(
                n -> !(n instanceof Rectangle) || ((Rectangle) n).getUserData() == null);
        squares[row][col].getChildren().clear();
    }

    // ── Click handling ────────────────────────────────────────────────

    private void handleSquareClick(int row, int col) {
        Square clicked = rowColToSquare(row, col);

        if (selectedSquare == null) {
            Piece piece = board.getPiece(clicked);
            if (piece == Piece.NONE) return;
            if (!myTurn) return; // TODO: replace with real color check

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
                if (piece != Piece.NONE && myTurn) {
                    handleSquareClick(row, col);
                }
            }
        }
    }

    private void commitMove(Square from, Square to) {
        Move move = new Move(from, to);

        board.doMove(move);
        String fromName = from.value().toLowerCase();
        String toName   = to.value().toLowerCase();
        redrawSquare(fromName);
        redrawSquare(toName);

        lastMoveFrom = fromName;
        lastMoveTo   = toName;
        applyLastMoveHighlight();

        selectedSquare = null;
        legalTargets.clear();
        myTurn = false;

        // TODO: send move to server
        // websocket.sendMove(fromName, toName);
        System.out.println("MOVE: " + fromName + " → " + toName);
    }

    // ── Highlights ────────────────────────────────────────────────────

    private void highlightSelected(int row, int col) {
        squares[row][col].setStyle("-fx-background-color: " + SELECT_COLOR + ";");
    }

    private void highlightLegalMoves() {
        for (Square target : legalTargets) {
            int[] rc = squareToRowCol(target);
            Piece piece = board.getPiece(target);

            if (piece != Piece.NONE) {
                squares[rc[0]][rc[1]].setStyle("-fx-background-color: " + SELECT_COLOR + ";");
            } else {
                double radius = Math.max(6, squareSize * 0.12);
                Circle dot = new Circle(radius);
                dot.setFill(Color.web("#000000", 0.18));
                dot.setMouseTransparent(true);
                dot.setUserData("dot");
                squares[rc[0]][rc[1]].getChildren().add(dot);
            }
        }
    }

    private void applyLastMoveHighlight() {
        if (lastMoveFrom == null || lastMoveTo == null) return;
        Square from = Square.fromValue(lastMoveFrom.toUpperCase());
        Square to   = Square.fromValue(lastMoveTo.toUpperCase());
        if (from == Square.NONE || to == Square.NONE) return;

        int[] rcFrom = squareToRowCol(from);
        int[] rcTo   = squareToRowCol(to);

        squares[rcFrom[0]][rcFrom[1]].setStyle("-fx-background-color: " + HIGHLIGHT + ";");
        squares[rcTo[0]][rcTo[1]].setStyle("-fx-background-color: " + HIGHLIGHT + ";");
    }

    private void clearHighlights() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean isLight = (r + c) % 2 != 0;
                squares[r][c].setStyle("-fx-background-color: " +
                        (isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");
                squares[r][c].getChildren().removeIf(n -> "dot".equals(n.getUserData()));
            }
        }
        if (lastMoveFrom != null) applyLastMoveHighlight();
    }

    // ── Coordinate helpers ────────────────────────────────────────────

    private int[] squareToRowCol(Square square) {
        int file = square.getFile().ordinal();
        int rank = square.getRank().ordinal();
        int row  = 7 - rank;
        int col  = file;
        return new int[]{row, col};
    }

    private Square rowColToSquare(int row, int col) {
        int rank = 7 - row;
        int file = col;
        String name = "" + (char)('a' + file) + (rank + 1);
        return Square.fromValue(name.toUpperCase());
    }

    // ── Builder helpers ───────────────────────────────────────────────

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

        javafx.scene.shape.Circle clip =
                new javafx.scene.shape.Circle(size / 2.0, size / 2.0, size / 2.0);
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
        if (username == null || username.isEmpty()) return "?";
        String[] parts = username.split("_");
        if (parts.length >= 2)
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }

    private String avatarColor(String username) {
        String[] palette = {
                "#7c5c3e", "#5c3e7c", "#3e7c5c",
                "#7c3e5c", "#3e5c7c", "#5c7c3e"
        };
        return palette[Math.abs(username.hashCode()) % palette.length];
    }
}
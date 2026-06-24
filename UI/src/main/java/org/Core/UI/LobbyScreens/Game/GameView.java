package org.Core.UI.LobbyScreens.Game;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.layout.*;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
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

    // ── Constants ─────────────────────────────────────────────────────
    private static final int    SQUARE_SIZE  = 72;
    private static final String LIGHT_SQUARE = "#f0d9b5";
    private static final String DARK_SQUARE  = "#779952";
    private static final String HIGHLIGHT    = "#f6f669";
    private static final String SELECT_COLOR = "#81b64c";
    private static final String DOT_COLOR    = "rgba(0,0,0,0.15)";

    // ── State ─────────────────────────────────────────────────────────
    private final Board          board        = new Board();
    private final GameFound      gameFound;
    private final UserSession    session;
    private final ViewNavigator  navigator;

    private Square               selectedSquare = null;
    private List<Square>         legalTargets   = new ArrayList<>();
    private String               lastMoveFrom   = null;
    private String               lastMoveTo     = null;
    private boolean              myTurn;


    private final StackPane      root;
    private final GridPane       boardGrid    = new GridPane();
    private final StackPane[][]  squares      = new StackPane[8][8];


    private final Label          myClockLabel  = new Label("10:00");
    private final Label          oppClockLabel = new Label("10:00");


    private final HBox           oppCaptured  = new HBox(2);
    private final HBox           myCaptured   = new HBox(2);


    public GameView(StackPane root,
                    GameFound gameFound,
                    UserSession session,
                    ViewNavigator navigator) {
        this.root      = root;
        this.gameFound = gameFound;
        this.session   = session;
        this.navigator = navigator;
        // TODO: replace with real color assignment from GameFound
        this.myTurn    = true;

        buildLayout();
        renderFromFen(gameFound.getFen());
    }

    // ── Public API ────────────────────────────────────────────────────

    /**
     * Render board from a FEN string.
     * Call on init, reconnect, or resync from server.
     */
    public void renderFromFen(String fen) {
        board.loadFromFen(fen);
        drawAllPieces();
        clearHighlights();
        selectedSquare = null;
        legalTargets.clear();
    }

    /**
     * Apply opponent move received from server.
     * Updates board state and re-renders the two affected squares.
     */
    public void applyServerMove(String from, String to, String newFen) {
        Platform.runLater(() -> {
            Move move = new Move(
                    Square.fromValue(from.toUpperCase()),
                    Square.fromValue(to.toUpperCase()));
            board.doMove(move);

            // sync check — trust server FEN
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

    /** Update clock labels. Call from WebSocket clock events. */
    public void updateClocks(String myTime, String oppTime) {
        Platform.runLater(() -> {
            myClockLabel.setText(myTime);
            oppClockLabel.setText(oppTime);
        });
    }

    public StackPane getView() { return root; }

    // ── Layout ────────────────────────────────────────────────────────

    private void buildLayout() {
        root.setStyle("-fx-background-color: #262421;");

        HBox body = new HBox(0);
        body.setAlignment(Pos.CENTER);

        VBox leftSidebar  = buildLeftSidebar();
        StackPane boardWrap = buildBoardArea();
        VBox rightSidebar = buildRightSidebar();

        body.getChildren().addAll(leftSidebar, boardWrap, rightSidebar);
        root.getChildren().add(body);
    }

    // ── Left sidebar ──────────────────────────────────────────────────

    private VBox buildLeftSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(120);
        sidebar.setMinWidth(120);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(12, 8, 12, 8));
        sidebar.setStyle("-fx-background-color: #262421;");

        // opponent info
        VBox oppInfo = buildPlayerInfo(
                gameFound.getOpponent().getUsername(),
                gameFound.getOpponent().getElo(),
                gameFound.getOpponent().getAvatarUrl(),
                false);

        // opponent captured pieces
        oppCaptured.setPadding(new Insets(4, 0, 4, 0));
        oppCaptured.setAlignment(Pos.CENTER_LEFT);

        // spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // my captured pieces
        myCaptured.setPadding(new Insets(4, 0, 4, 0));
        myCaptured.setAlignment(Pos.CENTER_LEFT);

        // my info
        VBox myInfo = buildPlayerInfo(
                session.getUsername(),
                session.getElo(),
                session.getAvatarUrl(),
                true);

        // clocks
        VBox oppClockBox = buildClockBox(oppClockLabel, false);
        VBox myClockBox  = buildClockBox(myClockLabel,  true);

        sidebar.getChildren().addAll(
                oppInfo, oppCaptured, oppClockBox,
                spacer,
                myClockBox, myCaptured, myInfo
        );
        return sidebar;
    }

    private VBox buildPlayerInfo(String username, int elo,
                                 String avatarUrl, boolean isMe) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 0, 8, 0));

        // avatar
        StackPane avatar = buildAvatar(avatarUrl,
                initials(username), avatarColor(username), 48);

        Label nameLbl = new Label(username);
        nameLbl.setStyle("""
            -fx-text-fill: #e8e4dc;
            -fx-font-size: 12px;
            -fx-font-weight: 700;
        """);
        nameLbl.setMaxWidth(100);

        Label eloLbl = new Label(elo + " ELO");
        eloLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

        box.getChildren().addAll(avatar, nameLbl, eloLbl);
        return box;
    }

    private VBox buildClockBox(Label clockLabel, boolean active) {
        VBox box = new VBox(0);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(4, 0, 4, 0));

        clockLabel.setStyle("""
            -fx-text-fill: #f0f0f0;
            -fx-font-size: 22px;
            -fx-font-weight: 900;
            -fx-background-color: #1a1a17;
            -fx-background-radius: 6;
            -fx-padding: 6 12;
        """);
        box.getChildren().add(clockLabel);
        return box;
    }

    // ── Board area ────────────────────────────────────────────────────

    private StackPane buildBoardArea() {
        StackPane wrap = new StackPane();

        boardGrid.setGridLinesVisible(false);

        // build 8x8 squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane sq = new StackPane();
                sq.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
                sq.setMinSize(SQUARE_SIZE, SQUARE_SIZE);
                sq.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);

                boolean isLight = (row + col) % 2 != 0;
                sq.setStyle("-fx-background-color: " +
                        (isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");

                final int r = row, c = col;
                sq.setOnMouseClicked(e -> handleSquareClick(r, c));

                squares[row][col] = sq;
                boardGrid.add(sq, col, row);
            }
        }

        wrap.getChildren().add(boardGrid);
        return wrap;
    }

    // ── Right sidebar ─────────────────────────────────────────────────

    private VBox buildRightSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(56);
        sidebar.setMinWidth(56);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(12, 8, 12, 8));
        sidebar.setStyle("-fx-background-color: #262421;");

        // draw offer button
        StackPane drawBtn = buildActionBtn("½", "#888888", () -> {
            // TODO: controller.offerDraw()
            System.out.println("Draw offered");
        });

        // resign button
        StackPane resignBtn = buildActionBtn("⚑", "#e05555", () -> {
            // TODO: controller.resign()
            System.out.println("Resigned");
        });

        sidebar.getChildren().addAll(drawBtn, resignBtn);
        return sidebar;
    }

    private StackPane buildActionBtn(String symbol, String color, Runnable action) {
        StackPane btn = new StackPane();
        btn.setPrefSize(40, 40);
        btn.setStyle("""
            -fx-background-color: #1e1e1b;
            -fx-background-radius: 6;
            -fx-cursor: hand;
        """);

        Label lbl = new Label(symbol);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: 700;");
        btn.getChildren().add(lbl);

        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #2a2a27;
            -fx-background-radius: 6;
            -fx-cursor: hand;
        """));
        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: #1e1e1b;
            -fx-background-radius: 6;
            -fx-cursor: hand;
        """));
        btn.setOnMouseClicked(e -> action.run());
        return btn;
    }

    // ── Board rendering ───────────────────────────────────────────────

    /**
     * Draw all pieces from current board state.
     * Only called on full render (init / renderFromFen).
     */
    private void drawAllPieces() {
        for (Square square : Square.values()) {
            if (square == Square.NONE) continue;
            int[] rc = squareToRowCol(square);
            clearSquareUI(rc[0], rc[1]);
            Piece piece = board.getPiece(square);
            if (piece != Piece.NONE) {
                placePiece(piece, rc[0], rc[1]);
            }
        }
    }

    /**
     * Redraw a single square by algebraic name e.g. "e2".
     * Used after incremental moves to avoid full re-render.
     */
    private void redrawSquare(String squareName) {
        Square sq = Square.fromValue(squareName.toUpperCase());
        if (sq == Square.NONE) return;
        int[] rc = squareToRowCol(sq);
        clearSquareUI(rc[0], rc[1]);
        Piece piece = board.getPiece(sq);
        if (piece != Piece.NONE) placePiece(piece, rc[0], rc[1]);
    }

    private void placePiece(Piece piece, int row, int col) {
        String imageName = piece.name(); // e.g. WHITE_PAWN, BLACK_QUEEN
        try {
            Image img = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream(
                                    "/Pieces/" + imageName + ".png")),
                    SQUARE_SIZE - 8, SQUARE_SIZE - 8, true, true);
            ImageView iv = new ImageView(img);
            iv.setMouseTransparent(true);
            squares[row][col].getChildren().add(iv);
        } catch (Exception e) {
            // fallback: show piece symbol if image missing
            Label fallback = new Label(piece.getFenSymbol());
            fallback.setStyle("-fx-text-fill: white; -fx-font-size: 28px;");
            fallback.setMouseTransparent(true);
            squares[row][col].getChildren().add(fallback);
        }
    }

    private void clearSquareUI(int row, int col) {
        // remove everything except the base color rectangle
        squares[row][col].getChildren().removeIf(
                n -> !(n instanceof Rectangle) || ((Rectangle) n).getUserData() == null);
        squares[row][col].getChildren().clear();
    }

    // ── Click handling ────────────────────────────────────────────────

    private void handleSquareClick(int row, int col) {
        Square clicked = rowColToSquare(row, col);

        if (selectedSquare == null) {
            // first click — select a piece
            Piece piece = board.getPiece(clicked);
            if (piece == Piece.NONE) return;

            // only allow clicking your own pieces on your turn
            // TODO: replace with real color check
            if (!myTurn) return;

            selectedSquare = clicked;
            legalTargets = board.legalMoves().stream()
                    .filter(m -> m.getFrom() == clicked)
                    .map(Move::getTo)
                    .collect(Collectors.toList());

            highlightSelected(row, col);
            highlightLegalMoves();

        } else {
            // second click — attempt move
            if (legalTargets.contains(clicked)) {
                commitMove(selectedSquare, clicked);
            } else {
                // re-select or deselect
                clearHighlights();
                selectedSquare = null;
                legalTargets.clear();

                Piece piece = board.getPiece(clicked);
                if (piece != Piece.NONE && myTurn) {
                    handleSquareClick(row, col); // re-select new piece
                }
            }
        }
    }

    private void commitMove(Square from, Square to) {
        Move move = new Move(from, to);

        // optimistic local update
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
        squares[row][col].setStyle(
                "-fx-background-color: " + SELECT_COLOR + ";");
    }

    private void highlightLegalMoves() {
        for (Square target : legalTargets) {
            int[] rc = squareToRowCol(target);
            Piece piece = board.getPiece(target);

            if (piece != Piece.NONE) {
                // capture ring
                squares[rc[0]][rc[1]].setStyle(
                        "-fx-background-color: " + SELECT_COLOR + ";");
            } else {
                // dot overlay
                Circle dot = new Circle(10);
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

        String baseFrom = (rcFrom[0] + rcFrom[1]) % 2 != 0 ? LIGHT_SQUARE : DARK_SQUARE;
        String baseTo   = (rcTo[0]   + rcTo[1])   % 2 != 0 ? LIGHT_SQUARE : DARK_SQUARE;

        squares[rcFrom[0]][rcFrom[1]].setStyle("-fx-background-color: " + HIGHLIGHT + ";");
        squares[rcTo[0]][rcTo[1]].setStyle("-fx-background-color: " + HIGHLIGHT + ";");
    }

    private void clearHighlights() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean isLight = (r + c) % 2 != 0;
                squares[r][c].setStyle("-fx-background-color: " +
                        (isLight ? LIGHT_SQUARE : DARK_SQUARE) + ";");
                // remove dots
                squares[r][c].getChildren()
                        .removeIf(n -> "dot".equals(n.getUserData()));
            }
        }
        // reapply last move highlight
        if (lastMoveFrom != null) applyLastMoveHighlight();
    }

    // ── Coordinate helpers ────────────────────────────────────────────

    /**
     * Convert chesslib Square to [row, col] for the board grid.
     * row 0 = rank 8 (top), row 7 = rank 1 (bottom) — white perspective.
     * TODO: flip board if playing as black.
     */
    private int[] squareToRowCol(Square square) {
        int file = square.getFile().ordinal(); // 0=a, 7=h
        int rank = square.getRank().ordinal(); // 0=rank1, 7=rank8
        int row  = 7 - rank; // flip so rank8 is at top
        int col  = file;
        return new int[]{row, col};
    }

    private Square rowColToSquare(int row, int col) {
        int rank = 7 - row;
        int file = col;
        String name = "" + (char)('a' + file) + (rank + 1);
        return Square.fromValue(name.toUpperCase());
    }

    // ── Avatar builder ────────────────────────────────────────────────

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
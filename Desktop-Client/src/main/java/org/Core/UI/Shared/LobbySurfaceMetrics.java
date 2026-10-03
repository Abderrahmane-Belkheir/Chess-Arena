package org.Core.UI.Shared;

/**
 * Shared sizing for the Lobby's bounded surface frame. Any other screen that
 * transitions directly to/from the Lobby (matchmaking, etc.) should bind its
 * own surface to these same values rather than picking its own proportions —
 * otherwise the surface visibly jumps to a different width/height on
 * transition, reading as a separate window popping up instead of the same
 * application frame staying put.
 */
public final class LobbySurfaceMetrics {

    private LobbySurfaceMetrics() {}

    // The fixed width every card inside the surface is laid out at.
    public static final double CONTENT_WIDTH = 460;

    // Horizontal-only gutter between the surface's own border and the
    // fixed-width content inside it — see LobbyView for why it's
    // horizontal-only (top/bottom stay flush with the nav bar/bottom nav).
    public static final double SURFACE_PADDING = 44;

    public static final double SURFACE_WIDTH = CONTENT_WIDTH + 2 * SURFACE_PADDING;

    // How much of the window's height the surface may use.
    public static final double SURFACE_HEIGHT_FRACTION = 0.95;
}

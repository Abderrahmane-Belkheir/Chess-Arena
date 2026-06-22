
package org.Core.UI.LobbyScreens.Friends;


public final class Styles {

    private Styles() {}

    public static String activeTabStyle() {
        return """
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #f0f0f0;
            -fx-background-radius: 7;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 10px;
            -fx-font-weight: 700;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }

    public static String inactiveTabStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #555555;
            -fx-background-radius: 7;
            -fx-border-color: #1e1e1e;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 10px;
            -fx-font-weight: 700;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }

    public static String addBtnStyle() {
        return """
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #888888;
            -fx-background-radius: 7;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
            -fx-cursor: hand;
        """;
    }

    public static String friendBtnStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #81b64c;
            -fx-background-radius: 7;
            -fx-border-color: #81b64c;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
        """;
    }

    public static String pendingBtnStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #e6b84a;
            -fx-background-radius: 7;
            -fx-border-color: #e6b84a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
            -fx-cursor: hand;
        """;
    }

    public static String acceptBtnStyleLarge() {
        return """
            -fx-background-color: #81b64c;
            -fx-text-fill: #1a1a14;
            -fx-background-radius: 7;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
            -fx-cursor: hand;
        """;
    }

    public static String acceptBtnStyleSmall() {
        return """
            -fx-background-color: #81b64c;
            -fx-text-fill: #1a1a14;
            -fx-background-radius: 6;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }

    public static String rejectBtnStyleLarge() {
        return """
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #666666;
            -fx-background-radius: 7;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 7;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 8 0;
            -fx-cursor: hand;
        """;
    }

    public static String rejectBtnStyleSmall() {
        return """
            -fx-background-color: #1e1e1e;
            -fx-text-fill: #666666;
            -fx-background-radius: 6;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }

    public static String cancelRequestBtnStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #666666;
            -fx-background-radius: 6;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-padding: 7 0;
            -fx-cursor: hand;
        """;
    }
}
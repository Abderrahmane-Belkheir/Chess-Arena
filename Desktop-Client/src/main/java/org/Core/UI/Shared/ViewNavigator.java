package org.Core.UI.Shared;

import com.google.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ViewNavigator {

    private final StackPane root;

    // The view we last transitioned to, tracked explicitly rather than
    // inferred from getChildren() — during a fade, root briefly holds two
    // children, so "children.get(0)" stops reliably meaning "the current
    // view" the moment two transitions can overlap (e.g. Play then almost
    // immediately Cancel).
    private Node current;
    private FadeTransition activeFade;

    @Inject
    public ViewNavigator(StackPane root) {
        this.root = root;
    }

    public void transitionTo(StackPane newView) {
        if (newView == current) return;

        // Screens like the Lobby are a single persistent StackPane reused
        // across transitions (matchmaking's Cancel button hands the exact
        // same lobby view back), not recreated each time. The fade below
        // leaves the *old* view's opacity at 0 once it's faded out — if
        // that same node later comes back as a newView without resetting
        // it, it re-enters invisible, and fading the (also opaque-at-start)
        // view in front of it away then reveals nothing but the root's own
        // near-black background: everything goes black. Always force it
        // back to fully opaque here, unconditionally.
        newView.setOpacity(1);

        // If the previous transition's fade-out is still running, stop it
        // immediately rather than leaving two independent fades mutating
        // overlapping nodes at once.
        if (activeFade != null) {
            activeFade.stop();
            activeFade = null;
        }

        Node old = current;
        current = newView;

        // Defensive: drop newView from root first (no-op if it isn't there)
        // so re-adding it can never throw "duplicate children added",
        // which a rapid-fire transition could otherwise trigger while the
        // previous transition's own add/remove is still mid-flight.
        root.getChildren().remove(newView);

        if (old == null) {
            root.getChildren().setAll(newView);
            return;
        }

        // newView goes in fully opaque, behind old (StackPane paints later
        // children on top, so inserting at index 0 puts it at the back) —
        // old is the only thing that animates, fading away on top of it.
        // Two earlier approaches both had a visible artifact: fading old and
        // new at the same time briefly stacks two partially-opaque glowing
        // "lobby-environment" backgrounds on top of each other (a flash of
        // light); fading old out, removing it, then fading new in from
        // scratch leaves a gap with nothing opaque on screen at all (a black
        // blink). Since newView is already fully rendered underneath from
        // frame one, neither gap exists — this is just an ordinary dissolve.
        root.getChildren().add(0, newView);

        FadeTransition out = new FadeTransition(Duration.millis(220), old);
        out.setFromValue(old.getOpacity());
        out.setToValue(0);
        out.setOnFinished(e -> {
            root.getChildren().remove(old);
            activeFade = null;
        });
        activeFade = out;
        out.play();
    }
}
package org.Core.UI.LobbyScreens.Friends;


import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;


import javax.security.sasl.AuthenticationException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * A single Friends-panel tab (online / offline / requests). No pagination —
 * the server returns the whole list in one request, so a section loads once
 * per session: eagerly for Online (called right after construction) or
 * lazily for Offline/Requests (called the first time their tab is opened).
 */
public abstract class FriendSection<T> {

    private static final String EMPTY_STATE_MARKER = "empty-state";

    protected final SectionState<T> state = new SectionState<>();

    protected abstract List<T> fetchAll() throws Exception;

    protected abstract Node buildRow(T item);

    protected abstract String emptyMessage();

    /** Optional hook called after the list has been (re)loaded (FX thread). */
    protected void onItemsLoaded(List<T> items) { }

    public VBox getList()  { return state.getList(); }
    public VBox getItems() { return state.getItems(); }
    public SectionState<T> getState() { return state; }

    /** Wire up the two-layer VBox structure. Call once, e.g. in the constructor. */
    protected void initList() {
        state.getList().setFillWidth(true);
        state.getItems().setFillWidth(true);
        state.getList().getChildren().add(state.getItems());
    }

    public void loadInitial() {
        if (state.isLoadedOnce() || state.isLoading()) return;
        fetch();
    }

    protected void fetch() {
        state.setLoading(true);
        showLoader();

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return fetchAll();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(items -> Platform.runLater(() -> onLoaded(items)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        state.setLoading(false);
                        hideFooter();
                        showError(resolveError(ex));
                    });
                    return null;
                });
    }

    private void onLoaded(List<T> items) {
        state.setLoading(false);
        state.setLoadedOnce(true);

        List<T> safeItems = items != null ? items : List.of();
        state.getItems().getChildren().clear();
        safeItems.forEach(item -> state.getItems().getChildren().add(buildRow(item)));

        onItemsLoaded(safeItems);

        hideFooter();

        addEmptyStateIfNeeded();
    }

    protected void showLoader() {
        hideFooter();
        state.getList().getChildren().add(FooterViews.buildBigLoader());
    }

    protected void hideFooter() {
        state.getList().getChildren().removeIf(n -> "footer".equals(n.getUserData()));
    }

    protected void showError(String message) {
        hideFooter();
        state.getList().getChildren().add(FooterViews.buildError(message, this::fetch));
    }

    /** Re-check the empty state after a row is removed individually (accept/reject/cancel). */
    protected void checkEmpty() {
        addEmptyStateIfNeeded();
    }

    /** Number of real rows currently shown — excludes the empty-state placeholder. */
    protected int itemCount() {
        return (int) state.getItems().getChildren().stream()
                .filter(n -> !EMPTY_STATE_MARKER.equals(n.getUserData()))
                .count();
    }

    private void addEmptyStateIfNeeded() {
        if (state.getItems().getChildren().isEmpty()) {
            Node empty = FooterViews.buildEmptyState(emptyMessage());
            empty.setUserData(EMPTY_STATE_MARKER);
            state.getItems().getChildren().add(empty);
        }
    }

    /**
     * Inserts a row at the top of this section's list (not appended at the
     * bottom), clearing the empty-state placeholder first if it's the only
     * thing showing. Not called anywhere yet — exposed for later use by live
     * status updates that move a single friend into a section without a
     * full refetch.
     */
    protected void insertAtTop(Node row) {
        state.getItems().getChildren().removeIf(n -> EMPTY_STATE_MARKER.equals(n.getUserData()));
        state.getItems().getChildren().add(0, row);
    }

    /**
     * Removes the first row matching the given predicate (e.g. by an id
     * stashed in the row's userData) and re-checks the empty state. Not
     * called anywhere yet — exposed for later use by live status updates
     * that move a friend out of a section without a full refetch.
     *
     * @return the item stored in the removed row's userData, or null if no
     *         row matched (i.e. this section didn't have that item)
     */
    @SuppressWarnings("unchecked")
    protected T removeRowIf(Predicate<Node> predicate) {
        var children = state.getItems().getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node n = children.get(i);
            if (predicate.test(n)) {
                int finalI = i;
                Platform.runLater(()->{
                  children.remove(finalI);
                  checkEmpty();
              });
                return (T) n.getUserData();
            }
        }
        return null;
    }

    protected String resolveError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AuthenticationException)
            return "Session expired. Please log in again.";
        return "Request failed. Check your connection.";
    }
}

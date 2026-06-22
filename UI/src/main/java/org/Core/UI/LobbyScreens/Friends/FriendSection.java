package org.Core.UI.LobbyScreens.Friends;


import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;


import javax.security.sasl.AuthenticationException;
import java.util.concurrent.CompletableFuture;

public abstract class FriendSection<T> {

    protected final SectionState<T> state = new SectionState<>();

    protected abstract PageResult<T> fetchPage(String cursor) throws Exception;

    protected abstract Node buildRow(T item);

    protected abstract String emptyMessage();

    /**
     * Optional hook called after a page of items has been appended to the
     * list (FX thread). Default no-op; override to update tab counts, etc.
     */
    protected void onItemsAppended(PageResult<T> page) { }

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
        fetch(true);
    }

    public void loadMore() {
        if (!state.isLoading() && state.isHasMore()) {
            fetch(false);
        }
    }

    protected void fetch(boolean isInitial) {
        if (!state.isHasMore()) return;
        state.setLoading(true);
        showLoader(isInitial);

        String cursor = state.getCursor();

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return fetchPage(cursor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(page -> Platform.runLater(() -> onPageLoaded(page, isInitial)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        state.setLoading(false);
                        hideFooter();
                        showError(resolveError(ex), isInitial);
                    });
                    return null;
                });
    }

    private void onPageLoaded(PageResult<T> page, boolean isInitial) {
        state.setLoading(false);
        state.setLoadedOnce(true);
        state.setCursor(page.nextCursor());
        state.setHasMore(page.hasMore());

        page.items().forEach(item -> state.getItems().getChildren().add(buildRow(item)));

        onItemsAppended(page);

        // Some backends can return an empty page while still signalling more
        // data is available (e.g. a page of all-already-loaded items).
        if (page.items().isEmpty() && state.isHasMore()) {
            fetch(false);
            return;
        }

        hideFooter();

        if (state.getItems().getChildren().isEmpty() && !state.isHasMore()) {
            state.getItems().getChildren().add(FooterViews.buildEmptyState(emptyMessage()));
        }
    }

    protected void showLoader(boolean isInitial) {
        hideFooter();
        Node loader = isInitial ? FooterViews.buildBigLoader() : FooterViews.buildInlineLoader();
        state.getList().getChildren().add(loader);
    }

    protected void hideFooter() {
        state.getList().getChildren().removeIf(n -> "footer".equals(n.getUserData()));
    }

    protected void showError(String message, boolean isInitial) {
        hideFooter();
        state.getList().getChildren().add(FooterViews.buildError(message, () -> fetch(isInitial)));
    }

    /** Re-check the empty state after a row is removed individually (accept/reject/cancel). */
    protected void checkEmpty() {
        if (state.getItems().getChildren().isEmpty() && !state.isHasMore()) {
            state.getItems().getChildren().add(FooterViews.buildEmptyState(emptyMessage()));
        }
    }

    protected String resolveError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AuthenticationException)
            return "Session expired. Please log in again.";
        return "Request failed. Check your connection.";
    }
}
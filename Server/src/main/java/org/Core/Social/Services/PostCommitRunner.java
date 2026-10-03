package org.Core.Social.Services;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Defers task until the current transaction commits, if there is one —
 * so a later rollback can't leave a push already sent for something that
 * never actually happened. Runs task immediately when there's no active
 * transaction (e.g. called from a plain background thread).
 */
public final class PostCommitRunner {

    private PostCommitRunner() {}

    public static void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}

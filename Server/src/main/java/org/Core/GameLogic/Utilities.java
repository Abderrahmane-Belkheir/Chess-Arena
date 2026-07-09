package org.Core.GameLogic;

import java.util.concurrent.TimeUnit;

public final class Utilities {
    public static final String TEST_POSITION="6k1/5ppp/6K1/8/8/8/5Q2/8 w - - 0 1";
    public static final String START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public final static long TEN_MINUTES_MS = TimeUnit.MINUTES.toMillis(10);
    public final static long THREE_MINUTES_MS=TimeUnit.MINUTES.toMillis(3);

    private Utilities() {}

}
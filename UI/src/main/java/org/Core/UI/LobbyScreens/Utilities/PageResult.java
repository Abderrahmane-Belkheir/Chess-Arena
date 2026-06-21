package org.Core.UI.LobbyScreens.Utilities;

import java.util.List;

public record PageResult<T>(List<T> items, String nextCursor, boolean hasMore) {}
package org.Core.Game.Logic.Services.Matchmaking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.Core.Game.Logic.Api.Dto.UserSession;
import org.Core.Game.Logic.Models.Game;
import org.Core.Game.Logic.Services.Game.GameFactory;
import org.Core.User.Exceptions.UserNotFoundException;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchmakingEntry {

    private final MatchmakingQueueService queueService;
    private final GameFactory gameFactory;
    private final UserRepo userRepo;
    private final List<QueueEntry> entries=new ArrayList<>();

    public synchronized void searchGame(UserSession session) {
        User user=userRepo.findById(session.userId()).orElseThrow(()->new UserNotFoundException("User not found"));
        if(user.getStatus()== User.Status.IN_GAME) throw new RuntimeException();
        QueueEntry player=new QueueEntry(session.userId(),user.getPublicId(), user.getUsername(),user.getElo(),user.getAvatarUrl(),System.currentTimeMillis(), session.sessionId());
        if(entries.isEmpty()) {
            entries.add(player);
            return;
        }
        QueueEntry opponent=entries.get(0);
        gameFactory.createGame(new MatchedPair(opponent,player), Game.GameType.RAPID, Game.MatchMode.RANKED);
        entries.removeAll(List.of(player,opponent));
    }

    public synchronized void stopSearch(String userId) {
        entries.removeIf(entry -> entry.userId().equals(userId));
    }

}

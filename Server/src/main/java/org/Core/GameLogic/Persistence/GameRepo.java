package org.Core.GameLogic.Persistence;

import org.Core.GameLogic.Services.Game.Events.GameOverInfo;
import org.Core.GameLogic.Models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface GameRepo extends JpaRepository<Game,String> {

    @Transactional
    @Modifying
    @Query("UPDATE Game g SET g.fen=:fen WHERE g.id=:gameId")
    void updateFen(@Param("gameId") String gameId,@Param("fen") String fen);

    @Transactional
    @Modifying
    @Query("UPDATE Game g SET g.result=:result,g.status=:status,g.endReason=:reason,g.endedAt=:at WHERE g.id=:gameId")
    void endGame(@Param("gameId") String gameId, @Param("result") Game.Result result,@Param("status") Game.GameStatus status, @Param("reason")GameOverInfo.EndReason reason,@Param("at") Instant at);


}

package org.Core.GameLogic.Persistence;

import org.Core.GameLogic.Models.GameMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameMoveRepo extends JpaRepository<GameMove,String> {
}

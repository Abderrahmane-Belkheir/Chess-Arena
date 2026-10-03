package org.Core.Game.Logic.Persistence;


import org.Core.Game.Logic.Models.GameMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameMoveRepo extends JpaRepository<GameMove,String> {
}

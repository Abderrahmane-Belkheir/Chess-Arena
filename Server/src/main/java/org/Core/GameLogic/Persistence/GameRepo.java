package org.Core.GameLogic.Persistence;

import org.Core.GameLogic.Models.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepo extends JpaRepository<Game,String> {
}

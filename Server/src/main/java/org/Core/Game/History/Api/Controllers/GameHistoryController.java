package org.Core.Game.History.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Game.History.Api.Dto.GameDetails;
import org.Core.Game.History.Api.Dto.GameHistory;
import org.Core.Game.History.Services.GamesHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game")
public class GameHistoryController {


    private final GamesHistoryService historyService;

    @GetMapping("/history")
    public ResponseEntity<GameHistory> getHistory(){
        return ResponseEntity.ok(historyService.fetchGames());
    }

    @GetMapping("/summary/{gameId}")
    public ResponseEntity<GameDetails> getGame(@RequestParam String gameId){
        return ResponseEntity.ok(historyService.fetchGameDetails(gameId));
    }

    @GetMapping("/moves/{gameId}")
    public ResponseEntity<List<String>> getGameMoves(@RequestParam String gameId){
        return ResponseEntity.ok(historyService.fetchGameMoves(gameId));
    }

}

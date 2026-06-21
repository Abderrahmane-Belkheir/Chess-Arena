package org.Core.Social.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsPage;
import org.Core.Social.Services.FriendShipQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/social")
public class FriendShipQueryController {

    private final FriendShipQuery friendShipQuery;
    @GetMapping("/friends")
    public ResponseEntity<FriendsPage> get(@RequestParam(required = false) String cursor) {


            // ── page 1 (9 friends) ──────────────────────────────────

        if (cursor.equals("page2")) {

            List<FriendsPage.FriendEntry> page2 = List.of(
                    new FriendsPage.FriendEntry("sneaky_sicilian", 1455, FriendsPage.Status.InLobby, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("king_hunter",     1980, FriendsPage.Status.InGame,  "", "#5c3e7c"),
                    new FriendsPage.FriendEntry("opening_book",    1320, FriendsPage.Status.InGame, "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("rapid_ruth",      1610, FriendsPage.Status.InLobby, "", "#3e7c5c"),
                    new FriendsPage.FriendEntry("blunder_buddy",   890,  FriendsPage.Status.InGame, "", "#7c3e3e"),
                    new FriendsPage.FriendEntry("tactics_tina",    1755, FriendsPage.Status.InGame,  "", "#5c7c3e"),
                    new FriendsPage.FriendEntry("zugzwang_zane",   2110, FriendsPage.Status.InGame, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("castling_cathy",  1430, FriendsPage.Status.InLobby, "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("forkmaster",      1295, FriendsPage.Status.InGame,  "", "#7c3e5c")
            );
            return ResponseEntity.ok(new FriendsPage(page2, "page3", true));
        }
        if (cursor.equals("page5")) {

            List<FriendsPage.FriendEntry> page2 = List.of(
                    new FriendsPage.FriendEntry("sneaky_sicilian", 1455, FriendsPage.Status.InLobby, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("king_hunter",     1980, FriendsPage.Status.InGame,  "", "#5c3e7c"),
                    new FriendsPage.FriendEntry("opening_book",    1320, FriendsPage.Status.InGame, "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("rapid_ruth",      1610, FriendsPage.Status.InLobby, "", "#3e7c5c"),
                    new FriendsPage.FriendEntry("blunder_buddy",   890,  FriendsPage.Status.InGame, "", "#7c3e3e"),
                    new FriendsPage.FriendEntry("tactics_tina",    1755, FriendsPage.Status.InGame,  "", "#5c7c3e"),
                    new FriendsPage.FriendEntry("zugzwang_zane",   2110, FriendsPage.Status.InGame, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("castling_cathy",  1430, FriendsPage.Status.InLobby, "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("forkmaster",      1295, FriendsPage.Status.InGame,  "", "#7c3e5c")
            );
            return ResponseEntity.ok(new FriendsPage(page2, null, false));
        }
        if (cursor.equals("page4")) {

            List<FriendsPage.FriendEntry> page2 = List.of(
                    new FriendsPage.FriendEntry("sneaky_sicilian", 1455, FriendsPage.Status.InLobby, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("king_hunter",     1980, FriendsPage.Status.InGame,  "", "#5c3e7c"),
                    new FriendsPage.FriendEntry("opening_book",    1320, FriendsPage.Status.InGame, "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("rapid_ruth",      1610, FriendsPage.Status.InLobby, "", "#3e7c5c"),
                    new FriendsPage.FriendEntry("blunder_buddy",   890,  FriendsPage.Status.InGame, "", "#7c3e3e"),
                    new FriendsPage.FriendEntry("tactics_tina",    1755, FriendsPage.Status.InGame,  "", "#5c7c3e"),
                    new FriendsPage.FriendEntry("zugzwang_zane",   2110, FriendsPage.Status.InGame, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("castling_cathy",  1430, FriendsPage.Status.InLobby, "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("forkmaster",      1295, FriendsPage.Status.InGame,  "", "#7c3e5c")
            );
            return ResponseEntity.ok(new FriendsPage(page2, "page5", true));
        }

        if (cursor.equals("page3")) {
            // ── page 3 (9 friends, last page → hasMore=false, nextCursor=null) ──
            List<FriendsPage.FriendEntry> page3 = List.of(
                    new FriendsPage.FriendEntry("en_passant_pete", 1505, FriendsPage.Status.InGame, "", "#5c3e7c"),
                    new FriendsPage.FriendEntry("gambit_gary",     1640, FriendsPage.Status.InLobby, "", "#3e7c7c"),
                    new FriendsPage.FriendEntry("stalemate_sam",   1120, FriendsPage.Status.InGame, "", "#7c5c3e"),
                    new FriendsPage.FriendEntry("promotion_priya", 1860, FriendsPage.Status.InGame,  "", "#3e5c7c"),
                    new FriendsPage.FriendEntry("fianchetto_finn", 1390, FriendsPage.Status.InGame, "", "#7c3e3e"),
                    new FriendsPage.FriendEntry("draw_offer_dana", 1565, FriendsPage.Status.InLobby, "", "#5c7c3e"),
                    new FriendsPage.FriendEntry("time_pressure_tj",1980, FriendsPage.Status.InGame,  "", "#7c3e5c"),
                    new FriendsPage.FriendEntry("smothered_sasha", 1250, FriendsPage.Status.InGame, "", "#3e7c5c"),
                    new FriendsPage.FriendEntry("back_rank_billy", 1715, FriendsPage.Status.InLobby, "", "#5c3e7c")
            );
            return ResponseEntity.ok(new FriendsPage(page3, "page4", true));
        }
        List<FriendsPage.FriendEntry> page1 = List.of(
                new FriendsPage.FriendEntry("knight_rider",   1685, FriendsPage.Status.InLobby, "", "#5c3e7c"),
                new FriendsPage.FriendEntry("queens_gambit",  1502, FriendsPage.Status.InGame,  "", "#7c3e3e"),
                new FriendsPage.FriendEntry("pawnstar",       1340, FriendsPage.Status.InGame,  "", "#3e5c7c"),
                new FriendsPage.FriendEntry("rook_n_roll",    1899, FriendsPage.Status.InLobby, "", "#7c5c3e"),
                new FriendsPage.FriendEntry("bishop_bash",    1455, FriendsPage.Status.InLobby, "", "#7c3e5c"),
                new FriendsPage.FriendEntry("endgame_emma",   2050, FriendsPage.Status.InLobby, "", "#3e7c7c"),
                new FriendsPage.FriendEntry("casual_carl",    980,  FriendsPage.Status.InGame, "", "#5c7c3e"),
                new FriendsPage.FriendEntry("blitz_betty",    1722, FriendsPage.Status.InLobby, "", "#3e5c7c"),
                new FriendsPage.FriendEntry("checkmate_carl", 1610, FriendsPage.Status.InGame,  "", "#7c3e5c")
        );
        return ResponseEntity.ok(new FriendsPage(page1, "page2", true));
    }

}

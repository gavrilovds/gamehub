package ru.gavrilovds.hw1.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.gavrilovds.hw1.generated.api.LeaderboardApi
import ru.gavrilovds.hw1.generated.model.LeaderboardEntry
import ru.gavrilovds.hw1.generated.model.LeaderboardRankResponse
import ru.gavrilovds.hw1.generated.model.LeaderboardScoreRequest
import ru.gavrilovds.hw1.generated.model.ScoreResponse
import ru.gavrilovds.hw1.service.LeaderboardService

@RestController
class LeaderboardController(private val leaderboard: LeaderboardService) : LeaderboardApi {
    override fun addLeaderboardScore(leaderboardScoreRequest: LeaderboardScoreRequest): ResponseEntity<ScoreResponse> =
        ResponseEntity.ok(
            leaderboard.addScore(leaderboardScoreRequest.playerId, leaderboardScoreRequest.score),
        )

    override fun getLeaderboardTop(limit: Int): ResponseEntity<List<LeaderboardEntry>> =
        ResponseEntity.ok(leaderboard.top(limit))

    override fun getLeaderboardRank(playerId: String): ResponseEntity<LeaderboardRankResponse> =
        ResponseEntity.ok(leaderboard.rank(playerId))
}

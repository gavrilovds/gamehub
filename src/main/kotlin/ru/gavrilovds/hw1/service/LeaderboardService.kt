package ru.gavrilovds.hw1.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import ru.gavrilovds.hw1.exception.PlayerNotFoundException
import ru.gavrilovds.hw1.generated.model.LeaderboardEntry
import ru.gavrilovds.hw1.generated.model.LeaderboardRankResponse
import ru.gavrilovds.hw1.generated.model.ScoreResponse
import ru.gavrilovds.hw1.redis.RedisKeys

@Service
class LeaderboardService(private val redis: StringRedisTemplate) {
    fun addScore(playerId: String, score: Double): ScoreResponse {
        require(score > 0) { "score must be greater than zero" }
        val total = redis.opsForZSet().incrementScore(RedisKeys.LEADERBOARD, playerId, score)
            ?: error("Redis did not return the updated score")
        return ScoreResponse(playerId, total)
    }

    fun top(limit: Int): List<LeaderboardEntry> = redis.opsForZSet()
        .reverseRangeWithScores(RedisKeys.LEADERBOARD, 0, (limit - 1).toLong())
        .orEmpty()
        .mapIndexed { index, tuple ->
            LeaderboardEntry(index + 1, requireNotNull(tuple.value), tuple.score ?: 0.0)
        }

    fun rank(playerId: String): LeaderboardRankResponse {
        val rank = redis.opsForZSet().rank(RedisKeys.LEADERBOARD, playerId)
            ?: throw PlayerNotFoundException(playerId)
        return LeaderboardRankResponse(
            playerId,
            rank,
            rank + 1,
            LeaderboardRankResponse.Order.ASC,
        )
    }
}

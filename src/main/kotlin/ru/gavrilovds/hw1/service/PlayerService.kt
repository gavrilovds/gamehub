package ru.gavrilovds.hw1.service

import org.springframework.stereotype.Service
import ru.gavrilovds.hw1.exception.PlayerNotFoundException
import ru.gavrilovds.hw1.generated.model.BatchPlayersRequest
import ru.gavrilovds.hw1.generated.model.BatchResponse
import ru.gavrilovds.hw1.generated.model.PlayerResponse
import ru.gavrilovds.hw1.generated.model.UpsertPlayerRequest
import ru.gavrilovds.hw1.redis.PlayerRecord
import ru.gavrilovds.hw1.redis.PlayerRedisRepository
import java.time.Clock
import java.time.OffsetDateTime

@Service
class PlayerService(
    private val players: PlayerRedisRepository,
    private val notifications: NotificationService,
    private val clock: Clock,
) {
    data class CachedPlayer(val profile: PlayerResponse, val cacheHit: Boolean)

    fun upsert(id: String, request: UpsertPlayerRequest): PlayerResponse = players.upsert(
        id = id,
        name = request.name,
        level = request.level,
        region = request.region,
        createdAt = OffsetDateTime.now(clock),
    ).toResponse()

    fun get(id: String): CachedPlayer {
        players.findCached(id)?.let { return CachedPlayer(it.toResponse(), true) }

        val player = findPlayer(id)
        players.cache(player)
        return CachedPlayer(player.toResponse(), false)
    }

    fun updateLevel(id: String, delta: Int): PlayerResponse {
        val current = findPlayer(id)
        val newLevel = players.incrementLevel(id, delta).toInt()
        notifications.levelChanged(id, newLevel)
        return current.copy(level = newLevel).toResponse()
    }

    fun batch(request: BatchPlayersRequest): BatchResponse {
        val startedAt = System.nanoTime()
        val createdAt = OffsetDateTime.now(clock)
        val records = request.players.map { player ->
            PlayerRecord(
                id = player.id,
                name = player.name,
                level = player.level,
                region = player.region,
                createdAt = createdAt,
            )
        }
        players.saveBatch(records)
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        return BatchResponse(records.size, elapsedMs, true)
    }

    private fun findPlayer(id: String) = players.find(id) ?: throw PlayerNotFoundException(id)

    private fun PlayerRecord.toResponse() = PlayerResponse(
        id = id,
        name = name,
        level = level,
        region = region,
        createdAt = createdAt,
    )
}

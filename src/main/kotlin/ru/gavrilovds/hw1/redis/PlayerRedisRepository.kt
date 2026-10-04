package ru.gavrilovds.hw1.redis

import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import ru.gavrilovds.hw1.config.GameHubProperties
import tools.jackson.databind.ObjectMapper
import java.time.OffsetDateTime

data class PlayerRecord(
    val id: String,
    val name: String,
    val level: Int,
    val region: String,
    val createdAt: OffsetDateTime,
)

@Repository
class PlayerRedisRepository(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val properties: GameHubProperties,
) {
    fun upsert(id: String, name: String, level: Int, region: String, createdAt: OffsetDateTime): PlayerRecord {
        val key = RedisKeys.player(id)
        val storedCreatedAt = redis.opsForHash<String, String>().get(key, CREATED_AT)
            ?.let(OffsetDateTime::parse)
            ?: createdAt
        val player = PlayerRecord(id, name, level, region, storedCreatedAt)

        redis.opsForHash<String, String>().putAll(key, player.toHash())
        evictCache(id)
        return player
    }

    fun find(id: String): PlayerRecord? {
        val fields = redis.opsForHash<String, String>().entries(RedisKeys.player(id))
        return fields.takeIf(Map<*, *>::isNotEmpty)?.toPlayer(id)
    }

    fun findCached(id: String): PlayerRecord? = redis.opsForValue()
        .get(RedisKeys.playerCache(id))
        ?.let { objectMapper.readValue(it, PlayerRecord::class.java) }

    fun cache(player: PlayerRecord) {
        redis.opsForValue().set(
            RedisKeys.playerCache(player.id),
            objectMapper.writeValueAsString(player),
            properties.cache.playerTtl,
        )
    }

    fun incrementLevel(id: String, delta: Int): Long {
        val level = redis.opsForHash<String, String>()
            .increment(RedisKeys.player(id), LEVEL, delta.toLong())
        evictCache(id)
        return level
    }

    fun saveBatch(players: List<PlayerRecord>) {
        redis.executePipelined(
            RedisCallback<Any?> { connection ->
                players.forEach { player ->
                    val hashArguments = player.toHash().flatMap { (field, value) ->
                        listOf(field.bytes(), value.bytes())
                    }
                    connection.execute(
                        HSET,
                        RedisKeys.player(player.id).bytes(),
                        *hashArguments.toTypedArray(),
                    )
                    connection.execute(DEL, RedisKeys.playerCache(player.id).bytes())
                }
                null
            },
        )
    }

    private fun evictCache(id: String) {
        redis.delete(RedisKeys.playerCache(id))
    }

    private fun PlayerRecord.toHash() = linkedMapOf(
        NAME to name,
        LEVEL to level.toString(),
        REGION to region,
        CREATED_AT to createdAt.toString(),
    )

    private fun Map<String, String>.toPlayer(id: String) = PlayerRecord(
        id = id,
        name = getValue(NAME),
        level = getValue(LEVEL).toInt(),
        region = getValue(REGION),
        createdAt = OffsetDateTime.parse(getValue(CREATED_AT)),
    )

    private fun String.bytes() = toByteArray(Charsets.UTF_8)

    private companion object {
        const val NAME = "name"
        const val LEVEL = "level"
        const val REGION = "region"
        const val CREATED_AT = "created_at"
        const val HSET = "HSET"
        const val DEL = "DEL"
    }
}

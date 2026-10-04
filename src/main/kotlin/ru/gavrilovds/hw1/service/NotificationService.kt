package ru.gavrilovds.hw1.service

import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import ru.gavrilovds.hw1.config.GameHubProperties
import ru.gavrilovds.hw1.stream.NotificationEvent
import java.time.Clock
import java.time.Instant

@Service
class NotificationService(
    private val redis: StringRedisTemplate,
    private val properties: GameHubProperties,
    private val clock: Clock,
) {
    fun levelChanged(playerId: String, level: Int) {
        val event = NotificationEvent.levelChanged(playerId, level, Instant.now(clock))
        val stream = properties.notifications.stream
        redis.opsForStream<String, String>().add(MapRecord.create(stream, event.toFields()))
        trimExpiredEntries()
    }

    private fun trimExpiredEntries() {
        val cutoff = Instant.now(clock).minus(properties.notifications.retention).toEpochMilli().toString()
        redis.execute { connection ->
            connection.execute(
                "XTRIM",
                properties.notifications.stream.bytes(),
                "MINID".bytes(),
                "~".bytes(),
                cutoff.bytes(),
            )
            null
        }
    }

    private fun String.bytes() = toByteArray(Charsets.UTF_8)
}

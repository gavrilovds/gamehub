package ru.gavrilovds.hw1.stream

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import ru.gavrilovds.hw1.config.GameHubProperties

@Component
class NotificationStreamInitializer(
    private val redis: StringRedisTemplate,
    private val properties: GameHubProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun ensureGroup() {
        try {
            redis.execute { connection ->
                connection.execute(
                    "XGROUP",
                    "CREATE".bytes(),
                    properties.notifications.stream.bytes(),
                    properties.notifications.group.bytes(),
                    "$".bytes(),
                    "MKSTREAM".bytes(),
                )
                null
            }
            log.info("Created Redis Stream consumer group {}", properties.notifications.group)
        } catch (exception: DataAccessException) {
            if (!exception.containsMessage("BUSYGROUP")) {
                throw exception
            }
            log.info("Redis Stream consumer group {} already exists", properties.notifications.group)
        }
    }

    private fun Throwable.containsMessage(fragment: String): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current.message?.contains(fragment, ignoreCase = true) == true) return true
            current = current.cause
        }
        return false
    }

    private fun String.bytes() = toByteArray(Charsets.UTF_8)
}

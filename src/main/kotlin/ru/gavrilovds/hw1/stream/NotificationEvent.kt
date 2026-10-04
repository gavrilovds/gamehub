package ru.gavrilovds.hw1.stream

import java.time.Instant

enum class NotificationType {
    LEVEL_CHANGED,
}

data class NotificationEvent(
    val playerId: String,
    val type: NotificationType,
    val message: String,
    val occurredAt: Instant,
) {
    fun toFields(): Map<String, String> = linkedMapOf(
        PLAYER_ID_FIELD to playerId,
        TYPE_FIELD to type.name,
        MESSAGE_FIELD to message,
        TIMESTAMP_FIELD to occurredAt.toString(),
    )

    companion object {
        private const val PLAYER_ID_FIELD = "player_id"
        private const val TYPE_FIELD = "type"
        private const val MESSAGE_FIELD = "message"
        private const val TIMESTAMP_FIELD = "timestamp"

        fun levelChanged(playerId: String, level: Int, occurredAt: Instant) = NotificationEvent(
            playerId = playerId,
            type = NotificationType.LEVEL_CHANGED,
            message = "Player $playerId reached level $level",
            occurredAt = occurredAt,
        )

        fun fromFields(fields: Map<String, String>) = NotificationEvent(
            playerId = fields.getValue(PLAYER_ID_FIELD),
            type = NotificationType.valueOf(fields.getValue(TYPE_FIELD)),
            message = fields.getValue(MESSAGE_FIELD),
            occurredAt = Instant.parse(fields.getValue(TIMESTAMP_FIELD)),
        )
    }
}

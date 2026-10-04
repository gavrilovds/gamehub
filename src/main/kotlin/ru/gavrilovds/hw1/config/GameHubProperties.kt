package ru.gavrilovds.hw1.config

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

@ConfigurationProperties("gamehub")
@Validated
data class GameHubProperties(
    @field:Valid val redis: Redis = Redis(),
    @field:Valid val notifications: Notifications = Notifications(),
    @field:Valid val cache: Cache = Cache(),
) {
    data class Redis(
        @field:NotBlank val master: String = "mymaster",
        @field:NotEmpty val sentinels: List<@NotBlank String> =
            listOf("localhost:26379", "localhost:26380", "localhost:26381"),
        val connectTimeout: Duration = Duration.ofSeconds(3),
        val commandTimeout: Duration = Duration.ofSeconds(3),
    ) {
        @get:AssertTrue(message = "Redis timeouts must be positive")
        val validTimeouts: Boolean
            get() = connectTimeout.isPositive() && commandTimeout.isPositive()
    }

    data class Notifications(
        @field:NotBlank val stream: String = "notifications",
        @field:NotBlank val group: String = "notifications-group",
        @field:NotBlank val consumer: String = "gamehub-consumer",
        val retention: Duration = Duration.ofDays(7),
    ) {
        @get:AssertTrue(message = "Notification retention must be positive")
        val validRetention: Boolean
            get() = retention.isPositive()
    }

    data class Cache(val playerTtl: Duration = Duration.ofSeconds(60)) {
        @get:AssertTrue(message = "Player cache TTL must be positive")
        val validPlayerTtl: Boolean
            get() = playerTtl.isPositive()
    }
}

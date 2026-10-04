package ru.gavrilovds.hw1.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import ru.gavrilovds.hw1.generated.model.AchievementCheckResponse
import ru.gavrilovds.hw1.generated.model.AchievementResponse
import ru.gavrilovds.hw1.generated.model.CommonAchievementsResponse
import ru.gavrilovds.hw1.redis.RedisKeys

@Service
class AchievementService(private val redis: StringRedisTemplate) {
    fun add(playerId: String, achievement: String): AchievementResponse {
        val added = redis.opsForSet().add(RedisKeys.achievements(playerId), achievement) == 1L
        return AchievementResponse(playerId, achievement, added)
    }

    fun contains(playerId: String, achievement: String) = AchievementCheckResponse(
        playerId,
        achievement,
        redis.opsForSet().isMember(RedisKeys.achievements(playerId), achievement) == true,
    )

    fun common(firstId: String, secondId: String) = CommonAchievementsResponse(
        firstId,
        secondId,
        redis.opsForSet().intersect(
            RedisKeys.achievements(firstId),
            RedisKeys.achievements(secondId),
        ).orEmpty(),
    )
}

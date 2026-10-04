package ru.gavrilovds.hw1.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import ru.gavrilovds.hw1.generated.model.LoginResponse
import ru.gavrilovds.hw1.redis.RedisKeys
import java.util.concurrent.TimeUnit

@Service
class LoginService(private val redis: StringRedisTemplate, private val loginCounterScript: DefaultRedisScript<Long>) {
    fun record(id: String): LoginResponse {
        val key = RedisKeys.logins(id)
        val count = redis.execute(loginCounterScript, listOf(key)) ?: 0
        val ttl = redis.getExpire(key, TimeUnit.SECONDS)
        return LoginResponse(id, count, ttl)
    }
}

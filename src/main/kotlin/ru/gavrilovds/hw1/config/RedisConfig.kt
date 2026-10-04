package ru.gavrilovds.hw1.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.ReadFrom
import io.lettuce.core.SocketOptions
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisNode
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

@Configuration
@EnableConfigurationProperties(GameHubProperties::class)
class RedisConfig {

    @Bean
    fun redisConnectionFactory(properties: GameHubProperties): LettuceConnectionFactory {
        val sentinel = RedisSentinelConfiguration().master(properties.redis.master)
        properties.redis.sentinels.forEach { address ->
            val (host, port) = parseAddress(address)
            sentinel.addSentinel(RedisNode(host, port))
        }

        val client = LettuceClientConfiguration.builder()
            .commandTimeout(properties.redis.commandTimeout)
            .shutdownTimeout(Duration.ofMillis(100))
            .clientOptions(
                ClientOptions.builder()
                    .socketOptions(
                        SocketOptions.builder()
                            .connectTimeout(properties.redis.connectTimeout)
                            .build(),
                    )
                    .build(),
            )
            .readFrom(ReadFrom.REPLICA_PREFERRED)
            .build()

        return LettuceConnectionFactory(sentinel, client).apply {
            validateConnection = true
        }
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory) = StringRedisTemplate(connectionFactory)

    @Bean
    fun loginCounterScript(): DefaultRedisScript<Long> = DefaultRedisScript<Long>().apply {
        setLocation(ClassPathResource("scripts/login-counter.lua"))
        resultType = Long::class.java
    }

    @Bean
    fun redisStartupProbe(connectionFactory: RedisConnectionFactory) = ApplicationRunner { _: ApplicationArguments ->
        val connection = connectionFactory.connection
        connection.use { connection ->
            check(connection.ping() == "PONG") { "Redis did not respond with PONG" }
        }
    }

    private fun parseAddress(value: String): Pair<String, Int> {
        val separator = value.lastIndexOf(':')
        require(separator > 0 && separator < value.lastIndex) {
            "Invalid Sentinel address '$value'; expected host:port"
        }
        return value.substring(0, separator) to value.substring(separator + 1).toInt()
    }
}

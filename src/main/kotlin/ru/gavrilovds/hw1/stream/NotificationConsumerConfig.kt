package ru.gavrilovds.hw1.stream

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.stream.StreamListener
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import ru.gavrilovds.hw1.config.GameHubProperties
import java.time.Duration

@Configuration
class NotificationConsumerConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("notificationStreamInitializer")
    fun notificationListenerContainer(
        connectionFactory: RedisConnectionFactory,
        redis: StringRedisTemplate,
        properties: GameHubProperties,
    ): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val serializer = RedisSerializer.string()
        val options: StreamMessageListenerContainer.StreamMessageListenerContainerOptions<
            String,
            MapRecord<String, String, String>,
            > = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
            .builder()
            .pollTimeout(Duration.ofSeconds(1))
            .serializer(serializer)
            .build()

        val container: StreamMessageListenerContainer<String, MapRecord<String, String, String>> =
            StreamMessageListenerContainer.create(connectionFactory, options)
        val stream = properties.notifications.stream
        val group = properties.notifications.group
        val consumer = Consumer.from(group, properties.notifications.consumer)

        val listener = StreamListener<String, MapRecord<String, String, String>> { message ->
            val event = NotificationEvent.fromFields(message.value)
            log.info(
                "Notification processed: id={}, playerId={}, type={}, message={}, occurredAt={}",
                message.id,
                event.playerId,
                event.type,
                event.message,
                event.occurredAt,
            )
            redis.opsForStream<String, String>().acknowledge(stream, group, message.id)
        }
        container.receive(
            consumer,
            StreamOffset.create(stream, ReadOffset.lastConsumed()),
            listener,
        )
        return container
    }
}

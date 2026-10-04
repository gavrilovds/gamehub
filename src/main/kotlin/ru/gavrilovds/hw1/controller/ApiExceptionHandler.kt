package ru.gavrilovds.hw1.controller

import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.gavrilovds.hw1.exception.PlayerNotFoundException
import ru.gavrilovds.hw1.generated.model.ErrorResponse
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(PlayerNotFoundException::class)
    fun notFound(exception: PlayerNotFoundException) =
        response(HttpStatus.NOT_FOUND, exception.message ?: "Resource not found")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun invalidBody(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return response(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(ConstraintViolationException::class, IllegalArgumentException::class)
    fun invalidRequest(exception: RuntimeException) =
        response(HttpStatus.BAD_REQUEST, exception.message ?: "Invalid request")

    @ExceptionHandler(DataAccessException::class)
    fun redisUnavailable(exception: DataAccessException) = response(
        HttpStatus.SERVICE_UNAVAILABLE,
        rootMessage(exception),
    )

    private fun response(status: HttpStatus, message: String) = ResponseEntity.status(status).body(
        ErrorResponse(status.value(), status.reasonPhrase, message, OffsetDateTime.now(ZoneOffset.UTC)),
    )

    private fun rootMessage(exception: Throwable): String {
        var current = exception
        while (current.cause != null) current = current.cause!!
        return current.message ?: "Redis is temporarily unavailable"
    }
}

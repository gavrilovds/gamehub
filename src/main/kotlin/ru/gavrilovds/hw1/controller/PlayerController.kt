package ru.gavrilovds.hw1.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.gavrilovds.hw1.generated.api.PlayersApi
import ru.gavrilovds.hw1.generated.model.AchievementCheckResponse
import ru.gavrilovds.hw1.generated.model.AchievementRequest
import ru.gavrilovds.hw1.generated.model.AchievementResponse
import ru.gavrilovds.hw1.generated.model.BatchPlayersRequest
import ru.gavrilovds.hw1.generated.model.BatchResponse
import ru.gavrilovds.hw1.generated.model.CommonAchievementsResponse
import ru.gavrilovds.hw1.generated.model.LevelUpdateRequest
import ru.gavrilovds.hw1.generated.model.LoginResponse
import ru.gavrilovds.hw1.generated.model.PlayerResponse
import ru.gavrilovds.hw1.generated.model.UpsertPlayerRequest
import ru.gavrilovds.hw1.service.AchievementService
import ru.gavrilovds.hw1.service.LoginService
import ru.gavrilovds.hw1.service.PlayerService

@RestController
class PlayerController(
    private val players: PlayerService,
    private val logins: LoginService,
    private val achievements: AchievementService,
) : PlayersApi {
    override fun batchPlayers(batchPlayersRequest: BatchPlayersRequest): ResponseEntity<BatchResponse> =
        ResponseEntity.ok(players.batch(batchPlayersRequest))

    override fun upsertPlayer(id: String, upsertPlayerRequest: UpsertPlayerRequest): ResponseEntity<PlayerResponse> =
        ResponseEntity.ok(players.upsert(id, upsertPlayerRequest))

    override fun getPlayer(id: String): ResponseEntity<PlayerResponse> {
        val result = players.get(id)
        return ResponseEntity.ok()
            .header("X-Cache", if (result.cacheHit) "HIT" else "MISS")
            .body(result.profile)
    }

    override fun updatePlayerLevel(
        id: String,
        levelUpdateRequest: LevelUpdateRequest,
    ): ResponseEntity<PlayerResponse> = ResponseEntity.ok(players.updateLevel(id, levelUpdateRequest.delta))

    override fun recordPlayerLogin(id: String): ResponseEntity<LoginResponse> = ResponseEntity.ok(logins.record(id))

    override fun addPlayerAchievement(
        id: String,
        achievementRequest: AchievementRequest,
    ): ResponseEntity<AchievementResponse> = ResponseEntity.ok(achievements.add(id, achievementRequest.achievementName))

    override fun hasPlayerAchievement(id: String, name: String): ResponseEntity<AchievementCheckResponse> =
        ResponseEntity.ok(achievements.contains(id, name))

    override fun getCommonAchievements(id1: String, id2: String): ResponseEntity<CommonAchievementsResponse> =
        ResponseEntity.ok(achievements.common(id1, id2))
}

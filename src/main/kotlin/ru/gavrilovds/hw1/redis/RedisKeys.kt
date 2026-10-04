package ru.gavrilovds.hw1.redis

object RedisKeys {
    const val LEADERBOARD = "tournament:main"

    fun player(id: String) = "player:$id"
    fun playerCache(id: String) = "cache:player:$id"
    fun logins(id: String) = "logins:$id"
    fun achievements(id: String) = "achievements:$id"
}

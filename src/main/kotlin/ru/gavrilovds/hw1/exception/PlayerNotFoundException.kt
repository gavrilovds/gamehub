package ru.gavrilovds.hw1.exception

class PlayerNotFoundException(id: String) : RuntimeException("Player '$id' was not found")

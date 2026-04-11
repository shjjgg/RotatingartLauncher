package com.app.ralaunch.core.di.service

import com.app.ralaunch.core.di.contract.GameListStorage
import com.app.ralaunch.core.model.GameItem
import com.app.ralaunch.core.model.GameList
import com.app.ralaunch.core.platform.AppConstants
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.random.Random

/**
 * Android 平台游戏列表存储实现。
 */
@OptIn(ExperimentalPathApi::class)
class AndroidGameListStorage(
    private val pathsProvider: StoragePathsProvider
) : GameListStorage {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun loadGameList(): List<GameItem> {
        return try {
            if (!gameListPathFull.exists()) return emptyList()

            val gameList = json.decodeFromString<GameList>(gameListPathFull.readText())
            gameList.games.mapNotNull { storageRootPathRelative ->
                loadGameInfo(storageRootPathRelative)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override fun saveGameList(games: List<GameItem>) {
        try {
            gamesDirPathFull.createDirectories()

            val gameList = GameList(games = games.map { it.id })
            gameListPathFull.writeText(json.encodeToString(gameList))

            games.forEach { saveGameInfo(it) }
            cleanupDeletedGameStorageRoots(games.map { it.id })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getGameGlobalStorageDirFull(): String = gamesDirPathFull.toString()

    override fun createGameStorageRoot(gameId: String): Pair<String, String> {
        val baseName = gameId.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "_")
        val storageRootPathRelative = "${baseName}_${randomHex(8)}"
        val storageRootPathFull = gamesDirPathFull.resolve(storageRootPathRelative)
        storageRootPathFull.createDirectories()
        return Pair(storageRootPathFull.toString(), storageRootPathRelative)
    }

    private fun loadGameInfo(storageRootPathRelative: String): GameItem? {
        return try {
            val gameInfoPathFull = gamesDirPathFull
                .resolve(storageRootPathRelative)
                .resolve(AppConstants.Files.GAME_INFO)
            if (!gameInfoPathFull.exists()) return null

            val content = gameInfoPathFull.readText()
            json.decodeFromString<GameItem>(content).also {
                it.gameListStorageParent = this
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveGameInfo(game: GameItem) {
        try {
            val storageRootPathRelative = game.id
            val storageRootPathFull = gamesDirPathFull.resolve(storageRootPathRelative)
            val gameInfoPathFull = storageRootPathFull.resolve(AppConstants.Files.GAME_INFO)

            storageRootPathFull.createDirectories()
            gameInfoPathFull.writeText(json.encodeToString(game))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanupDeletedGameStorageRoots(keepRoots: List<String>) {
        if (!gamesDirPathFull.exists()) return

        val keepStorageRootsRelative = keepRoots.toSet()
        gamesDirPathFull.listDirectoryEntries()
            .filter { it.isDirectory() }
            .filter { it.name !in keepStorageRootsRelative }
            .forEach {
                runCatching { it.deleteRecursively() }
            }
    }

    private fun randomHex(length: Int): String {
        val chars = "0123456789abcdef"
        return buildString(length) {
            repeat(length) {
                append(chars[Random.nextInt(chars.length)])
            }
        }
    }

    private val gamesDirPathFull
        get() = Path(pathsProvider.gamesDirPathFull())

    private val gameListPathFull
        get() = gamesDirPathFull.resolve(AppConstants.Files.GAME_LIST)
}

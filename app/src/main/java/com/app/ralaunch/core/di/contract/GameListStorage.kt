package com.app.ralaunch.core.di.contract

import com.app.ralaunch.core.model.GameItem

/**
 * 游戏列表存储接口
 * 由各平台实现
 */
interface GameListStorage {
    fun loadGameList(): List<GameItem>
    fun saveGameList(games: List<GameItem>)
    fun getGameGlobalStorageDirFull(): String

    /**
     * 创建游戏安装目录
     * @param gameId 游戏名称（用于生成存储ID）
     * @return Pair(目录绝对路径, 存储ID)
     */
    fun createGameStorageRoot(gameId: String): Pair<String, String>
}

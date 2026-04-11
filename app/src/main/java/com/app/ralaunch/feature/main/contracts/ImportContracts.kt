package com.app.ralaunch.feature.main.contracts

data class ImportUiState(
    val isImporting: Boolean = false,
    val progress: Int = 0,
    val status: String = "",
    val errorMessage: String? = null,
    val lastCompletedGameId: String? = null
)

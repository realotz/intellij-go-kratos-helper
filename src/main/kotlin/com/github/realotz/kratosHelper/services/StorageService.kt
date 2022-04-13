package com.github.realotz.kratosHelper.services

import com.github.realotz.kratosHelper.state.ConfigState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.project.Project

@State(
        name = "StorageService",
)
class StorageService(project: Project) : PersistentStateComponent<ConfigState> {
    private var storage = ConfigState()

    override fun getState(): ConfigState = storage

    override fun loadState(newStorage: ConfigState) {
        storage = newStorage
    }
}
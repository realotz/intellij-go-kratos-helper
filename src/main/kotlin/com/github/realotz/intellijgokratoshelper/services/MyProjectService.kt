package com.github.realotz.intellijgokratoshelper.services

import com.intellij.openapi.project.Project
import com.github.realotz.intellijgokratoshelper.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}

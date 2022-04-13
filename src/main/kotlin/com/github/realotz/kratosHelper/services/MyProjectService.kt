package com.github.realotz.kratosHelper.services

import com.intellij.openapi.project.Project
import com.github.realotz.kratosHelper.MyBundle

class MyProjectService(project: Project) {
    init {
        println(MyBundle.message("projectService", project.name))
    }
}

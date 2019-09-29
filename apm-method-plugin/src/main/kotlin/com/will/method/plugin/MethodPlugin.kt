package com.will.method.plugin

import com.android.build.gradle.AppExtension
import com.will.base.utils.Log

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.Collections

class MethodPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        Log.d("", "**********************************")
        Log.d("", "*******                    *******")
        Log.d("", "*******       plugin       *******")
        Log.d("", "*******                    *******")
        Log.d("", "**********************************")
        val appExtension = project.properties["android"] as AppExtension
        appExtension.registerTransform(MethodTransform(project), Collections.EMPTY_LIST)
    }
}
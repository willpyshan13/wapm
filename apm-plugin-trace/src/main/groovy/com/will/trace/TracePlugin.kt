package com.will.trace

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Desc:trace plugin
 *
 * Date: 2019-09-26 17:13
 * Copyright: Copyright (c) 2018-2019
 * Company:
 * Updater:
 * Update Time:
 * Update Comments:
 * @author: pengysh
 */
class TracePlugin : Plugin<Project> {
    override fun apply(p0: Project) {
        when{
            p0.project.hasProperty("com.android.application")->{

            }
        }
    }

}
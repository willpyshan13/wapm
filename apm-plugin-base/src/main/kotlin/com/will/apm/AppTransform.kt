package com.will.apm

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager

/**
 * Desc:
 *
 * Date: 2019-09-27 14:15
 * Copyright: Copyright (c) 2018-2019
 * Company:
 * Updater:
 * Update Time:
 * Update Comments:
 * @author: pengysh
 */
class AppTransform : BaseTransform(){
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =TransformManager.SCOPE_FULL_PROJECT
}
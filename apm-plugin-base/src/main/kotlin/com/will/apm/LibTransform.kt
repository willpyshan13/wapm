package com.will.apm

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager

/**
 * Desc:
 *
 * Date: 2019-09-27 14:16
 * Copyright: Copyright (c) 2018-2019
 * Company:
 * Updater:
 * Update Time:
 * Update Comments:
 * @author: pengysh
 */
class LibTransform : BaseTransform(){
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =TransformManager.PROJECT_ONLY

}
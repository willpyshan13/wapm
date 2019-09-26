package com.will.trace.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject
import java.util.concurrent.TimeUnit

/**
 * desc
 * @author will
 * Created time 2019-09-26 23:11.
 */

abstract class BaseTransform :Transform(){
    override fun getName() = "will"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation?.let {

        }
    }



}
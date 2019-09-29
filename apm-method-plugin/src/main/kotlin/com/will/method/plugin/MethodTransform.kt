package com.will.method.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.will.base.ApmTransform
import com.will.base.RunVariant
import com.will.method.plugin.bytecode.DebugWeaver

import org.gradle.api.Project

import java.io.IOException

class MethodTransform(private val project: Project) : ApmTransform(project) {
//    private var debugHunterExtension: MethodExtension? = null

    override val runVariant: RunVariant
        get() = RunVariant.ALWAYS

    init {
//        project.extensions.create("methodExt", MethodExtension::class.java)
        this.bytecodeWeaver = DebugWeaver()
    }

    @Throws(IOException::class, TransformException::class, InterruptedException::class)
    override fun transform(
        context: Context,
        inputs: Collection<TransformInput>?,
        referencedInputs: Collection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
//        debugHunterExtension = project.extensions.getByName("methodExt")
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
    }

//    override fun inDuplcatedClassSafeMode(): Boolean {
//        return debugHunterExtension!!.duplcatedClassSafeMode
//    }
}
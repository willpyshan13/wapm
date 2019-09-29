package com.will.base

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.google.common.io.Files
import com.will.base.asm.BaseWeaver
import com.will.base.asm.ClassLoaderHelper

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.HashSet

open class ApmTransform(private val project: Project) : Transform() {

    private val logger: Logger
    protected var bytecodeWeaver: BaseWeaver? = null
    private val waitableExecutor: WaitableExecutor
    private var emptyRun = false

    protected open val runVariant: RunVariant
        get() = RunVariant.ALWAYS

    init {
        this.logger = project.logger
        this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
    }

    override fun getName(): String {
        return this.javaClass.simpleName
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return SCOPES
    }

    override fun isIncremental(): Boolean {
        return true
    }


    @Throws(IOException::class, TransformException::class, InterruptedException::class)
    override fun transform(
        context: Context,
        inputs: Collection<TransformInput>?,
        referencedInputs: Collection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        val runVariant = runVariant
        if ("debug" == context.variantName) {
            emptyRun = runVariant === RunVariant.RELEASE || runVariant === RunVariant.NEVER
        } else if ("release" == context.variantName) {
            emptyRun = runVariant === RunVariant.DEBUG || runVariant === RunVariant.NEVER
        }
        logger.warn(
            name + " isIncremental = " + isIncremental + ", runVariant = "
                    + runVariant + ", emptyRun = " + emptyRun + ", inDuplcatedClassSafeMode = " + inDuplcatedClassSafeMode()
        )
        val startTime = System.currentTimeMillis()
        if (!isIncremental) {
            outputProvider!!.deleteAll()
        }
        val urlClassLoader = ClassLoaderHelper.getClassLoader(inputs, referencedInputs, project)
        this.bytecodeWeaver!!.classLoader =urlClassLoader
        var flagForCleanDexBuilderFolder = false
        for (input in inputs!!) {
            for (jarInput in input.jarInputs) {
                val status = jarInput.status
                val dest = outputProvider!!.getContentLocation(
                    jarInput.file.absolutePath,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                if (isIncremental && !emptyRun) {
                    when (status) {
                        Status.NOTCHANGED -> {
                        }
                        Status.ADDED, Status.CHANGED -> transformJar(jarInput.file, dest, status)
                        Status.REMOVED -> if (dest.exists()) {
                            FileUtils.forceDelete(dest)
                        }
                    }
                } else {
                    //Forgive me!, Some project will store 3rd-party aar for serveral copies in dexbuilder folder,,unknown issue.
                    if (inDuplcatedClassSafeMode() and !isIncremental && !flagForCleanDexBuilderFolder) {
                        cleanDexBuilderFolder(dest)
                        flagForCleanDexBuilderFolder = true
                    }
                    transformJar(jarInput.file, dest, status)
                }
            }

            for (directoryInput in input.directoryInputs) {
                val dest = outputProvider!!.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes, directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.forceMkdir(dest)
                if (isIncremental && !emptyRun) {
                    val srcDirPath = directoryInput.file.absolutePath
                    val destDirPath = dest.absolutePath
                    val fileStatusMap = directoryInput.changedFiles
                    for ((inputFile, status) in fileStatusMap) {
                        val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
                        val destFile = File(destFilePath)
                        when (status) {
                            Status.NOTCHANGED -> {
                            }
                            Status.REMOVED -> if (destFile.exists()) {

                                destFile.delete()
                            }
                            Status.ADDED, Status.CHANGED -> {
                                try {
                                    FileUtils.touch(destFile)
                                } catch (e: IOException) {
                                    //maybe mkdirs fail for some strange reason, try again.
                                    Files.createParentDirs(destFile)
                                }

                                transformSingleFile(inputFile, destFile, srcDirPath)
                            }
                        }
                    }
                } else {
                    transformDir(directoryInput.file, dest)
                }

            }

        }

        waitableExecutor.waitForTasksWithQuickFail<Any>(true)
        val costTime = System.currentTimeMillis() - startTime
        logger.warn(name + " costed " + costTime + "ms")
    }

    private fun transformSingleFile(inputFile: File, outputFile: File, srcBaseDir: String) {
        waitableExecutor.execute<Any> {
            bytecodeWeaver!!.weaveSingleClassToFile(inputFile, outputFile, srcBaseDir)
            null
        }
    }

    @Throws(IOException::class)
    private fun transformDir(inputDir: File, outputDir: File) {
        if (emptyRun) {
            FileUtils.copyDirectory(inputDir, outputDir)
            return
        }
        val inputDirPath = inputDir.absolutePath
        val outputDirPath = outputDir.absolutePath
        if (inputDir.isDirectory) {
            for (file in com.android.utils.FileUtils.getAllFiles(inputDir)) {
                waitableExecutor.execute<Any> {
                    val filePath = file.absolutePath
                    val outputFile = File(filePath.replace(inputDirPath, outputDirPath))
                    bytecodeWeaver!!.weaveSingleClassToFile(file, outputFile, inputDirPath)
                    null
                }
            }
        }
    }

    private fun transformJar(srcJar: File, destJar: File, status: Status) {
        waitableExecutor.execute<Any> waitableExecutor@{
            if (emptyRun) {
                FileUtils.copyFile(srcJar, destJar)
                return@waitableExecutor null
            }
            bytecodeWeaver!!.weaveJar(srcJar, destJar)
            null
        }
    }

    private fun cleanDexBuilderFolder(dest: File) {
        waitableExecutor.execute<Any> {
            try {
                val dexBuilderDir = replaceLastPart(dest.absolutePath, name, "dexBuilder")
                //intermediates/transforms/dexBuilder/debug
                val file = File(dexBuilderDir).parentFile
                project.logger.warn("clean dexBuilder folder = " + file.absolutePath)
                if (file.exists() && file.isDirectory) {
                    com.android.utils.FileUtils.deleteDirectoryContents(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            null
        }
    }

    private fun replaceLastPart(
        originString: String,
        replacement: String,
        toreplace: String
    ): String {
        val start = originString.lastIndexOf(replacement)
        val builder = StringBuilder()
        builder.append(originString.substring(0, start))
        builder.append(toreplace)
        builder.append(originString.substring(start + replacement.length))
        return builder.toString()
    }

    override fun isCacheable(): Boolean {
        return true
    }

    protected open fun inDuplcatedClassSafeMode(): Boolean {
        return false
    }

    companion object {

        private val SCOPES = HashSet<QualifiedContent.Scope>()

        init {
            SCOPES.add(QualifiedContent.Scope.PROJECT)
            SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS)
            SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
        }
    }
}

package com.will.method.plugin.bytecode

import com.will.base.asm.BaseWeaver
import com.will.base.asm.ExtendClassWriter
import com.will.method.plugin.bytecode.prego.DebugPreGoClassAdapter

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

import java.io.IOException
import java.io.InputStream

class DebugWeaver : BaseWeaver() {

    override fun setExtension(extension: Any) {}

    @Throws(IOException::class)
    override fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        var classWriter: ClassWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
        val debugPreGoClassAdapter =
            DebugPreGoClassAdapter(classWriter)
        classReader.accept(debugPreGoClassAdapter, ClassReader.EXPAND_FRAMES)
        //if need parameter
        if (debugPreGoClassAdapter.isNeedParameter) {
            classWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
            val debugClassAdapter =
                DebugClassAdapter(
                    classWriter,
                    debugPreGoClassAdapter.getMethodParametersMap()
                )
            classReader.accept(debugClassAdapter, ClassReader.EXPAND_FRAMES)
        }
        return classWriter.toByteArray()
    }

    override fun isWeavableClass(fullQualifiedClassName: String): Boolean {
        val superResult = super.isWeavableClass(fullQualifiedClassName)
        val isByteCodePlugin = fullQualifiedClassName.startsWith(PLUGIN_LIBRARY)
        return superResult && !isByteCodePlugin
    }

    companion object {

        private val PLUGIN_LIBRARY = "com.will.library"
    }


}

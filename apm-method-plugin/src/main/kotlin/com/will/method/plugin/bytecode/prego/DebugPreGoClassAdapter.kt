package com.will.method.plugin.bytecode.prego

import com.will.method.plugin.bytecode.Parameter

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import java.util.HashMap

class DebugPreGoClassAdapter(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM5, cv) {

    private val methodParametersMap = HashMap<String, List<Parameter>>()
    private var debugPreGoMethodAdapter: DebugPreGoMethodAdapter? = null
    var isNeedParameter = false
        private set

    override fun visitMethod(
        access: Int, name: String,
        desc: String, signature: String, exceptions: Array<String>
    ): MethodVisitor? {
        val mv = cv.visitMethod(access, name, desc, signature, exceptions)
        if (debugPreGoMethodAdapter != null && debugPreGoMethodAdapter!!.needParameter) {
            isNeedParameter = true
        }
        val methodUniqueKey = name + desc
        debugPreGoMethodAdapter =
            DebugPreGoMethodAdapter(
                methodUniqueKey,
                methodParametersMap,
                mv!!
            )
        return if (mv == null) null else debugPreGoMethodAdapter
    }

    fun getMethodParametersMap(): Map<String, List<Parameter>> {
        return this.methodParametersMap
    }

    override fun visitEnd() {
        super.visitEnd()
        if (debugPreGoMethodAdapter != null && debugPreGoMethodAdapter!!.needParameter) {
            isNeedParameter = true
        }
    }
}
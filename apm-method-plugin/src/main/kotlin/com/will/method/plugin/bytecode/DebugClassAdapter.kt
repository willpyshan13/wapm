package com.will.method.plugin.bytecode

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class DebugClassAdapter internal constructor(
    cv: ClassVisitor,
    private val methodParametersMap: Map<String, List<Parameter>>
) : ClassVisitor(Opcodes.ASM7, cv) {
    private var debugMethodAdapter: DebugMethodAdapter? = null
    private var className: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
    }

    override fun visitMethod(
        access: Int, name: String?,
        desc: String?, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor? {
        val mv = cv.visitMethod(access, name, desc, signature, exceptions)
        val methodUniqueKey = name + desc
        debugMethodAdapter = methodParametersMap[methodUniqueKey]?.let {
            DebugMethodAdapter(
                className!!,
                it,
                name!!,
                access,
                desc!!,
                mv
            )
        }
        return if (mv == null) null else debugMethodAdapter
    }

}
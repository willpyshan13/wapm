package com.will.method.plugin.bytecode.prego

import com.will.method.plugin.bytecode.Parameter

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

import java.util.ArrayList

class DebugPreGoMethodAdapter(
    private val methodKey: String,
    private val methodParametersMap: MutableMap<String, List<Parameter>>,
    mv: MethodVisitor
) : MethodVisitor(Opcodes.ASM7, mv), Opcodes {
    private val DEFAULT_ANNOTATION = "Lcom/will/library/MethodDebug;"
    private val DEFAULT_ANNOTATION_DEBUG = "Lcom/will/library/MethodDebugImpl;"
    private val parameters = ArrayList<Parameter>()
    var needParameter = false
        private set
    private val labelList = ArrayList<Label>()

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        val defaultAv = super.visitAnnotation(desc, visible)
        if (DEFAULT_ANNOTATION == desc || DEFAULT_ANNOTATION_DEBUG == desc) {
            needParameter = true
        }
        return defaultAv
    }

    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        if ("this" != name && start === labelList[0] && needParameter) {
            val type = Type.getType(descriptor)
            if (type.sort == Type.OBJECT || type.sort == Type.ARRAY) {
                parameters.add(
                    Parameter(
                        name,
                        "Ljava/lang/Object;",
                        index
                    )
                )
            } else {
                parameters.add(Parameter(name, descriptor, index))
            }
        }
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    override fun visitEnd() {
        methodParametersMap[methodKey] = parameters
        super.visitEnd()
    }

    override fun visitLabel(label: Label) {
        labelList.add(label)
        super.visitLabel(label)
    }

}

package com.will.method.plugin.bytecode

import com.will.base.utils.Log

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

class DebugMethodAdapter(
    className: String,
    private val parameters: List<Parameter>,
    private val methodName: String,
    access: Int,
    private val methodDesc: String,
    mv: MethodVisitor
) : LocalVariablesSorter(Opcodes.ASM7, access, methodDesc, mv), Opcodes {
    private val DEFAULT_ANNOTATION = "Lcom/will/library/MethodDebug;"
    private val DEFAULT_ANNOTATION_DEBUG = "Lcom/will/library/MethodDebugImpl;"
    private val DEFAULT_PARAMETER = "com/will/library/ParameterPrinter"
    private val DEFAULT_PARAMETER_DESC = "(Ljava/lang/String;%s)Lcom/will/library/ParameterPrinter;"
    private val DEFAULT_RESULT = "com/will/library/ResultPrinter"

    private val DEFAULT_RESULT_KT = "com/will/library/KtResultPrinter"
    private var className: String? = null
    private var debugMethod = false
    private var debugMethodWithCustomLogger = false
    private var timingStartVarIndex: Int = 0


    init {
        Log.d("debug visitAnnotation", "DebugMethodAdapter")
        if (!className.endsWith("/")) {
            this.className = className.substring(className.lastIndexOf("/") + 1)
        } else {
            this.className = className
        }
    }

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
        val defaultAv = super.visitAnnotation(desc, visible)
        Log.d("debug visitAnnotation", desc)
        if (DEFAULT_ANNOTATION == desc) {
            debugMethod = true
        } else if (DEFAULT_ANNOTATION_DEBUG == desc) {
            debugMethodWithCustomLogger = true
        }
        return defaultAv
    }

    override fun visitCode() {
        super.visitCode()
        Log.d("debug visitCode", "visitCode")
        if (!debugMethod && !debugMethodWithCustomLogger) return
        val printUtilsVarIndex = newLocal(Type.getObjectType(DEFAULT_PARAMETER))
        mv.visitTypeInsn(Opcodes.NEW, DEFAULT_PARAMETER)
        mv.visitInsn(Opcodes.DUP)
        mv.visitLdcInsn(className)
        mv.visitLdcInsn(methodName)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            DEFAULT_PARAMETER,
            "<init>",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            false
        )
        mv.visitVarInsn(Opcodes.ASTORE, printUtilsVarIndex)
        for (i in parameters.indices) {
            val parameter = parameters[i]
            val name = parameter.name
            val desc = parameter.desc
            val index = parameter.index
            val opcode = Utils.getLoadOpcodeFromDesc(desc)
            val fullyDesc = String.format(DEFAULT_PARAMETER_DESC, desc)
            visitPrint(printUtilsVarIndex, index, opcode, name, fullyDesc)
        }
        mv.visitVarInsn(Opcodes.ALOAD, printUtilsVarIndex)
        if (debugMethod) {
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, DEFAULT_PARAMETER, "print", "()V", false)
        } else if (debugMethodWithCustomLogger) {
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                DEFAULT_PARAMETER,
                "printWithCustomLogger",
                "()V",
                false
            )
        }
        //Timing
        timingStartVarIndex = newLocal(Type.LONG_TYPE)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/System",
            "currentTimeMillis",
            "()J",
            false
        )
        mv.visitVarInsn(Opcodes.LSTORE, timingStartVarIndex)
    }

    private fun visitPrint(
        varIndex: Int,
        localIndex: Int,
        opcode: Int,
        name: String?,
        desc: String
    ) {
        Log.d("debug visitPrint", "visitPrint")
        mv.visitVarInsn(Opcodes.ALOAD, varIndex)
        mv.visitLdcInsn(name)
        mv.visitVarInsn(opcode, localIndex)
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            DEFAULT_PARAMETER,
            "append",
            desc, false
        )
        mv.visitInsn(Opcodes.POP)
    }

    override fun visitInsn(opcode: Int) {
        if ((debugMethod || debugMethodWithCustomLogger) && (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN || opcode == Opcodes.ATHROW)) {
            val returnType = Type.getReturnType(methodDesc)
            var returnDesc = methodDesc.substring(methodDesc.indexOf(")") + 1)
            if (returnDesc.startsWith("[") || returnDesc.startsWith("L")) {
                returnDesc =
                    "Ljava/lang/Object;" //regard object extended from Object or array as object
            }
            //store origin return value
            var resultTempValIndex = -1
            if (returnType !== Type.VOID_TYPE || opcode == Opcodes.ATHROW) {
                resultTempValIndex = newLocal(returnType)
                var storeOpcocde =
                    Utils.getStoreOpcodeFromType(returnType)
                if (opcode == Opcodes.ATHROW) storeOpcocde = Opcodes.ASTORE
                mv.visitVarInsn(storeOpcocde, resultTempValIndex)
            }
            //parameter1 parameter2
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/System",
                "currentTimeMillis",
                "()J",
                false
            )
            mv.visitVarInsn(Opcodes.LLOAD, timingStartVarIndex)
            mv.visitInsn(Opcodes.LSUB)
            val index = newLocal(Type.LONG_TYPE)
            mv.visitVarInsn(Opcodes.LSTORE, index)
            mv.visitLdcInsn(className)    //parameter 1 string
            mv.visitLdcInsn(methodName)   //parameter 2 string
            mv.visitVarInsn(Opcodes.LLOAD, index) //parameter 3 long
            //parameter 4
            if (returnType !== Type.VOID_TYPE || opcode == Opcodes.ATHROW) {
                var loadOpcode =
                    Utils.getLoadOpcodeFromType(returnType)
                if (opcode == Opcodes.ATHROW) {
                    loadOpcode = Opcodes.ALOAD
                    returnDesc = "Ljava/lang/Object;"
                }
                mv.visitVarInsn(loadOpcode, resultTempValIndex)
                val formatDesc =
                    String.format("(Ljava/lang/String;Ljava/lang/String;J%s)V", returnDesc)
                Log.d("debug visitInsn", "VOID_TYPE   $debugMethod  $formatDesc")
                if (debugMethod) {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        DEFAULT_RESULT,
                        "print",
                        formatDesc,
                        false
                    )
                } else {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        DEFAULT_RESULT,
                        "printWithCustomLogger",
                        formatDesc,
                        false
                    )
                }
                mv.visitVarInsn(loadOpcode, resultTempValIndex)
            } else {
                mv.visitLdcInsn("void")
                if (debugMethod) {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        DEFAULT_RESULT,
                        "print",
                        "(Ljava/lang/String;Ljava/lang/String;JLjava/lang/Object;)V",
                        false
                    )
                } else {
                    mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        DEFAULT_RESULT,
                        "printWithCustomLogger",
                        "(Ljava/lang/String;Ljava/lang/String;JLjava/lang/Object;)V",
                        false
                    )
                }
            }
        }
        super.visitInsn(opcode)
    }

}

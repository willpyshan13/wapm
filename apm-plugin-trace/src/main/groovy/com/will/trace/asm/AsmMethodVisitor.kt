package com.will.trace.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * Desc:
 *
 * Date: 2019-09-27 16:19
 * Copyright: Copyright (c) 2018-2019
 * Company:
 * Updater:
 * Update Time:
 * Update Comments:
 * @author: pengysh
 */
class AsmMethodVisitor(methodVisitor: MethodVisitor,access:Int,name:String,desc:String) :AdviceAdapter(Opcodes.ASM7,methodVisitor,access,name,desc){
    override fun onMethodEnter() {
        super.onMethodEnter()
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
    }

    override fun visitInsn(opcode: Int) {
        super.visitInsn(opcode)
    }
}
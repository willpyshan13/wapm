package com.will.method.plugin.bytecode;

import com.will.base.utils.Log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.List;

public final class DebugMethodAdapter extends LocalVariablesSorter implements Opcodes {
    private String DEFAULT_ANNOTATION = "Lcom/will/library/MethodDebug;";
    private String DEFAULT_PARAMETER = "com/will/library/ParameterPrinter";
    private String DEFAULT_PARAMETER_DESC = "(Ljava/lang/String;%s)Lcom/will/library/ParameterPrinter;";
    private String DEFAULT_RESULT = "com/will/library/ResultPrinter";

    private String DEFAULT_RESULT_KT = "com/will/library/KtResultPrinter";
    private List<Parameter> parameters;
    private String className;
    private String methodName;
    private boolean debugMethod = false;
    private boolean debugMethodWithCustomLogger = false;
    private int timingStartVarIndex;
    private String methodDesc;


    public DebugMethodAdapter(String className, List<Parameter> parameters, String name, int access, String desc, MethodVisitor mv) {
        super(Opcodes.ASM7, access, desc, mv);
        Log.d("debug visitAnnotation", "DebugMethodAdapter");
        if (!className.endsWith("/")) {
            this.className = className.substring(className.lastIndexOf("/") + 1);
        } else {
            this.className = className;
        }
        this.parameters = parameters;
        this.methodName = name;
        this.methodDesc = desc;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor defaultAv = super.visitAnnotation(desc, visible);
        Log.d("debug visitAnnotation", desc);
        if (DEFAULT_ANNOTATION.equals(desc)) {
            debugMethod = true;
        } else if ("Lcom/hunter/library/debug/HunterDebugImpl;".equals(desc)) {
            debugMethodWithCustomLogger = true;
        }
        return defaultAv;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        Log.d("debug visitCode", "visitCode");
        if (!debugMethod && !debugMethodWithCustomLogger) return;
        int printUtilsVarIndex = newLocal(Type.getObjectType(DEFAULT_PARAMETER));
        mv.visitTypeInsn(NEW, DEFAULT_PARAMETER);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(INVOKESPECIAL, DEFAULT_PARAMETER, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        mv.visitVarInsn(ASTORE, printUtilsVarIndex);
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            String name = parameter.name;
            String desc = parameter.desc;
            int index = parameter.index;
            int opcode = Utils.getLoadOpcodeFromDesc(desc);
            String fullyDesc = String.format(DEFAULT_PARAMETER_DESC, desc);
            visitPrint(printUtilsVarIndex, index, opcode, name, fullyDesc);
        }
        mv.visitVarInsn(ALOAD, printUtilsVarIndex);
        if (debugMethod) {
            mv.visitMethodInsn(INVOKEVIRTUAL, DEFAULT_PARAMETER, "print", "()V", false);
        } else if (debugMethodWithCustomLogger) {
            mv.visitMethodInsn(INVOKEVIRTUAL, DEFAULT_PARAMETER, "printWithCustomLogger", "()V", false);
        }
        //Timing
        timingStartVarIndex = newLocal(Type.LONG_TYPE);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(Opcodes.LSTORE, timingStartVarIndex);
    }

    private void visitPrint(int varIndex, int localIndex, int opcode, String name, String desc) {
        Log.d("debug visitPrint", "visitPrint");
        mv.visitVarInsn(ALOAD, varIndex);
        mv.visitLdcInsn(name);
        mv.visitVarInsn(opcode, localIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL,
                DEFAULT_PARAMETER,
                "append",
                desc, false);
        mv.visitInsn(POP);
    }

    @Override
    public void visitInsn(int opcode) {
        if ((debugMethod || debugMethodWithCustomLogger) && ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW)) {
            Type returnType = Type.getReturnType(methodDesc);
            String returnDesc = methodDesc.substring(methodDesc.indexOf(")") + 1);
            if (returnDesc.startsWith("[") || returnDesc.startsWith("L")) {
                returnDesc = "Ljava/lang/Object;"; //regard object extended from Object or array as object
            }
            //store origin return value
            int resultTempValIndex = -1;
            if (returnType != Type.VOID_TYPE || opcode == ATHROW) {
                resultTempValIndex = newLocal(returnType);
                int storeOpcocde = Utils.getStoreOpcodeFromType(returnType);
                if (opcode == ATHROW) storeOpcocde = ASTORE;
                mv.visitVarInsn(storeOpcocde, resultTempValIndex);
            }
            //parameter1 parameter2
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, timingStartVarIndex);
            mv.visitInsn(LSUB);
            int index = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, index);
            mv.visitLdcInsn(className);    //parameter 1 string
            mv.visitLdcInsn(methodName);   //parameter 2 string
            mv.visitVarInsn(LLOAD, index); //parameter 3 long
            //parameter 4
            if (returnType != Type.VOID_TYPE || opcode == ATHROW) {
                int loadOpcode = Utils.getLoadOpcodeFromType(returnType);
                if (opcode == ATHROW) {
                    loadOpcode = ALOAD;
                    returnDesc = "Ljava/lang/Object;";
                }
                mv.visitVarInsn(loadOpcode, resultTempValIndex);
                String formatDesc = String.format("(Ljava/lang/String;Ljava/lang/String;J%s)V", returnDesc);
                Log.d("debug visitInsn", "VOID_TYPE   " + debugMethod + "  " + formatDesc);
                if (debugMethod) {
                    mv.visitMethodInsn(INVOKESTATIC, DEFAULT_RESULT, "print", formatDesc, false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, DEFAULT_RESULT, "printWithCustomLogger", formatDesc, false);
                }
                mv.visitVarInsn(loadOpcode, resultTempValIndex);
            } else {
                mv.visitLdcInsn("void");
                if (debugMethod) {
                    mv.visitMethodInsn(INVOKESTATIC, DEFAULT_RESULT, "print", "(Ljava/lang/String;Ljava/lang/String;JLjava/lang/Object;)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, DEFAULT_RESULT, "printWithCustomLogger", "(Ljava/lang/String;Ljava/lang/String;JLjava/lang/Object;)V", false);
                }
            }
        }
        super.visitInsn(opcode);
    }

}

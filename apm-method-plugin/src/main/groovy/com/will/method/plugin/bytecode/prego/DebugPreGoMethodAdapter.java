package com.will.method.plugin.bytecode.prego;

import com.will.base.utils.Log;
import com.will.method.plugin.bytecode.Parameter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DebugPreGoMethodAdapter extends MethodVisitor implements Opcodes {
    private String DEFAULT_ANNOTATION = "Lcom/will/library/MethodDebug;";
    private Map<String, List<Parameter>> methodParametersMap;
    private List<Parameter> parameters = new ArrayList<>();
    private String methodKey;
    private boolean needParameter = false;
    private List<Label> labelList = new ArrayList<>();

    public DebugPreGoMethodAdapter(String methodKey, Map<String, List<Parameter>> methodParametersMap, MethodVisitor mv) {
        super(Opcodes.ASM7, mv);
        this.methodKey = methodKey;
        this.methodParametersMap = methodParametersMap;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor defaultAv = super.visitAnnotation(desc, visible);
        if(DEFAULT_ANNOTATION.equals(desc) || "Lcom/hunter/library/debug/HunterDebugImpl;".equals(desc)) {
            needParameter = true;
        }
        return defaultAv;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if(!"this".equals(name) && start == labelList.get(0) && needParameter) {
            Type type = Type.getType(desc);
            if(type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
                parameters.add(new Parameter(name, "Ljava/lang/Object;", index));
            } else {
                parameters.add(new Parameter(name, desc, index));
            }
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitEnd() {
        methodParametersMap.put(methodKey, parameters);
        super.visitEnd();
    }

    @Override
    public void visitLabel(Label label) {
        labelList.add(label);
        super.visitLabel(label);
    }

    public boolean getNeedParameter() {
        return needParameter;
    }

}

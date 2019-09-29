package com.will.method.plugin.bytecode;

import com.android.build.gradle.internal.LoggerWrapper;
import com.will.base.asm.BaseWeaver;
import com.will.base.asm.ExtendClassWriter;
import com.will.base.utils.Log;
import com.will.method.plugin.bytecode.prego.DebugPreGoClassAdapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

public final class DebugWeaver extends BaseWeaver {

    private static final String PLUGIN_LIBRARY = "com.will.library";

    @Override
    public void setExtension(Object extension) {
    }

    @Override
    public byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassWriter classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS);
        DebugPreGoClassAdapter debugPreGoClassAdapter = new DebugPreGoClassAdapter(classWriter);
        classReader.accept(debugPreGoClassAdapter, ClassReader.EXPAND_FRAMES);
        //if need parameter
        if(debugPreGoClassAdapter.isNeedParameter()) {
            classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS);
            DebugClassAdapter debugClassAdapter = new DebugClassAdapter(classWriter, debugPreGoClassAdapter.getMethodParametersMap());
            classReader.accept(debugClassAdapter, ClassReader.EXPAND_FRAMES);
        }
        return classWriter.toByteArray();
    }

    @Override
    public boolean isWeavableClass(String fullQualifiedClassName) {
        boolean superResult = super.isWeavableClass(fullQualifiedClassName);
        boolean isByteCodePlugin = fullQualifiedClassName.startsWith(PLUGIN_LIBRARY);
        return superResult && !isByteCodePlugin;
    }



}

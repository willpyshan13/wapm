package com.will.method.plugin;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.will.base.ApmTransform;
import com.will.base.RunVariant;
import com.will.method.plugin.bytecode.DebugWeaver;

import org.gradle.api.Project;

import java.io.IOException;
import java.util.Collection;

public class MethodTransform extends ApmTransform {

    private Project project;
    private MethodExtension debugHunterExtension;

    public MethodTransform(Project project) {
        super(project);
        this.project = project;
        project.getExtensions().create("methodExt", MethodExtension.class);
        this.bytecodeWeaver = new DebugWeaver();
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        debugHunterExtension = (MethodExtension) project.getExtensions().getByName("methodExt");
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental);
    }

    @Override
    protected RunVariant getRunVariant() {
        return debugHunterExtension.runVariant;
    }

    @Override
    protected boolean inDuplcatedClassSafeMode() {
        return debugHunterExtension.duplcatedClassSafeMode;
    }
}
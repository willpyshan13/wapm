package com.will.method.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.LoggerWrapper;
import com.will.base.utils.Log;
import com.will.method.plugin.bytecode.DebugWeaver;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

public class MethodPlugin implements Plugin<Project> {
    private static final LoggerWrapper logger = LoggerWrapper.getLogger(DebugWeaver.class);
    @SuppressWarnings("NullableProblems")
    @Override
    public void apply(Project project) {

        Log.d("","**********************************");
        Log.d("","*******                    *******");
        Log.d("","*******       plugin       *******");
        Log.d("","*******                    *******");
        Log.d("","**********************************");
        AppExtension appExtension = (AppExtension)project.getProperties().get("android");
        appExtension.registerTransform(new MethodTransform(project), Collections.EMPTY_LIST);
    }

}
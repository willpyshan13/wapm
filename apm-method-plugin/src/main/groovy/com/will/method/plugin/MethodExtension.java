package com.will.method.plugin;

import com.will.base.RunVariant;

public class MethodExtension {

    public RunVariant runVariant = RunVariant.ALWAYS;
    public boolean duplcatedClassSafeMode = false;

    @Override
    public String toString() {
        return "MethodExtension{" +
                "runVariant=" + runVariant +
                ", duplcatedClassSafeMode=" + duplcatedClassSafeMode +
                '}';
    }
}

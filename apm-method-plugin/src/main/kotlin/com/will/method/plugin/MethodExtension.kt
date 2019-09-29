package com.will.method.plugin

import com.will.base.RunVariant

class MethodExtension {

    var runVariant = RunVariant.ALWAYS
    var duplcatedClassSafeMode = false

    override fun toString(): String {
        return "MethodExtension{" +
                "runVariant=" + runVariant +
                ", duplcatedClassSafeMode=" + duplcatedClassSafeMode +
                '}'.toString()
    }
}

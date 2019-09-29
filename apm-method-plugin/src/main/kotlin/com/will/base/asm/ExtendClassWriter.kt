package com.will.base.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.io.IOException
import java.io.InputStream
import java.util.HashSet

class ExtendClassWriter(private val urlClassLoader: ClassLoader, flags: Int) : ClassWriter(flags) {

    /**
     * https://github.com/Moniter123/pinpoint/blob/40106ffe6cc4d6aea9d59b4fb7324bcc009483ee/profiler/src/main/java/com/navercorp/pinpoint/profiler/instrument/ASMClassWriter.java
     */
    override fun getCommonSuperClass(type1: String, type2: String): String? {
        if (type1 == null || type1 == OBJECT || type2 == null || type2 == OBJECT) {
            return OBJECT
        }

        if (type1 == type2) {
            return type1
        }

        val type1ClassReader = getClassReader(type1)
        val type2ClassReader = getClassReader(type2)
        if (type1ClassReader == null || type2ClassReader == null) {
            return OBJECT
        }

        if (isInterface(type1ClassReader)) {
            var interfaceName = type1
            if (isImplements(interfaceName, type2ClassReader)) {
                return interfaceName
            }
            if (isInterface(type2ClassReader)) {
                interfaceName = type2
                if (isImplements(interfaceName, type1ClassReader)) {
                    return interfaceName
                }
            }
            return OBJECT
        }

        if (isInterface(type2ClassReader)) {
            return if (isImplements(type2, type1ClassReader)) {
                type2
            } else OBJECT
        }

        val superClassNames = HashSet<String>()
        superClassNames.add(type1)
        superClassNames.add(type2)

        var type1SuperClassName: String? = type1ClassReader.superName
        if (!type1SuperClassName?.let { superClassNames.add(it) }!!) {
            return type1SuperClassName
        }

        var type2SuperClassName: String? = type2ClassReader.superName
        if (!type2SuperClassName?.let { superClassNames.add(it) }!!) {
            return type2SuperClassName
        }

        while (type1SuperClassName != null || type2SuperClassName != null) {
            if (type1SuperClassName != null) {
                type1SuperClassName = getSuperClassName(type1SuperClassName)
                if (type1SuperClassName != null) {
                    if (!superClassNames.add(type1SuperClassName)) {
                        return type1SuperClassName
                    }
                }
            }

            if (type2SuperClassName != null) {
                type2SuperClassName = getSuperClassName(type2SuperClassName)
                if (type2SuperClassName != null) {
                    if (!superClassNames.add(type2SuperClassName)) {
                        return type2SuperClassName
                    }
                }
            }
        }

        return OBJECT
    }

    private fun isImplements(interfaceName: String, classReader: ClassReader): Boolean {
        var classInfo: ClassReader? = classReader

        while (classInfo != null) {
            val interfaceNames = classInfo.interfaces
            for (name in interfaceNames) {
                if (name != null && name == interfaceName) {
                    return true
                }
            }

            for (name in interfaceNames) {
                if (name != null) {
                    val interfaceInfo = getClassReader(name)
                    if (interfaceInfo != null) {
                        if (isImplements(interfaceName, interfaceInfo)) {
                            return true
                        }
                    }
                }
            }

            val superClassName = classInfo.superName
            if (superClassName == null || superClassName == OBJECT) {
                break
            }
            classInfo = getClassReader(superClassName)
        }

        return false
    }

    private fun isInterface(classReader: ClassReader): Boolean {
        return classReader.access and Opcodes.ACC_INTERFACE != 0
    }


    private fun getSuperClassName(className: String): String? {
        val classReader = getClassReader(className) ?: return null
        return classReader.superName
    }

    private fun getClassReader(className: String): ClassReader? {
        val inputStream = urlClassLoader.getResourceAsStream("$className.class")
        try {
            if (inputStream != null) {
                return ClassReader(inputStream)
            }
        } catch (ignored: IOException) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ignored: IOException) {
                }

            }
        }
        return null
    }

    companion object {

        val TAG = "ExtendClassWriter"

        private val OBJECT = "java/lang/Object"
    }
}


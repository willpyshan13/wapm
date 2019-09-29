package com.will.base.asm

import java.io.IOException
import java.io.InputStream

interface IWeaver {

    /**
     * Check a certain file is weavable
     * @param filePath path
     * @return 异常
     */
    @Throws(IOException::class)
    fun isWeavableClass(filePath: String): Boolean

    /**
     * Weave single class to byte array
     *
     * @param inputStream input
     * @return 异常
     */
    @Throws(IOException::class)
    fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray


}


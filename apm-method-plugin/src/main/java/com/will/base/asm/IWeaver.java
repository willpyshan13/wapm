package com.will.base.asm;

import java.io.IOException;
import java.io.InputStream;

public interface IWeaver {

    /**
     * Check a certain file is weavable
     * @param filePath path
     * @return 异常
     */
    public boolean isWeavableClass(String filePath) throws IOException;

    /**
     * Weave single class to byte array
     *
     * @param inputStream input
     * @return 异常
     */
    public byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException;


}


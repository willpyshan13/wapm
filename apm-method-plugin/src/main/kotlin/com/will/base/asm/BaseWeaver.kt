package com.will.base.asm

import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.attribute.FileTime
import java.util.Enumeration
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

abstract class BaseWeaver : IWeaver {

    lateinit var classLoader: ClassLoader

    @Throws(IOException::class)
    fun weaveJar(inputJar: File, outputJar: File) {
        val inputZip = ZipFile(inputJar)
        val outputZip = ZipOutputStream(
            BufferedOutputStream(
                java.nio.file.Files.newOutputStream(outputJar.toPath())
            )
        )
        val inEntries = inputZip.entries()
        while (inEntries.hasMoreElements()) {
            val entry = inEntries.nextElement()
            val originalFile = BufferedInputStream(inputZip.getInputStream(entry))
            val outEntry = ZipEntry(entry.name)
            val newEntryContent: ByteArray
            // seperator of entry name is always '/', even in windows
            if (!isWeavableClass(outEntry.name.replace("/", "."))) {
                newEntryContent = org.apache.commons.io.IOUtils.toByteArray(originalFile)
            } else {
                newEntryContent = weaveSingleClassToByteArray(originalFile)
            }
            val crc32 = CRC32()
            crc32.update(newEntryContent)
            outEntry.crc = crc32.value
            outEntry.method = ZipEntry.STORED
            outEntry.size = newEntryContent.size.toLong()
            outEntry.compressedSize = newEntryContent.size.toLong()
            outEntry.lastAccessTime = ZERO
            outEntry.lastModifiedTime = ZERO
            outEntry.creationTime = ZERO
            outputZip.putNextEntry(outEntry)
            outputZip.write(newEntryContent)
            outputZip.closeEntry()
        }
        outputZip.flush()
        outputZip.close()
    }

    @Throws(IOException::class)
    fun weaveSingleClassToFile(inputFile: File, outputFile: File, inputBaseDir: String) {
        var inputBaseDir = inputBaseDir
        if (!inputBaseDir.endsWith(FILE_SEP)) inputBaseDir = inputBaseDir + FILE_SEP
        if (isWeavableClass(
                inputFile.absolutePath.replace(inputBaseDir, "").replace(
                    FILE_SEP,
                    "."
                )
            )
        ) {
            FileUtils.touch(outputFile)
            val inputStream = FileInputStream(inputFile)
            val bytes = weaveSingleClassToByteArray(inputStream)
            val fos = FileOutputStream(outputFile)
            fos.write(bytes)
            fos.close()
            inputStream.close()
        } else {
            if (inputFile.isFile) {
                FileUtils.touch(outputFile)
                FileUtils.copyFile(inputFile, outputFile)
            }
        }
    }

    @Throws(IOException::class)
    override fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS)
        val classWriterWrapper = wrapClassWriter(classWriter)
        classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    open fun setExtension(extension: Any) {

    }

    protected fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
        return classWriter
    }

    override fun isWeavableClass(fullQualifiedClassName: String): Boolean {
        return fullQualifiedClassName.endsWith(".class") && !fullQualifiedClassName.contains("R$") && !fullQualifiedClassName.contains(
            "R.class"
        ) && !fullQualifiedClassName.contains("BuildConfig.class")
    }

    companion object {

        private val ZERO = FileTime.fromMillis(0)

        private val FILE_SEP = File.separator
    }

}

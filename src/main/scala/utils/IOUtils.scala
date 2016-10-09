package utils

import java.io._
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

/**
  * Created by Aleksey Voronets on 15.02.16.
  */

// scalastyle:off null
object IOUtils extends IOUtils

trait IOUtils {

    private type WithClose = {def close()}
    private type WithCloseAndFlush = {
        def close()
        def flush()
    }
    val DefaultBufferSize = 1024

    def ensureFlushAndClose[T <: WithCloseAndFlush, R](resource: T)(block: T => R): R =
        try {block(resource)} finally {if (resource != null) resource.flush(); resource.close()}

    def unZip(zipFile: String, outPutFolder: String): Seq[File] = {
        //zip file content
        val files = scala.collection.mutable.ArrayBuffer.empty[File]
        val buffer = new Array[Byte](DefaultBufferSize)

        ensureClose(new ZipInputStream(new FileInputStream(zipFile))) { zis =>
            //get the zipped file list entry
            var ze: ZipEntry = zis.getNextEntry()
            while (ze != null) {
                val fileName = ze.getName()
                val newFile = new File(outPutFolder + File.separator + fileName)
                files += newFile

                var len: Int = zis.read(buffer)

                ensureClose(new FileOutputStream(newFile)) { fos =>
                    while (len > 0) {
                        fos.write(buffer, 0, len)
                        len = zis.read(buffer)
                    }
                }
                ze = zis.getNextEntry()
            }

            zis.closeEntry()
        }
        files
    }

    def zip(outputPath: String, files: Seq[File]): Unit = {
        ensureClose(new ZipOutputStream(new FileOutputStream(outputPath))) { zip =>
            files.foreach { file =>
                val path = file.getAbsolutePath
                zip.putNextEntry(new ZipEntry(file.getName))
                ensureClose(new BufferedInputStream(new FileInputStream(path))) { in =>
                    var b = in.read()
                    while (b > -1) {
                        zip.write(b)
                        b = in.read()
                    }
                }
                zip.closeEntry()
            }
        }
    }

    def ensureClose[T <: WithClose, R](resource: T)(block: T => R): R =
        try {block(resource)} finally {if (resource != null) resource.close()}

}

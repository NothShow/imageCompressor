import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object CacheFile {
    private const val DIR_NAME = "img_compressor"
    private const val LOG_NAME = "log.txt"

    fun saveLog(project: String, content:String){
        val parent = File(project, DIR_NAME)
        val targetFile = File(parent, LOG_NAME)
        if (!parent.exists()){
            parent.mkdir()
        }
        if (!targetFile.exists()){
            targetFile.createNewFile()
        }
        val fileWriter = FileWriter(targetFile,true)
        val bufferWriter = BufferedWriter(fileWriter)
        bufferWriter.write(content+ "\n")
        bufferWriter.flush()
        bufferWriter.close()
        fileWriter.close()
    }
}
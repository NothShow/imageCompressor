import GitCommitUtil.getNewCommit
import GitCommitUtil.getOldCommit
import com.zhou.compress.compressor.ICompressor
import com.zhou.compress.compressor.TinypngCompressor
import com.zhou.compress.file.FileUtils
import org.eclipse.jgit.lib.Repository
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer

/**
 * 压缩图片的工具处理集成worker，包含main()方法，应该在每次送测前调用该main方法
 */
object CompressionWorker {

    /**
     * Worker的main()方法，在每次送测前要做压缩图片的具体逻辑
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val projectPath = args[0]
        CacheFile.saveLog(projectPath, "\n ${getCurrentDate()} \n")
        // 检查路径是否合法，不合法则抛出异常
        checkProjectPath(projectPath)
        val newCommit = getNewCommit(projectPath)
        val oldCommit = getOldCommit(projectPath)

        val newPicList = GitCommitUtil.getDiffPictures(projectPath,newCommit,oldCommit)
        // 压缩操作
        val compressor = TinypngCompressor()
        if (newPicList.size <= compressor.availableCount) {
            newPicList.forEach(Consumer<String> { imgPath: String ->
                val fullImgPath = projectPath + File.separatorChar + imgPath
                if (FileUtils.exists(fullImgPath)){
                    compressor.setPath(fullImgPath, fullImgPath)
                    compressor.done()
                    logCompressResult(compressor,projectPath, imgPath)
                }else{
                    println("文件不存在，跳过 $fullImgPath")
                }
            })
        }
        // push
        GitCommitUtil.commitChange(projectPath, oldCommit,newCommit)
    }

    private fun checkProjectPath(projectPath: String) {
        var repository: Repository? = null
        if (FileUtils.isDir(projectPath)) {
            repository = GitCommitUtil.getRepository(projectPath)
        }
        requireNotNull(repository) { String.format("projectPath is invalid %s", projectPath) }
    }

    private fun logCompressResult(
        compressor: ICompressor,
        projectPath: String,
        filePath: String
    ) {
        val rawSize = compressor.rawFileSize
        val compressSize = compressor.compressedFileSize
        val compressSuccess = compressor.isCompressSuccess
        val compressRate = 1f - compressSize.toFloat() / rawSize.toFloat()
        val content = StringBuffer().append(compressSuccess).append("\t")
            .append(filePath).append("\t")
            .append(rawSize).append("\t")
            .append(compressSize).append("\t")
            .append(NumberFormat.getPercentInstance().format(compressRate)).toString()
        println("logCompressResult $content")
        CacheFile.saveLog(projectPath, content)
    }

    private fun getCurrentDate():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }

}
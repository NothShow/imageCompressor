import com.zhou.compress.git.Committer
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import java.io.File
import java.io.IOException
import java.util.*

object GitCommitUtil {
    private const val MESSAGE_PREFIX = "[RC] Robot ImageCompress"
    fun getRepository(projectPath: String): Repository?{
        var repository: Repository? = null
        val builder = FileRepositoryBuilder()
        try {
            repository = builder.setGitDir(File(projectPath, Constants.DOT_GIT))
                .readEnvironment()
                .findGitDir()
                .build()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return repository!!
    }

    private fun getGit(projectPath: String):Git {
        val repository = getRepository(projectPath)
        return Git(repository)
    }

    fun getOldCommit(projectPath: String):String{
        val repository = getRepository(projectPath)
        val countOfCommit = Math.min(300,Git(repository).log().all().call().count())
        Git(repository).log().setMaxCount(countOfCommit).call().forEachIndexed { index, revCommit ->
            if (revCommit.shortMessage.startsWith(MESSAGE_PREFIX)){
                println("getOldCommit ${revCommit.shortMessage} ${revCommit.id.name} ${revCommit.commitTime}")
                return revCommit.toObjectId().name()
            }else if (index == countOfCommit - 1){
                println("getOldCommit ${revCommit.shortMessage} ${revCommit.id.name} $index  ${revCommit.commitTime}")
                return revCommit.toObjectId().name()
            }
        }
        return ObjectId.zeroId().name()
    }

    fun getNewCommit(projectPath: String): String {
        var result = ""
        val repository = Committer.getInstance().initialCommitter(projectPath).repository
        Git(repository).log().setMaxCount(1).call().forEach {
            println("getNewCommit ${it.shortMessage} ${it.id.name} ${it.commitTime}")
            result = it.toObjectId().name()
        }
        return result
    }

    private fun getTreeParser(repository: Repository?, commitId:String):CanonicalTreeParser{
        var treeId:ObjectId = ObjectId.zeroId()
        val objectId = ObjectId.fromString(commitId)
        RevWalk(repository).use { walk ->
            treeId = walk.parseCommit(objectId).tree.id
            walk.dispose()
        }
        val reader = repository?.newObjectReader()
        val treeParser = CanonicalTreeParser()
        treeParser.reset(reader, treeId)
        return treeParser
    }

    fun getDiffPictures(projectPath:String, newCommitId:String, oldCommitId:String):MutableList<String>{
        val result = mutableListOf<String>()
        val repository = getRepository(projectPath)

        val newTreeIter = getTreeParser(repository, newCommitId)
        val oldTreeIter = getTreeParser(repository, oldCommitId)

        val diffs = Git(repository).diff()
            .setNewTree(newTreeIter)
            .setOldTree(oldTreeIter)
            .call();
        diffs.forEach {
            if (it.changeType == DiffEntry.ChangeType.ADD || it.changeType == DiffEntry.ChangeType.MODIFY){
                val picturePath = it.newPath
                if (picturePath.toUpperCase().endsWith(".JPG")) {
                    println(it.newPath)
                    result.add(it.newPath)
                }else if (picturePath.toUpperCase().endsWith(".PNG") && !picturePath.contains(".9.png")){
                    println(it.newPath)
                    result.add(it.newPath)
                }
            }
        }
        return result
    }

    fun commitChange(projectPath:String, oldCommitId: String, newCommitId: String){
        val git = getGit(projectPath)
        val status = git.status().call()
        if (status.uncommittedChanges.isNotEmpty() || status.untracked.isNotEmpty()){
            val addCommand = git.add()
            status.uncommittedChanges.forEach {
                addCommand.addFilepattern(it)
            }
            status.untracked.forEach {
                addCommand.addFilepattern(it)
            }
            addCommand.call()
            git.commit()
                .setMessage("$MESSAGE_PREFIX $oldCommitId <-> $newCommitId")
                .call()
            val password = "WmhvdXppZGFuMjAxMl8="
            val usernamePasswordCredentialsProvider = UsernamePasswordCredentialsProvider("guobao_zhou@intsig.net",String(Base64.getDecoder().decode(password)))
            val branchName = "Robot_Image_Compress_"+System.currentTimeMillis()
            val ref = git.branchCreate().setName(branchName).call()
            git.push().add(ref).setCredentialsProvider(usernamePasswordCredentialsProvider).call()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {

    }
}
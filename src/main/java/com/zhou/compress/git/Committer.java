package com.zhou.compress.git;

import com.zhou.compress.file.FileUtils;
import com.zhou.compress.file.MyFileWriter;
import com.zhou.compress.file.PropertiesUtil;
import com.zhou.compress.util.StringUtil;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Committer {
    // static
    private static Committer sCommitter;

    private Committer() {
    }

    public static Committer getInstance() {
        if (sCommitter == null) {
            synchronized (Committer.class) {
                if (sCommitter == null)
                    sCommitter = new Committer();
            }
        }

        return sCommitter;
    }

    private String lastCompressCommitSHA;
    private String targetCompressCommitSHA;
    private String targetPath;
    private String logPath;

    public void loadProperties() {
        lastCompressCommitSHA = PropertiesUtil.get().getLastCommitSha();
        targetCompressCommitSHA = PropertiesUtil.get().getTargetCommitSha();
        targetPath = PropertiesUtil.get().getTargetPath();
        logPath = PropertiesUtil.get().getLogPath();
    }


    public static final int FROM_COMMIT = 233, FROM_TREE = 666;

    // member
    Repository mRepository;

    public Committer initialCommitter(String rootPath) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            mRepository = builder.setGitDir(new File(rootPath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mRepository == null)
            System.out.println("Committer:  未获取到git仓库！");
        else
            System.out.println("Committer:  获取到了git仓库：" + mRepository.getDirectory());

        return sCommitter;
    }

    public Repository getRepository() {
        if (mRepository == null) {
            File file;
            if (StringUtil.isEmpty(targetPath)) {
                file = new File(new File("").getAbsolutePath(), Constants.DOT_GIT);
            } else {
                file = new File(targetPath);
            }
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            try {
                mRepository = builder.setGitDir(file)
                        .readEnvironment()
                        .findGitDir()
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mRepository;
    }

    /**
     * add一个修改过的文件到暂存区
     *
     * @param filePath 修改后文件的路径
     * @return
     */
    public Committer addFile(String filePath) {
        Git git = new Git(mRepository);

        try {
            git.add().addFilepattern(filePath).call();
            System.out.println("Commit: 成功add修改后的所有文件");
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return sCommitter;
    }

    /**
     * add多个文件到暂存区
     *
     * @param imgPathList 一个存放文件们的路径的List
     * @return
     */
    public Committer addFile(List<String> imgPathList) {
        Git git = new Git(mRepository);

        try {
            AddCommand addCommand = git.add();
            imgPathList.forEach(imgPath -> addCommand.addFilepattern(imgPath));
            addCommand.call();
            System.out.println("Commit: 成功add修改后的所有文件");
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return sCommitter;
    }

    /**
     * add所有修改过的文件到暂存区
     *
     * @return
     */
    public Committer addFile() {
        return addFile(".");
    }

    public Committer commitChanges(String msg) {
        try {
            new Git(mRepository).commit().setMessage(msg).call();
            System.out.println("Committer:  提交了修改，修改内容为" + msg);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return sCommitter;
    }

    public ObjectId getCurrentCommitTree() {
        List<ObjectId> objectIds = new ArrayList<>();
        try {
            new Git(mRepository).log().setMaxCount(1).call().forEach(revCommit -> {
                objectIds.add(revCommit.getTree().getId());
            });
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return objectIds.get(0).toObjectId();
    }

    public ObjectId getCurrentCommit() {
        List<ObjectId> objectIds = new ArrayList<>();
        try {
            new Git(mRepository).log().setMaxCount(1).call().forEach(revCommit -> {
                objectIds.add(revCommit.getId());
            });
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return objectIds.get(0).toObjectId();
    }

    public ObjectId getRecordCommitTree(String path) {
        ObjectId objectId;
//        String objectIdStr = MyFileWriter.getInstance()
//                .loadLineFromFile(path)
//                .get(0).split(" ")[1];
        String objectIdStr = MyFileWriter.getInstance()
                .loadLineFromFile(path)
                .get(0);
        objectId = ObjectId.fromString(objectIdStr);
        return objectId;
    }

    public ObjectId getRecordCommit(String path) {
        ObjectId objectId;
//        String objectIdStr = MyFileWriter.getInstance()
//                .loadLineFromFile(path)
//                .get(0).split(" ")[1];
        String objectIdStr = MyFileWriter.getInstance()
                .loadLineFromFile(path)
                .get(0);
        objectId = ObjectId.fromString(objectIdStr);
        return objectId;
    }

    public List<DiffEntry> compareDiffFromFileWithCommitId(String path) throws IOException, GitAPIException {
        if (mRepository == null)
            return null;
        RevCommit oldCommit, newCommit;
        ObjectId newCommitId = getCurrentCommit(), oldCommitId = getRecordCommit(path);
        // 利用存储和现在最近的commit值获取commit
        try (RevWalk walk = new RevWalk(mRepository)) {
            oldCommit = walk.parseCommit(oldCommitId);
            walk.reset();
            newCommit = walk.parseCommit(newCommitId);
            walk.dispose();
        }


        ObjectReader reader = mRepository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldCommit.getTree().getId());

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newCommit.getTree().getId());

        return new Git(mRepository).diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();
    }


    public List<DiffEntry> compareDiffFromFileWithTreeId(String path) throws IOException, GitAPIException {

        ObjectReader reader = mRepository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, getRecordCommitTree(path));

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, getCurrentCommitTree());

        List<DiffEntry> diffs = new Git(mRepository).diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();
        return diffs;
    }

    public ArrayList<String> getDiffPictures(String path, int method) throws IOException, GitAPIException {
        List<DiffEntry> diffs;
        if (method == FROM_COMMIT)
            diffs = compareDiffFromFileWithCommitId(path);
        else if (method == FROM_TREE)
            diffs = compareDiffFromFileWithTreeId(path);
        else
            return null;
        ArrayList<String> changedPicturePaths = new ArrayList<>();
        String picturePath = "";

        for (DiffEntry diff : diffs) {
            picturePath = diff.getNewPath();
            if (picturePath.toUpperCase().endsWith(".JPG") || picturePath.toUpperCase().endsWith(".PNG")) {
                changedPicturePaths.add(picturePath);
            }
        }
        return changedPicturePaths;
    }

    public List<String> getDiffPictures() {
        List<String> picPathList = new ArrayList<>();
        ObjectId oldCommitId = getLastCommit();
        ObjectId targetCommitId = getTargetCommit();
        List<DiffEntry> diffEntries = null;
        try {
            diffEntries = compareDiff(oldCommitId, targetCommitId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        if (diffEntries != null) {
            diffEntries
                    .stream()
                    .filter(diffEntry -> FileUtils.isImage(diffEntry.getNewPath()))
                    .forEach(diffEntry -> {
                        String filePath = diffEntry.getNewPath();
                        File file = new File(getRootPath(), filePath);
                        if (file.exists()) {
                            picPathList.add(file.getPath());
                        } else {
                            // TODO
                        }
                    });
        }

        if(checkCommitOldest(oldCommitId)){
            System.out.println("当前lastCommit是最早的一次commit，需要额外压缩，可能耗时较久，请稍候...");
            List<String> firstFilePaths = getFilePathListByCommitObjectId(oldCommitId);
            picPathList.removeAll(firstFilePaths);
            picPathList.addAll(firstFilePaths);
        }
        return picPathList;
    }

    private List<DiffEntry> compareDiff(ObjectId oldCommitId, ObjectId targetCommitId) throws IOException, GitAPIException {
        RevCommit oldCommit, newCommit;
        try (RevWalk walk = new RevWalk(mRepository)) {
            oldCommit = walk.parseCommit(oldCommitId);
            walk.reset();
            newCommit = walk.parseCommit(targetCommitId);
            walk.dispose();
        }

        ObjectReader reader = mRepository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldCommit.getTree().getId());

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newCommit.getTree().getId());

        return new Git(mRepository).diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();
    }

    public ObjectId getTargetCommit() {
        if (StringUtil.isEmpty(targetCompressCommitSHA)) {
            return getCurrentCommit();
        } else {
            return ObjectId.fromString(targetCompressCommitSHA);
        }
    }

    private ObjectId getLastCommit() {
        if (StringUtil.isEmpty(lastCompressCommitSHA)) {
            throw new IllegalArgumentException("缺少参数：lastCompressCommitSHA");
        } else {
            return ObjectId.fromString(lastCompressCommitSHA);
        }
    }

    public File getTargetFile() {
        return targetPath == null ? new File("") : new File(targetPath);
    }

    public String getRootPath() {
        return mRepository != null ? mRepository.getWorkTree().getPath() : null;
    }

    public List<String> getFilePathListByCommitSHA(String commitSHA) {
        ObjectId objId = ObjectId.fromString(commitSHA);
        return getFilePathListByCommitObjectId(objId);
    }

    public List<String> getFilePathListByCommitObjectId(ObjectId objId) {
        List<String> pathList = new ArrayList<>();
        try (RevWalk revWalk = new RevWalk(mRepository)) {
            RevCommit commit = revWalk.parseCommit(objId);
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(mRepository);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                String filePath = treeWalk.getPathString();
                if (!StringUtil.isEmpty(filePath) && FileUtils.isImage(filePath)){
                    pathList.add(filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathList;
    }

    /**
     * 给定一个commitSHA,判断是否为最早的commit
     *
     * @param commitSHA
     * @return
     */
    public boolean checkCommitOldest(String commitSHA){
        // boolean数组，其中第0个变量表示isOldest,第1个表示findCommit
        ObjectId commitObjectId = ObjectId.fromString(commitSHA);
        return checkCommitOldest(commitObjectId);
    }
    /**
     * 给定一个commit的objectId,判断是否为最早的commit
     *
     * @param commitObjectId
     * @return
     */
    public boolean checkCommitOldest(ObjectId commitObjectId){
        // boolean数组，其中第0个变量表示isOldest,第1个表示findCommit
        boolean[] booleans = new boolean[]{true, false};
        try {
            new Git(mRepository).log().call().forEach(revCommit -> {
                if(booleans[1]){
                    booleans[0] = false;
                }
                if(revCommit.equals(commitObjectId)){
                    booleans[1] = true;
                }
            });
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return booleans[0];
    }

    // main函数仅仅用来测试
    public static void main(String[] args) {
//        System.out.println(Committer.getInstance().initialCommitter("").getCurrentCommit());
//        ArrayList<String> lines = MyFileWriter.getInstance().loadLineFromFile("D:\\workplace\\TinypngSample\\img_compressor\\test.txt");
//        for (String a:
//             lines) {
//            System.out.println(a);
//        }
//        MyFileWriter.getInstance().writeLineIntoFile("line4'sContent!@#$%^&*()", "D:\\workplace\\TinypngSample\\img_compressor\\test.txt", false);
//        MyFileWriter.getInstance().writeLineIntoFile("line5'sContent!@#$%^&*()", "D:\\workplace\\TinypngSample\\img_compressor\\test.txt", true);
//        MyFileWriter.getInstance().writeLineIntoFile("line6'sContent!@#$%^&*()", "D:\\workplace\\TinypngSample\\img_compressor\\test.txt", true);
//
//        String line = getInstance().initialCommitter("D:\\workplace\\TinypngSample\\").getCurrentCommit().toString();
//        String[] arr = line.split(" ");
//        MyFileWriter.getInstance().writeLineIntoFile(arr[1], "D:\\workplace\\TinypngSample\\img_compressor\\config.txt", true);

//        try {
//            ArrayList<String> paths = getInstance().initialCommitter("D:\\workplace\\imagecompressiontool\\").getDiffPictures("D:\\workplace\\TinypngSample\\img_compressor\\config.txt", Committer.FROM_COMMIT);
//            for (String path :
//                    paths) {
//                System.out.println(path);
//            }
//        } catch (IOException | GitAPIException e) {
//            e.printStackTrace();
//        }


        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository mRepository = builder.setGitDir(new File("D:\\workspace\\imagecompressiontool\\", ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            ObjectId objId = mRepository.resolve("7431e7f52fec6a41ba6641a677941890ef42d64b");
//            RevWalk revWalk = new RevWalk(mRepository);
            try (RevWalk revWalk = new RevWalk(mRepository)) {
                RevCommit commit = revWalk.parseCommit(objId);
                RevTree tree = commit.getTree();
                System.out.println("Having tree: " + tree);
                TreeWalk treeWalk = new TreeWalk(mRepository);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);

//                if (!treeWalk.next()) {
//                    throw new IllegalStateException("Did not find expected file 'README.md'");
//                }
                while (treeWalk.next()) {
                    System.out.println(treeWalk.getPathString());
                }


//                ObjectId objectId = treeWalk.getObjectId(0);
//                ObjectLoader loader = mRepository.open(objectId);
//
//                // and then one can the loader to read the file
//                loader.copyTo(System.out);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

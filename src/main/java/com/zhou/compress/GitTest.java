package com.zhou.compress;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitTest {

    public static void main(String[] s) throws IOException, GitAPIException {
        String gitPath = "D:\\workplace\\TinypngSample";
        File file = new File(gitPath);
        if (!file.exists()){
            return;
        }
//        String gitPath = "D:\\workplace\\AndroidLearnProjects\\CriminalIntent";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(gitPath,".git"))
                .readEnvironment()
                .findGitDir()
                .build();
        Git git = new Git(repository);
/*        git.log().call().forEach( revCommit -> {
            //SHA revCommit.getName()
            System.out.println(revCommit.getShortMessage() +"   " + revCommit.getName());

        });*/
        List<ObjectId> objectIds = new ArrayList<>();
        System.out.println("输出Commit的序列（最新2个）：");
        git.log().setMaxCount(2).call().forEach( revCommit -> {
            //SHA revCommit.getName()
            System.out.println("SHA1值为：" + revCommit.getName());
            System.out.println("概要信息为：" + revCommit.getShortMessage());
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^");
            ObjectId id = revCommit.getId();
            RevTree revTree = revCommit.getTree();
            ObjectId treeId = revTree.getId();
            objectIds.add(treeId);
        });

        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, objectIds.get(1));

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
//        newTreeIter.reset(reader, Committer.getInstance().testFun());
        newTreeIter.reset(reader, objectIds.get(0));

        List<DiffEntry> diffs= git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();
        System.out.println("输出两次commit的不同");
        for (DiffEntry diff : diffs) {
            System.out.println(diff.getNewPath());
//            diff.getOldPath();
//            diff.getNewPath();
        }


    }
}
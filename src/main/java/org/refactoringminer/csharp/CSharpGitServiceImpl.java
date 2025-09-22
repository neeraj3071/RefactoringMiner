package org.refactoringminer.csharp;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.refactoringminer.util.GitServiceImpl;

import java.util.Map;
import java.util.Set;

/**
 * CSharpGitServiceImpl - Extended GitService that supports C# files alongside Java files
 * 
 * This class extends the standard GitServiceImpl to detect and process .cs files
 * in addition to .java files, enabling RefactoringMiner to work with C# repositories.
 */
public class CSharpGitServiceImpl extends GitServiceImpl {
    
    /**
     * Check if a file is either a Java or C# source file
     */
    private boolean isJavaOrCSharpFile(String path) {
        if (path == null) {
            return false;
        }
        
        boolean isJava = path.endsWith(".java");
        boolean isCSharp = path.endsWith(".cs");
        
        if (isCSharp) {
            System.out.println("DEBUG: Found C# file - " + path);
        }
        
        return isJava || isCSharp;
    }
    
    /**
     * Override fileTreeDiff to support C# files alongside Java files
     */
    @Override
    public void fileTreeDiff(Repository repository, RevCommit currentCommit, Set<String> javaFilesBefore, Set<String> javaFilesCurrent, Map<String, String> renamedFilesHint) throws Exception {
        System.out.println("DEBUG: CSharpGitServiceImpl.fileTreeDiff - Processing commit");
        
        if (currentCommit.getParentCount() > 0) {
            ObjectId oldTree = currentCommit.getParent(0).getTree();
            ObjectId newTree = currentCommit.getTree();
            final TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(oldTree);
            tw.addTree(newTree);

            final RenameDetector rd = new RenameDetector(repository);
            rd.setRenameScore(55);
            rd.addAll(DiffEntry.scan(tw));

            for (DiffEntry diff : rd.compute(tw.getObjectReader(), null)) {
                DiffEntry.ChangeType changeType = diff.getChangeType();
                String oldPath = diff.getOldPath();
                String newPath = diff.getNewPath();
                
                System.out.println("DEBUG: Processing diff entry - " + changeType + ": " + oldPath + " -> " + newPath);
                
                if (changeType != DiffEntry.ChangeType.ADD) {
                    if (isJavaOrCSharpFile(oldPath)) {
                        javaFilesBefore.add(oldPath);
                        System.out.println("DEBUG: Adding to filesBefore: " + oldPath);
                    }
                }
                if (changeType != DiffEntry.ChangeType.DELETE) {
                    if (isJavaOrCSharpFile(newPath)) {
                        javaFilesCurrent.add(newPath);
                        System.out.println("DEBUG: Adding to filesCurrent: " + newPath);
                    }
                }
                if (changeType == DiffEntry.ChangeType.RENAME && diff.getScore() >= rd.getRenameScore()) {
                    if (isJavaOrCSharpFile(oldPath) && isJavaOrCSharpFile(newPath)) {
                        renamedFilesHint.put(oldPath, newPath);
                        System.out.println("DEBUG: Detected rename: " + oldPath + " -> " + newPath);
                    }
                }
            }
            tw.close();
        }
        else if (currentCommit.getParentCount() == 0) {
            // initial commit of the repository
            System.out.println("DEBUG: Processing initial commit");
            ObjectId newTree = currentCommit.getTree();
            final TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(newTree);
            
            while (tw.next()) {
                String newPath = tw.getPathString();
                if (isJavaOrCSharpFile(newPath)) {
                    javaFilesCurrent.add(newPath);
                    System.out.println("DEBUG: Adding initial file: " + newPath);
                }
            }
            tw.close();
        }
        
        System.out.println("DEBUG: fileTreeDiff completed - filesBefore: " + javaFilesBefore.size() + 
                         ", filesCurrent: " + javaFilesCurrent.size() + 
                         ", renames: " + renamedFilesHint.size());
    }
}
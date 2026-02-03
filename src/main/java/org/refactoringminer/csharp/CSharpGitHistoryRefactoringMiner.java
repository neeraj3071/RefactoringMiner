package org.refactoringminer.csharp;

import gr.uom.java.xmi.UMLModel;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.util.*;

/**
 * C# GitHistoryRefactoringMiner - Overrides model creation to handle C# files
 * by using CPatMiner transformation before standard RefactoringMiner processing
 */
public class CSharpGitHistoryRefactoringMiner extends GitHistoryRefactoringMinerImpl {

    /**
     * PUBLIC STATIC OVERRIDE - Required to intercept parent class's createModel() calls
     * Checks if C# files are present and routes to appropriate parser
     */
    public static UMLModel createModel(Map<String, String> fileContents, Set<String> repositoryDirectories) throws Exception {
        // Count C# files
        long csharpFiles = fileContents.keySet().stream()
            .filter(path -> path != null && path.toLowerCase().endsWith(".cs"))
            .count();
        
        if (csharpFiles > 0) {
            System.out.println("CSharpGitHistoryRefactoringMiner.createModel: Found " + csharpFiles + 
                             " C# files - using CSharpUMLModelASTReader");
            return new CSharpUMLModelASTReader(fileContents, repositoryDirectories, false).getUmlModel();
        } else {
            // No C# files - use parent class's standard Java parser
            System.out.println("CSharpGitHistoryRefactoringMiner.createModel: No C# files found - using standard UMLModelASTReader");
            return GitHistoryRefactoringMinerImpl.createModel(fileContents, repositoryDirectories);
        }
    }

    /**
     * Override detectRefactorings to use C# GitService that supports .cs files
     */
    @Override
    protected List<Refactoring> detectRefactorings(GitService gitService, Repository repository, 
                                                    RefactoringHandler handler, RevCommit currentCommit) throws Exception {
        
        // Use our C# GitService instead of the provided one
        CSharpGitServiceImpl csharpGitService = new CSharpGitServiceImpl();
        return super.detectRefactorings(csharpGitService, repository, handler, currentCommit);
    }

    @Override
    public void detectAll(Repository repository, String branch, RefactoringHandler handler) throws Exception {
        // Use our C# GitService
        CSharpGitServiceImpl gitService = new CSharpGitServiceImpl() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        
        RevWalk walk = gitService.createAllRevsWalk(repository, branch);
        try {
            detectWithCustomGitService(gitService, repository, handler, walk.iterator());
        } finally {
            walk.dispose();
        }
    }

    @Override
    public void detectBetweenCommits(Repository repository, String startCommitId, String endCommitId,
                                   RefactoringHandler handler) throws Exception {
        // Use our C# GitService
        CSharpGitServiceImpl gitService = new CSharpGitServiceImpl() {
            @Override
            public boolean isCommitAnalyzed(String sha1) {
                return handler.skipCommit(sha1);
            }
        };
        
        Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repository, startCommitId, endCommitId);
        detectWithCustomGitService(gitService, repository, handler, walk.iterator());
    }
    
    /**
     * Custom detection loop using C# GitService
     */
    private void detectWithCustomGitService(GitService gitService, Repository repository, 
                                          RefactoringHandler handler, Iterator<RevCommit> commits) {
        while (commits.hasNext()) {
            RevCommit currentCommit = commits.next();
            try {
                detectRefactorings(gitService, repository, handler, currentCommit);
            } catch (Exception e) {
                handler.handleException(currentCommit.getId().getName(), e);
            }
        }
    }
    
    @Override
    public void detectAtCommit(Repository repository, String commitId, RefactoringHandler handler) {
        RevWalk walk = new RevWalk(repository);
        try {
            // Get the RevCommit object directly
            RevCommit commit = walk.parseCommit(repository.resolve(commitId));
            if (commit.getParentCount() > 0) {
                walk.parseCommit(commit.getParent(0));
            }
            
            // Use our C# GitService
            CSharpGitServiceImpl gitService = new CSharpGitServiceImpl();
            detectRefactorings(gitService, repository, handler, commit);
        } catch (Exception e) {
            handler.handleException(commitId, e);
        } finally {
            walk.close();
        }
    }

    /**
     * Entry point from command line - detects refactorings at specific commit
     */
    public static void detectAtCommit(String repoPath, String commitId) {
        try {
            GitServiceImpl tempGitService = new GitServiceImpl();
            Repository repo = tempGitService.cloneIfNotExists(repoPath, repoPath);
            CSharpGitHistoryRefactoringMiner miner = new CSharpGitHistoryRefactoringMiner();
            
            System.out.println("Analyzing C# commit: " + commitId);
            miner.detectAtCommit(repo, commitId, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    System.out.println("=== REFACTORINGS FOUND ===");
                    if (refactorings.isEmpty()) {
                        System.out.println("No refactorings detected");
                    } else {
                        for (Refactoring refactoring : refactorings) {
                            System.out.println(refactoring.toString());
                        }
                    }
                }

                @Override
                public void handleException(String commit, Exception e) {
                    System.err.println("Error analyzing commit " + commit + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
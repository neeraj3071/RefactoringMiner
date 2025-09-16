package main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.SystemUtils;

import transformation.Transformation;
import utils.FileIO;
import utils.NotifyingBlockingThreadPoolExecutor;
import change.ChangeAnalyzer;


public class MainChangeAnalyzer {
    private static int THREAD_POOL_SIZE = 1;

    private static final Callable<Boolean> blockingTimeoutCallback = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return true; // keep waiting
        }
    };
    private static NotifyingBlockingThreadPoolExecutor pool = new NotifyingBlockingThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 15, TimeUnit.SECONDS, 200, TimeUnit.MILLISECONDS, blockingTimeoutCallback);

    public static String inputPath = "E:/PhD1/research_project_2/CPatMinerV2/repositories", outputPath = "E:/PhD1/research_project_2/CPatMinerV2/outputs";

    public static void main(String[] args) {

        //Transformation.transform(); // for testing and debugging with test.cs file

        String content = null;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-i")) {
                    inputPath = args[i + 1];
                }
                if (args[i].equals("-o")) {
                    outputPath = args[i + 1];
                }
            }
        }

        inputPath = "E:/PhD1/research_project_2/CPatMinerV2/repositories";
        outputPath = "E:/PhD1/research_project_2/CPatMinerV2/outputs";
        content = FileIO.readStringFromFile(inputPath + "/repos.csv");
        Scanner sc = new Scanner(content);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            int index = line.indexOf(',');
            if (index < 0)
                index = line.length();
            String name = line.substring(0, index);
            File dir = new File(inputPath + "/" + name);
            analyze(dir, name);
        }
        sc.close();

        try {
            pool.await(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
        }
    }

    private static void analyze(final File dir, final String name) {
        if (!dir.isDirectory())
            return;
        File git = new File(dir, ".git");
        if (git.exists()) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    long startProjectTime = System.currentTimeMillis();
                    System.out.println(name);
                    String url = dir.getAbsolutePath();
                    ChangeAnalyzer ca = new ChangeAnalyzer(name, -1, url);
                    ca.buildGitConnector();
                    ca.analyzeGit();
                    long endProjectTime = System.currentTimeMillis();
                    ca.getCproject().setRunningTime(endProjectTime - startProjectTime);
                    ca.closeGitConnector();
                    System.out.println("Done " + name + " in " + (endProjectTime - startProjectTime) / 1000 + "s");
                }
            });
        }
    }

}

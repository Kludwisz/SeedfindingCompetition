package kludwisz.seedfinding.day6;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Day6 {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        long taskSize = 1_000_000_000L;
        for (int task = 0; task < 24; task++) {
            long rangeStart = task * taskSize;
            long rangeEnd = (task + 1) * taskSize;
            executor.submit(new StructureClusterFinder(rangeStart, rangeEnd));
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

package kludwisz.seedfinding.day1;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.data.SeedList;
import kludwisz.structure.TrialChambers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Day1 {
    public static final long seed = -6583981180238954485L;
    public static final AtomicInteger completed = new AtomicInteger(0);

    public static void main1(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(8);

        int globalRegionMin = -30_000_000 / 16 / 34;
        int globalRegionMax = 30_000_000 / 16 / 34;
        int regionZPerTask = 100;
        int tasks = (int)Math.ceil(((double)globalRegionMax - globalRegionMin) / regionZPerTask);
        System.out.println("Executing " + tasks + " tasks...");

        int xMin = globalRegionMin, xMax = globalRegionMax;
        // splitting work only over regionZ
        for (int i = 0; i < tasks; i++) {
            int zMin = globalRegionMin + i * regionZPerTask;
            int zMax = Math.min(globalRegionMin + (i + 1) * regionZPerTask - 1, globalRegionMax);
            System.out.println("Task #" + i + " - zMin: " + zMin + ", zMax: " + zMax);
            FirstFilter filter = new FirstFilter(seed, new RPos(xMin, zMin, 34), new RPos(xMax, zMax, 34));
            executor.submit(filter);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(4000);
                SeedList list = new SeedList(SeedList.EntryFormat.SEED, SeedList.EntryFormat.CHUNK_POS);
                synchronized(ResultCollector.results) {
                    ResultCollector.getResults().forEach(result -> list.addEntry(List.of(result.seed(), result.regionX(), result.regionZ())));
                    ResultCollector.getResults().clear();
                }
                list.appendToFile("src/main/resources/day1.txt");
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished execution.");
        //ResultCollector.getResults().forEach(System.out::println); // precaution
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(8);

        int globalRegionMin = -30_000_000 / 16 / 34;
        int globalRegionMax = 30_000_000 / 16 / 34;
        int regionZPerTask = 100;
        int tasks = (int)Math.ceil(((double)globalRegionMax - globalRegionMin) / regionZPerTask);
        System.out.println("Executing " + tasks + " tasks...");

        int xMin = globalRegionMin, xMax = globalRegionMax;
        // splitting work only over regionZ
        for (int i = 0; i < tasks; i++) {
            int zMin = globalRegionMin + i * regionZPerTask;
            int zMax = Math.min(globalRegionMin + (i + 1) * regionZPerTask - 1, globalRegionMax);
            System.out.println("Task #" + i + " - zMin: " + zMin + ", zMax: " + zMax);
            FirstFilter filter = new FirstFilter(seed, new RPos(xMin, zMin, 34), new RPos(xMax, zMax, 34));
            executor.submit(filter);
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(4000);
                SeedList list = new SeedList(SeedList.EntryFormat.SEED, SeedList.EntryFormat.CHUNK_POS);
                synchronized(ResultCollector.results) {
                    ResultCollector.getResults().forEach(result -> list.addEntry(List.of(result.seed(), result.regionX(), result.regionZ())));
                    ResultCollector.getResults().clear();
                }
                list.appendToFile("src/main/resources/day1_alt.txt");
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished execution.");
        //ResultCollector.getResults().forEach(System.out::println); // precaution
    }

    public static void main2(String[] args) {
        SeedList list = SeedList.fromFile("src/main/resources/day1.txt", SeedList.EntryFormat.SEED, SeedList.EntryFormat.CHUNK_POS);
        SecondFilter filter2 = new SecondFilter(seed);

        int i = 0;
        for (var entry : list.getEntries()) {
            //if (i % 100 == 0)
            //    System.out.println("tested " + i);

            CPos regionPos = entry.getChunkPos(0);
            filter2.testPos(new RPos(regionPos.getX(), regionPos.getZ(), 34));
            i++;
        }
    }
}

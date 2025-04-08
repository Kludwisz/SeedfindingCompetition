package kludwisz.seedfinding.day1;

import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import kludwisz.data.SeedList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entry points for the code I used to find my submission for day 1 of the competition.
 * The task was to find the coolest/rarest feature on a given seed: -6583981180238954485.
 * I decided to find a seed that generates a bunch of minecart chests near an interset point
 * inside a Trial Chambers structure (spawner or vault). This worked out pretty well in the
 * end, but I encountered some dead ends along the way, hence the multiple entry points.
 */
public class Day1 {
    public static final long seed = -6583981180238954485L;
    public static final AtomicInteger completed = new AtomicInteger(0);

    /**
     * The very first (and ultimately best) attempt at finding a submission.
     */
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
                SeedList list = new SeedList(SeedList.EntryFormat.CHUNK_POS);
                synchronized(ResultCollector.results) {
                    ResultCollector.getResults().forEach(result -> list.addEntry(List.of(result.regionX(), result.regionZ())));
                    ResultCollector.getResults().clear();
                }
                list.appendToFile("src/main/resources/day1_final.txt");
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished execution.");
        //ResultCollector.getResults().forEach(System.out::println); // precaution
    }

    /**
     * The second attempt that aimed (and failed) to explicitly find 3-chest clusters
     */
    public static void main_attempt2() {
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
                SeedList list = new SeedList(SeedList.EntryFormat.BLOCK_POS);
                synchronized(ResultCollector.results2) {
                    ResultCollector.results2.forEach(result -> list.addEntry(List.of((long)result.pos().getX(), (long)result.pos().getY(), (long)result.pos().getZ())));
                    ResultCollector.results2.clear();
                }
                list.appendToFile("src/main/resources/day1_triple.txt");
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished execution.");
        //ResultCollector.getResults().forEach(System.out::println); // precaution
    }

    /**
     * The third attempt in which I tried to make use of multiple Mineshafts. This also
     * failed, as it turned out that the decorator-seeded PRNG doesn't get reset on a
     * per-structure-start basis, but rather per structure type.
     */
    public static void main_attempt3() {
        SeedList list = SeedList.fromFile("src/main/resources/day1_final.txt", SeedList.EntryFormat.CHUNK_POS);
        System.out.println("Loaded " + list.getEntries().size() + " entries.");
        SecondFilter filter2 = new SecondFilter(seed);

        int i = 0;
        for (var entry : list.getEntries()) {
            if (i % 1000 == 0)
                System.out.println("tested " + i);

            CPos regionPos = entry.getChunkPos(0);
            filter2.testPos(new RPos(regionPos.getX(), regionPos.getZ(), 34));
            SeedList outList = new SeedList(SeedList.EntryFormat.BLOCK_POS);
            ResultCollector.results2.forEach(result -> outList.addEntry(List.of((long)result.pos().getX(), (long)result.pos().getY(), (long)result.pos().getZ())));
            ResultCollector.results2.clear();
            outList.appendToFile("src/main/resources/day1_final_out.txt");
            i++;
        }
    }
}

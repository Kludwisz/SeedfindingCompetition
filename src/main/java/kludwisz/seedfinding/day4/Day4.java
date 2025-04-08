package kludwisz.seedfinding.day4;

import kludwisz.data.SeedList;

import java.util.List;
import java.util.stream.LongStream;

/**
 * Entry points for the code I used to find my submission for day 4 of the competition.
 * In short, the task was to find a seed with as many half-beds within 200 blocks of 0,0 as possible.
 * The code achieves that by finding a rare intersection of a Trial Chambers and an Ancient City, and
 * filtering the results to those that can also potentially generate an igloo half-bed.
 */
public class Day4 {
    public static void main(String[] args) {
        //findQuadBed();
        //findIglooQuadBed();
        extendIglooSeeds();
    }

    /**
     * Entry point for the simple quad-bed finder.
     */
    private static void findQuadBed() {
        // let's first find a seed with one cluster of half beds
        List<HalfBedFinder.Result> results = HalfBedFinder.run(100_000_000L, 200_000_000L);

        results.forEach(r -> {
            SeedList list = new SeedList();

            LongStream.range(0L, 100L)
                    .map(upper -> (upper << 48) | r.structureSeed())
                    .forEach(worldSeed -> list.addEntry(List.of(worldSeed)));

            list.appendToFile("src/main/resources/day4_D.txt");
        });
    }

    /**
     * Entry point for the first phase of the quad-bed + igloo finder.
     */
    private static void findIglooQuadBed() {
        final long taskSize = 1_000_000_000L;

        for (long task = 0L; task < 1000L; task++) {
            System.out.println("running task " + task);
            List<HalfBedFinder.Result> results = HalfBedFinder.run2(task * taskSize, (task+1) * taskSize);

            results.forEach(r -> {
                SeedList list = new SeedList();

                LongStream.range(0L, 8000L)
                        .map(upper -> (upper << 48) | r.structureSeed())
                        .forEach(worldSeed -> list.addEntry(List.of(worldSeed)));

                list.appendToFile("src/main/resources/day4_igloo.txt");
            });
        }
    }

    /**
     * Entry point for the second phase of the quad-bed + igloo finder.
     * Extends the seed list of igloo half-bed structure seeds to full worldseeds for use in Cubiomes Viewer.
     */
    private static void extendIglooSeeds() {
        SeedList igloos = SeedList.fromFile("src/main/resources/day4_igloo.txt");
        SeedList extIgloos = new SeedList();
        igloos.toFlatStructureSeedList().getEntries().forEach(ent -> {
            long structureSeed = ent.getSeed();
            LongStream.range(0, 65536L).map(upper -> (upper << 48) | structureSeed).forEach(worldSeed -> {
                extIgloos.addEntry(List.of(worldSeed));
            });
        });
        extIgloos.toFile("src/main/resources/day4_igloo_ext.txt");
    }

//    public static void main(String [] args) {
//        long ss = 78532210291632797L & Mth.MASK_48;
//        SeedList list = new SeedList();
//        LongStream.range(0L, 65536L)
//                .map(upper -> (upper << 48) | ss)
//                .forEach(worldSeed -> list.addEntry(List.of(worldSeed)));
//        list.toFile("src/main/resources/day4_vill_test.txt");
//    }
}

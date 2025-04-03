package kludwisz.seedfinding.day4;

import kludwisz.data.SeedList;

import java.util.List;
import java.util.stream.LongStream;

public class Day4 {
    public static void main_old(String[] args) {
        // let's first find a seed with one cluster of half beds
        List<HalfBedFinder.Result> results = HalfBedFinder.run(100_000_000L, 200_000_000L);

        // map the results to lists of 5k worldseeds each (checkable via cubiomes)
        results.forEach(r -> {
            SeedList list = new SeedList();

            LongStream.range(0L, 100L)
                    .map(upper -> (upper << 48) | r.structureSeed())
                    .forEach(worldSeed -> list.addEntry(List.of(worldSeed)));

            list.appendToFile("src/main/resources/day4_D.txt");
        });
    }

    public static void main(String[] args) {
        final long taskSize = 100_000_000L;

        for (long task = 0; task < 20; task++) {
            System.out.println("running task " + task);
            List<HalfBedFinder.Result> results = HalfBedFinder.run2(task * taskSize, (task+1) * taskSize);

            results.forEach(r -> {
                SeedList list = new SeedList();

                LongStream.range(0L, 65536L)
                        .map(upper -> (upper << 48) | r.structureSeed())
                        .forEach(worldSeed -> list.addEntry(List.of(worldSeed)));

                list.appendToFile("src/main/resources/day4_E.txt");
            });
        }
    }

    // debug
//    public static void main(String[] args) {
//        HalfBedFinder.debug(5066549583033058L);
//    }
}

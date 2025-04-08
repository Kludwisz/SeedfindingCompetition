package kludwisz.seedfinding.day6;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.Mineshaft;
import kludwisz.ancientcity.AncientCity;
import kludwisz.ancientcity.AncientCityGenerator;
import kludwisz.data.SeedList;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.mineshafts.MineshaftLoot;
import kludwisz.structure.TrialChambers;

import java.util.ArrayList;
import java.util.List;

/**
 * This was our original idea for round 6 of the seedfinding competition - finding
 * large clusters of underground structures. Later on, we came up with a much more
 * unique & fun idea, which you can find the implementation of in the files:
 * - MineshaftStateReverser.java
 * - CorridorFinder.java
 */
public class StructureClusterFinder implements Runnable {
    public static final String FULL_RESULTS_FILENAME = "src/main/resources/day6_full.txt";
    public static final String SEED_RESULTS_FILENAME = "src/main/resources/day6_seeds.txt";

    private static final MCVersion VERSION = MCVersion.v1_21;
    private static final DistanceMetric CHEBYSHEV = DistanceMetric.CHEBYSHEV;
    private static final DistanceMetric MANHATTAN = DistanceMetric.MANHATTAN;
    private static final int MIN_SHAFTS = 2;
    private static final int MIN_SPAWNERS = 3;

    private final ChunkRand rand = new ChunkRand();
    private final AncientCity ancientCity;
    private final TrialChambers trialChambers;
    private final NewStronghold stronghold;
    private final Mineshaft mineshaft;
    private final long rangeStart;
    private final long rangeEnd;

    public StructureClusterFinder(long rangeStart, long rangeEnd) {
        this.ancientCity = new AncientCity(VERSION);
        this.trialChambers = new TrialChambers(VERSION);
        this.stronghold = new NewStronghold();
        this.mineshaft = new Mineshaft(VERSION);

        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public void run() {
        for (long seed = rangeStart; seed < rangeEnd; seed++) {
            Result res = check(seed);
            if (res == null) continue;
            addResultToFiles(res);
        }

        System.out.printf("Task %d -- %d finished.\n", rangeStart, rangeEnd);
    }

    private synchronized void addResultToFiles(Result res) {
        System.out.println("Got a result: " + res);
        SeedList oneResult = new SeedList(SeedList.EntryFormat.SEED, SeedList.EntryFormat.CHUNK_POS);
        oneResult.addEntry(List.of(res.seed(), (long)res.pos().getX(), (long)res.pos().getZ()));
        oneResult.appendToFile(FULL_RESULTS_FILENAME);
        oneResult.toFlatStructureSeedList().extendWithSisterSeeds(16000).appendToFile(SEED_RESULTS_FILENAME);
    }

    // ---------------------------------------------------------------------

    /**
     * Checks if the given worldseed has a valid structure cluster.
     * @return a Result object containing the worldseed and the center of the cluster, or null if no valid cluster was found.
     */
    private Result check(long worldseed) {
        // find first ring strongholds,
        // find ancient city and trial chamber in the corresponding region(s)
        // then check if any two structures are further than 2 chunks chebyshev from eachother
        // then check number of mineshafts within 2 chunks chebyshev (has to be >= MIN_SHAFTS)

        CPos[] firstRingSH = stronghold.getFirstRingApproxStarts(worldseed, rand);

        for (CPos sh : firstRingSH) {
            // firstly, check if the initial stronghold position is close enough to a trial chambers and an ancient city
            RPos tcRegion = sh.toRegionPos(trialChambers.getSpacing());
            CPos tc = trialChambers.getInRegion(worldseed, tcRegion.getX(), tcRegion.getZ(), rand);

            RPos acRegion = sh.toRegionPos(ancientCity.getSpacing());
            CPos ac = ancientCity.getInRegion(worldseed, acRegion.getX(), acRegion.getZ(), rand);
            if (ac.distanceTo(tc, CHEBYSHEV) > 3 || ac.distanceTo(tc, MANHATTAN) <= 3) continue; // too close = bad TC biome
            if (ac.distanceTo(sh, CHEBYSHEV) > 3 || tc.distanceTo(sh, CHEBYSHEV) > 3) continue;

            // got good cluster of ancient city, trial chambers, and (potentially) stronghold

            // find the chunk position of the trial chambers' central intersection piece
            rand.setCarverSeed(worldseed, tc.getX(), tc.getZ(), VERSION);
            rand.nextInt(21); // y value
            Vec3i rot = rand.getRandom(BlockRotation.values()).getDirection().getVector();
            CPos center = new CPos(tc.getX() + rot.getX() * 2, tc.getZ() + rot.getZ() * 2);
            if (center.distanceTo(ac, CHEBYSHEV) > 2) continue; // want the center to be close to the ancient city

            List<CPos> shafts = getMineshaftsAround(worldseed, center);
            if (shafts.size() < MIN_SHAFTS) continue; // requiring at least MIN_SHAFTS mineshafts

            // check layout-specific parameters (requires access to critical section)
            if (!StructureClusterFinder.layoutCheck(worldseed, tc, ac, shafts, rand))
                continue;

            return new Result(worldseed, center);
        }

        return null;
    }

    /**
     * Returns a list of mineshafts that generate in a 7x7 chunk area around the given position (pos) on the given seed.
     */
    private List<CPos> getMineshaftsAround(long worldseed, CPos pos) {
        ArrayList<CPos> ms = new ArrayList<>();

        for (int dcx = -3; dcx <= 3; dcx++) {
            for (int dcz = -3; dcz <= 3; dcz++) {
                if (mineshaft.canStart(mineshaft.at(pos.getX() + dcx, pos.getZ() + dcz), worldseed, rand))
                    ms.add(new CPos(pos.getX() + dcx, pos.getZ() + dcz));
            }
        }

        return ms;
    }


    private static final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private static final AncientCityGenerator acgen = new AncientCityGenerator();
    private static final MineshaftLoot mgen = new MineshaftLoot(VERSION);

    /**
     * Checks if the generated trial chambers structure intersects with the central ancient city piece,
     * and if the generated mineshafts produce at least MIN_SPAWNERS spider spawners inside the
     * trial chambers' atrium.
     */
    private static synchronized boolean layoutCheck(long worldseed, CPos tc, CPos ac, List<CPos> mineshafts, ChunkRand rand) {
        // critical section, uses non-thread-safe structure generators
        tcgen.generate(worldseed, tc.getX(), tc.getZ(), rand);
        var atriumPiece = tcgen.getPieces().stream().filter(p -> p.getName().equals("corridor/atrium_1")).toList();
        if (atriumPiece.isEmpty())
            return false; // should never happen

        long spawners = 0L;
        for (CPos ms : mineshafts) {
            mgen.generateMineshaft(worldseed, ms, false);
            spawners += mgen.getCorridors().stream()
                    .filter(c -> c.hasCobwebs)
                    .filter(c -> atriumPiece.get(0).box.contains(c.boundingBox.getCenter()))
                    .count();
        }
        if (spawners < (long)MIN_SPAWNERS)
            return false; // not enough spawners

        acgen.generate(worldseed, ac.getX(), ac.getZ(), rand);
        var centerPiece = acgen.pieces[0];
        return centerPiece.box.intersects(atriumPiece.get(0).box);
    }

    // ---------------------------------------------------------------------

    private record Result(long seed, CPos pos) {}
}

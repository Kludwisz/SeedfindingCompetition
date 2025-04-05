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
import kludwisz.data.SeedList;
import kludwisz.structure.TrialChambers;

import java.util.List;

public class StructureClusterFinder implements Runnable {
    public static final String FULL_RESULTS_FILENAME = "src/main/resources/day6_full.txt";
    public static final String SEED_RESULTS_FILENAME = "src/main/resources/day6_seeds.txt";

    private static final MCVersion VERSION = MCVersion.v1_21;
    private static final DistanceMetric CHEBYSHEV = DistanceMetric.CHEBYSHEV;
    private static final DistanceMetric MANHATTAN = DistanceMetric.MANHATTAN;
    private static final int MIN_SHAFTS = 3;

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

        System.out.printf("Task %d -- %d finished.", rangeStart, rangeEnd);
    }

    private synchronized void addResultToFiles(Result res) {
        SeedList oneResult = new SeedList(SeedList.EntryFormat.SEED, SeedList.EntryFormat.CHUNK_POS);
        oneResult.addEntry(List.of(res.seed(), (long)res.pos().getX(), (long)res.pos().getZ()));
        oneResult.appendToFile(FULL_RESULTS_FILENAME);
        oneResult.toFlatList().appendToFile(SEED_RESULTS_FILENAME);
    }

    // ---------------------------------------------------------------------

    private Result check(long worldseed) {
        // find first ring strongholds,
        // find ancient city and trial chamber in the corresponding region(s)
        // then check if any two structures are further than 2 chunks chebyshev from eachother
        // then check number of mineshafts within 2 chunks chebyshev (has to be >= MIN_SHAFTS)

        CPos[] firstRingSH = stronghold.getFirstRingApproxStarts(worldseed, rand);

        for (CPos sh : firstRingSH) {
            RPos tcRegion = sh.toRegionPos(trialChambers.getSpacing());
            CPos tc = trialChambers.getInRegion(worldseed, tcRegion.getX(), tcRegion.getZ(), rand);

            RPos acRegion = sh.toRegionPos(ancientCity.getSpacing());
            CPos ac = ancientCity.getInRegion(worldseed, acRegion.getX(), acRegion.getZ(), rand);
            if (ac.distanceTo(tc, CHEBYSHEV) > 3 || ac.distanceTo(tc, MANHATTAN) <= 3) continue; // too close = no tc
            if (ac.distanceTo(sh, CHEBYSHEV) > 3 || tc.distanceTo(sh, CHEBYSHEV) > 3) continue;

            // got good cluster of ancient city, trial chambers, and (potentially) stronghold

            rand.setCarverSeed(worldseed, tc.getX(), tc.getZ(), VERSION);
            rand.nextInt(21); // y value
            Vec3i rot = rand.getRandom(BlockRotation.values()).getDirection().getVector();
            CPos center = new CPos(tc.getX() + rot.getX() * 2, tc.getZ() + rot.getZ() * 2);
            if (center.distanceTo(ac, CHEBYSHEV) > 2) continue;
            int mineshaftCount = getMineshaftsAround(worldseed, center);
            if (mineshaftCount < MIN_SHAFTS) continue;

            // check layout-specific parameters (requires access to critical section)
            if (!StructureClusterFinder.layoutCheck(worldseed, tc, ac, rand))
                continue;

            return new Result(worldseed, center);
        }

        return null;
    }

    private int getMineshaftsAround(long worldseed, CPos sh) {
        // offset by 2 chunks in each direction from stronghold
        int mineshafts = 0;

        for (int dcx = -3; dcx <= 3; dcx++) {
            for (int dcz = -3; dcz <= 3; dcz++) {
                if (mineshaft.canStart(mineshaft.at(sh.getX() + dcx, sh.getZ() + dcz), worldseed, rand))
                    mineshafts++;
            }
        }

        return mineshafts;
    }

    // critical section, uses non-thread-safe structure generators
    private static synchronized boolean layoutCheck(long worldseed, CPos tc, CPos ac, ChunkRand rand) {

        return true;
    }

    // ---------------------------------------------------------------------

    private record Result(long seed, CPos pos) {}
}

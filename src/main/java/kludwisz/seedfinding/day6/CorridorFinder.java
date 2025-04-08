package kludwisz.seedfinding.day6;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcreversal.ChunkRandomReverser;
import kludwisz.ancientcity.AncientCity;
import kludwisz.ancientcity.AncientCityGenerator;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.mineshafts.MineshaftLoot;
import kludwisz.structure.TrialChambers;

import java.util.ArrayList;


public class CorridorFinder {
    //private static final long carverSeed = 280238318665164L; // mineshaft with 5 spider corridors going 50 blocks -z

    private static final long carverSeed = 156076060682621L; // mineshaft with 4 spider corridors going ~30 blocks -z
    // this carver also guarantees correct rotation and y value of trial chamber

    private static final MCVersion VERSION = MCVersion.v1_21;
    private static final TrialChambers chambers = new TrialChambers(VERSION);
    private static final AncientCity city = new AncientCity(VERSION);
    private static final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private static final AncientCityGenerator acgen = new AncientCityGenerator();
    private static final MineshaftLoot mgen = new MineshaftLoot(VERSION);
    private static final ChunkRand rand = new ChunkRand();

    public static final ArrayList<Result> results = new ArrayList<>();

    public static void main(String[] args) {
        final int R = 100;
        final CPos min = new CPos(-R, -R);
        final CPos max = new CPos(R, R);
        find(min, max);
    }

    private static void find(CPos min, CPos max) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                final CPos pos = new CPos(x, z);
                ChunkRandomReverser.reverseCarverSeed(carverSeed, x, z, MCVersion.v1_16_1)
                        .forEach(seed -> processStructureSeed(seed, pos));
            }
        }
    }

    private static void processStructureSeed(long seed, CPos pos) {
        // get trial chamber and ancient city in the same region as out chunk
        RPos tcRegion = pos.toRegionPos(chambers.getSpacing());
        RPos acRegion = pos.toRegionPos(city.getSpacing());
        CPos tc = chambers.getInRegion(seed, tcRegion.getX(), tcRegion.getZ(), rand);
        if (tc.distanceTo(pos, DistanceMetric.MANHATTAN) != 0) return; // have to be the same chunk here
//        CPos ac = city.getInRegion(seed, acRegion.getX(), acRegion.getZ(), rand);
//        System.out.println("here0");

//        // check if both close enough to chunk and that the tc is not too close to the ac
//        if (tc.distanceTo(ac, DistanceMetric.CHEBYSHEV) > 3) return;
//        if (tc.distanceTo(ac, DistanceMetric.MANHATTAN) <= 1) return;
//        System.out.println("here1");

//        // check if trial chambers y height is between -35 and -37 and if it has good rotation
//        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
//        int yval = rand.nextInt(21) - 41;
//        if (yval < -37 || yval > -35) return;
//        if (!rand.getRandom(BlockRotation.values()).equals(BlockRotation.NONE)) return;

//        System.out.println("here2");

//        // count how many spider corridors intersect trial chamber pieces
//        tcgen.generate(seed, tc.getX(), tc.getZ(), rand);
//        mgen.generateMineshaft(seed, pos, false);
//
//        List<MineshaftGenerator.MineshaftCorridor> spiderCorridors = mgen.getCorridors().stream()
//                .filter(c -> c.hasCobwebs).toList();
//
//        int spiderCount = 0;
//        for (var corr : spiderCorridors) {
//            if (tcgen.getPieces().stream().anyMatch(p -> p.box.contains(corr.boundingBox.getCenter())))
//                spiderCount++;
//        }
//        if (spiderCount != 4)
//            return;

//        // check if ancient city main piece intersects with trial chamber pieces
//        acgen.generate(seed, ac.getX(), ac.getZ(), rand);
//        var centerPiece = acgen.pieces[0];
//        if (tcgen.getPieces().stream().noneMatch(p -> p.box.intersects(centerPiece.box)))
//            return;

        System.out.println(seed + " at " + pos.getX() + " " + pos.getZ());
        results.add(new Result(seed, pos));
    }

    public record Result(long structureSeed, CPos mineshaft) {}
}

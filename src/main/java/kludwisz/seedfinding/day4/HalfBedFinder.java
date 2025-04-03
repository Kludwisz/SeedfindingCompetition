package kludwisz.seedfinding.day4;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import kludwisz.ancientcity.AncientCity;
import kludwisz.ancientcity.AncientCityGenerator;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.structure.TrialChambers;

import java.util.ArrayList;
import java.util.List;

import static kludwisz.seedfinding.MC.*;


public class HalfBedFinder {
    private static final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private static final AncientCityGenerator acgen = new AncientCityGenerator();
    private static final AncientCity city = new AncientCity(VERSION);
    private static final TrialChambers chambers = new TrialChambers(VERSION);
    private static final ChunkRand rand = new ChunkRand();

    public static List<Result> run(long start, long end) {
        ArrayList<Result> results = new ArrayList<>();
        for (long seed = start; seed < end; seed++) {
            checkStructureSeed(seed, results);
        }
        return results;
    }

    // iteration 1 of the finder

    public static void checkStructureSeed(long seed, ArrayList<Result> results) {
        // get positions of tc and ac within the 0,0 region
        CPos tc = chambers.getInRegion(seed, 0, 0, rand);
        CPos ac = city.getInRegion(seed, 0, 0, rand);
//        if (tc.distanceTo(ac, DistanceMetric.EUCLIDEAN) < 4 || tc.distanceTo(ac, DistanceMetric.EUCLIDEAN) > 9)
//            return;
//        // not close but not that far

        // more specific params
        int dx = tc.getX() - ac.getX(); // delta goes from ac to tc
        int dz = tc.getZ() - ac.getZ();
        if ((dx != 6 && dz != -1) && (dx != -5 && dz != -3))
            return;

        // get position of bed cluster for trial chambers
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        if (pickedY + 8 != -33) // effectively, we need nextInt(21) == 0
            return;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        BPos bedVec = BedVectors.getVectorFor(startPieceRotation);
        BPos bedCluster = tc.toBlockPos(pickedY).add(bedVec);
        if (bedCluster.distanceTo(BPos.ORIGIN, DistanceMetric.CHEBYSHEV) > 190)
            return;

        // check if any of the ancient city towers generate at the correct place
        acgen.generate(seed, ac.getX(), ac.getZ(), rand);

        for (int i = 0; i < acgen.piecesLen; i++) {
            var piece = acgen.pieces[i];
            final String name = piece.getName();
            if (name.equals("structures/tall_ruin_1") || name.equals("structures/tall_ruin_3")) {
                Vec3i center = piece.box.getCenter();
                if (center.getX() == bedCluster.getX() && center.getZ() == bedCluster.getZ()) {
                    if (!checkTrialChamberLayout(seed, tc))
                        return;

                    System.out.println("Found seed with half beds: " + seed);
                    System.out.println("Bed cluster: " + bedCluster);
                    System.out.println("Ancient city tower: " + center);
                    System.out.println("Rotation: " + startPieceRotation.name());
                    System.out.println("TC: " + tc.toBlockPos(pickedY));
                    System.out.println("AC: " + ac.toBlockPos(0) + "\n");
                    results.add(new Result(seed, tc, ac));
                    return;
                }
            }
        }
    }

    private static boolean checkTrialChamberLayout(long seed, CPos tc) {
        tcgen.generate(seed, tc.getX(), tc.getZ(), rand);
        return tcgen.getPieces().stream().anyMatch(piece -> piece.getName().equals("intersection/intersection_2"));
    }

    // iteration 2 of the finder

    public static void checkStructureSeed2(long seed, ArrayList<Result> results) {
        // filter positions of tc and ac within the 0,0 and -1,0 regions
        CPos tc_00 = chambers.getInRegion(seed, 0, 0, rand);
        CPos ac_00 = city.getInRegion(seed, 0, 0, rand);

        // more specific params
        int dx = tc.getX() - ac.getX(); // delta goes from ac to tc
        int dz = tc.getZ() - ac.getZ();
        if ((dx != 6 && dz != -1) && (dx != -5 && dz != -3))
            return;

        // get position of bed cluster for trial chambers
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        if (pickedY + 8 != -33) // effectively, we need nextInt(21) == 0
            return;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        BPos bedVec = BedVectors.getVectorFor(startPieceRotation);
        BPos bedCluster = tc.toBlockPos(pickedY).add(bedVec);
        if (bedCluster.distanceTo(BPos.ORIGIN, DistanceMetric.CHEBYSHEV) > 190)
            return;

        // check if any of the ancient city towers generate at the correct place
        acgen.generate(seed, ac.getX(), ac.getZ(), rand);

        for (int i = 0; i < acgen.piecesLen; i++) {
            var piece = acgen.pieces[i];
            final String name = piece.getName();
            if (name.equals("structures/tall_ruin_1") || name.equals("structures/tall_ruin_3")) {
                Vec3i center = piece.box.getCenter();
                if (center.getX() == bedCluster.getX() && center.getZ() == bedCluster.getZ()) {
                    if (!checkTrialChamberLayout(seed, tc))
                        return;

                    System.out.println("Found seed with half beds: " + seed);
                    System.out.println("Bed cluster: " + bedCluster);
                    System.out.println("Ancient city tower: " + center);
                    System.out.println("Rotation: " + startPieceRotation.name());
                    System.out.println("TC: " + tc.toBlockPos(pickedY));
                    System.out.println("AC: " + ac.toBlockPos(0) + "\n");
                    results.add(new Result(seed, tc, ac));
                    return;
                }
            }
        }
    }

    public static void debug(long seed) {
        CPos pos = city.getInRegion(seed, 0, 0, rand);
        acgen.generate(seed, pos.getX(), pos.getZ(), rand);

    }

    public record Result(long structureSeed, CPos trialChamber, CPos ancientCity) {}
}

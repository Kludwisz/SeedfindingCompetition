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

    public static void checkStructureSeed(long seed, ArrayList<Result> results) {
        // get positions of tc and ac within the 0,0 region
        CPos tc = chambers.getInRegion(seed, 0, 0, rand);
        CPos ac = city.getInRegion(seed, 0, 0, rand);
        if (tc.distanceTo(ac, DistanceMetric.EUCLIDEAN) < 6 || tc.distanceTo(ac, DistanceMetric.EUCLIDEAN) > 10)
            return;
        // not close but not that far TODO tuning

        // get position of bed cluster for trial chambers
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        if (pickedY + 8 != -33) // effectively, we need nextInt(21) == 0
            return;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        BPos bedVec = BedVectors.getVectorFor(startPieceRotation);
        BPos bedCluster = tc.toBlockPos(pickedY).add(bedVec);

        // check if any of the ancient city towers generate at the correct place
        acgen.generate(seed, ac.getX(), ac.getZ(), rand);

        for (var piece : acgen.pieces) {
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

    public record Result(long structureSeed, CPos trialChamber, CPos ancientCity) {}
}

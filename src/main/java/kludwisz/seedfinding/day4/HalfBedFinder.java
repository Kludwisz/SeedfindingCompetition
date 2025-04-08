package kludwisz.seedfinding.day4;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.structure.Igloo;
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
    private static final NewVillage village = new NewVillage(VERSION);
    private static final Igloo igloo = new Igloo(VERSION);
    private static final ChunkRand rand = new ChunkRand();

    /**
     * All the code below is the first iteration of the quad-bed finder. It simply searches
     * for the correct configuration of a Trial Chambers and an Ancient City. This was used
     * to get an initial estimate of how rare such features were, and to find common chunk
     * offsets at which the structures can generate suitable layouts.
     */

    public static List<Result> run(long start, long end) {
        ArrayList<Result> results = new ArrayList<>();
        for (long seed = start; seed < end; seed++) {
            checkStructureSeed(seed, results);
        }
        return results;
    }

    /**
     * Checks if the given structure seed generates a Trial Chambers and an Ancient City that
     * have such layouts that a quad-bed cluster is guaranteed to generate (excluding biome checks).
     */
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

        // get position of bed cluster
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        if (pickedY + 8 != -33) // effectively, we need nextInt(21) == 0
            return;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        BPos bedVec = BedVectors.getIntersection3Vector(startPieceRotation);
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

    /**
     * Checks if the trial chamber in the given position generates the correct intersection piece - intersection_2
     */
    private static boolean checkTrialChamberLayout(long seed, CPos tc) {
        tcgen.generate(seed, tc.getX(), tc.getZ(), rand);
        return tcgen.getPieces().stream().anyMatch(piece -> piece.getName().equals("intersection/intersection_2"));
    }

    /**
     * The following code is the second iteration of the quad-bed finder. Its search parameters
     * are fine-tuned using the data gathered in the first iteration. While initially the plan
     * was to find a second Ancient City - Trial Chambers pair to get more half-beds, it turned
     * out to be too slow and rare to find within the competition's limited time. Ultimately,
     * a simple approach of checking for an igloo near a village sufficed to find seeds with
     * 5 half-beds. With some major adjustments, this kind of search could potentially yield
     * between 6 and 8 half-beds.
     */

    public static List<Result> run2(long start, long end) {
        ArrayList<Result> results = new ArrayList<>();
        for (long seed = start; seed < end; seed++) {
            checkStructureSeed2(seed, results);
        }
        return results;
    }

    /**
     * Checks the relative positions of the Trial Chambers and Ancient City using hardcoded
     * offset vectors that were found in the first iteration of the quad-bed finder.
     */
    private static boolean filterStructurePos(CPos tc, CPos ac) {
        if (tc.getX() > 15) return false;
        int dx = tc.getX() - ac.getX(); // delta goes from ac to tc
        int dz = tc.getZ() - ac.getZ();
        return (dx == 6 && dz == -1) || (dx == -5 && dz == -3);
    }

    /**
     * Returns the exact block position of the center of a 4-bed cluster assuming that
     * the Trial Chambers generates with the intersection_2 piece.
     */
    private static BPos getBedCluster(long seed, CPos tc) {
        // get position of bed cluster for trial chambers
        rand.setCarverSeed(seed, tc.getX(), tc.getZ(), VERSION);
        int pickedY = rand.nextInt(21) - 41;
        if (pickedY + 8 != -33) // effectively, we need nextInt(21) == 0
            return null;
        BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
        BPos bedVec = BedVectors.getIntersection3Vector(startPieceRotation);
        return tc.toBlockPos(pickedY).add(bedVec);
    }

    /**
     * Very similar to the first iteration, expect this time we're also looking for
     * a close village and igloo.
     */
    public static void checkStructureSeed2(long seed, ArrayList<Result> results) {
        // filter positions of tc and ac within the 0,0 region for the hard cluster
        CPos tc_00 = chambers.getInRegion(seed, 0, 0, rand);
        CPos ac_00 = city.getInRegion(seed, 0, 0, rand);
        if (!filterStructurePos(tc_00, ac_00))
            return;

        // filter positions for igloo near ac + tc and village nearby
        CPos ig = igloo.getInRegion(seed, 0, 0, rand);
        if (ig.distanceTo(CPos.ZERO, DistanceMetric.CHEBYSHEV) > 16)
            return;
        CPos vill = village.getInRegion(seed, 0, 0, rand);
        if (vill.distanceTo(ig, DistanceMetric.MANHATTAN) >= 4)
            return;

        BPos cluster1 = getBedCluster(seed, tc_00);
        if (cluster1 == null)
            return;

//        // filter positions inside -1, 0 for arbitrary close trial chambers and ancient city
//        CPos tc_10 = chambers.getInRegion(seed, -1, 0, rand);
//        if (tc_10.distanceTo(tc_00, DistanceMetric.CHEBYSHEV) > 22)
//            return;
//        CPos ac_10 = city.getInRegion(seed, -1, 0, rand);
//        if (tc_10.distanceTo(ac_10, DistanceMetric.EUCLIDEAN) < 3 || tc_10.distanceTo(ac_10, DistanceMetric.EUCLIDEAN) > 7)
//            return;

        // now comes the hard part. need to filter city layout for the hard cluster and just check if
        // two beds intersect any ancient city piece for the second one
        acgen.generate(seed, ac_00.getX(), ac_00.getZ(), rand);
        boolean correctLayout = false;
        for (int i = 0; i < acgen.piecesLen; i++) {
            var piece = acgen.pieces[i];
            final String name = piece.getName();

            // these two structures always generate a hashtag-like roof pattern which, when intersecting with
            // the Trial Chambers bed cluster, alwyas produces 4 half-beds.
            if (name.equals("structures/tall_ruin_1") || name.equals("structures/tall_ruin_3")) {
                Vec3i center = piece.box.getCenter();
                if (center.getX() == cluster1.getX() && center.getZ() == cluster1.getZ()) {
                    correctLayout = checkTrialChamberLayout(seed, tc_00);
                    break;
                }
            }
        }
        if (!correctLayout)
            return;

//        // generate both the second trial chambers and the second ancient city
//        tcgen.generate(seed, tc_10.getX(), tc_10.getZ(), rand);
//        acgen.generate(seed, ac_10.getX(), ac_10.getZ(), rand);
//
//        // check for beds intersecting tall ac pieces
//        List<TrialChambersPieces.Piece> beds = tcgen.getPieces().stream()
//                .filter(piece -> piece.getName().contains("bed"))
//                .toList();
//
//        int intersections = 0;
//        for (int i = 0; i < acgen.piecesLen; i++) {
//            var piece = acgen.pieces[i];
//            final String name = piece.getName();
//            if (name.equals("structures/tall_ruin_1") || name.equals("structures/tall_ruin_3") || name.contains("city_center")) {
//                for (TrialChambersPieces.Piece bed : beds) {
//                    if (bed.box.intersects(piece.box)) {
//                        intersections++;
//                    }
//                }
//            }
//        }
//
//        System.out.println("reached isection check: " + intersections + " for seed: " + seed);
//        if (intersections < 2)
//            return;

        System.out.println("Found candidate seed: " + seed);
        System.out.println("Bed cluster: " + cluster1);
        results.add(new Result(seed, tc_00, ac_00));
    }

    public record Result(long structureSeed, CPos trialChamber, CPos ancientCity) {}
}

package kludwisz.seedfinding.day1;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.ancientcity.AncientCity;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.mineshafts.MineshaftLoot;
import kludwisz.structure.TrialChambers;

import java.util.ArrayList;
import java.util.List;


public class FirstFilter implements Runnable {
    private final long worldseed;
    private final RPos regionMin;
    private final RPos regionMax;

    public FirstFilter(long worldseed, RPos regionMin, RPos regionMax) {
        this.worldseed = worldseed;
        this.regionMin = regionMin;
        this.regionMax = regionMax;
    }


    @Override
    public void run() {
        // iterate over region coords
        for (int rx = regionMin.getX(); rx <= regionMax.getX(); rx++) {
            for (int rz = regionMin.getZ(); rz <= regionMax.getZ(); rz++) {
                if (this.testRegion(rx, rz)) {
                    ResultCollector.addResult(new Result(rx, rz));
                }
//                BPos clusterCenter = this.testRegion2(rx, rz);
//                if (clusterCenter != null) {
//                    ResultCollector.addResult2(new Result2(clusterCenter));
//                }
            }
        }
        System.out.printf("Task #%d finished.\n", Day1.completed.incrementAndGet());
    }

    private final MCVersion version = MCVersion.v1_21;
    private final TrialChambers TC = new TrialChambers(version);
    private final AncientCity AC = new AncientCity(version);
    private final ChunkRand rand = new ChunkRand();
    private final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private final MineshaftLoot mgen = new MineshaftLoot(version);

    private boolean testRegion(int regionX, int regionZ) {
        CPos chambers = TC.getInRegion(worldseed, regionX, regionZ, rand);
        rand.setCarverSeed(worldseed, chambers.getX(), chambers.getZ(), version);
        rand.nextInt(21); // y value
        Vec3i startPieceRotationVector = rand.getRandom(BlockRotation.values()).getDirection().getVector();
        CPos center = new CPos(chambers.getX() + startPieceRotationVector.getX() * 2, chambers.getZ() + startPieceRotationVector.getZ() * 2);

//        RPos acr = chambers.toRegionPos(AC.getSpacing());
//        CPos ac = AC.getInRegion(worldseed, acr.getX(), acr.getZ(), rand);
//        if (ac.distanceTo(chambers, DistanceMetric.CHEBYSHEV) > 3 || ac.distanceTo(chambers, DistanceMetric.EUCLIDEAN) <= 2)
//            return false;

        // check for nearby mineshaft (decently fast)
        ArrayList<CPos> mineshafts = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                rand.setCarverSeed(worldseed, center.getX()+dx, center.getZ()+dz, version);
                if (rand.nextDouble() < 0.004D) {
                    mineshafts.add(new CPos(center.getX()+dx, center.getZ()+dz));
                }
            }
        }
        if (mineshafts.size() < 3)
            return false;
        // we should have eliminated nearly 72% of seeds by now

        // generate both the trial chambers and the mineshafts and check for possible double chest arrangement
//        tcgen.generate(worldseed, chambers.getX(), chambers.getZ(), rand);
//
//        HashMap<CPos, Integer> chestCounts = new HashMap<>();
//        tcgen.getPieces().stream()
//                .filter(p -> p.getName().contains("spawner") && !p.getName().contains("connector"))
//                .map(p -> p.box.getCenter())
//                .forEach(pos -> {
//                    chestCounts.putIfAbsent(new CPos(pos.getX(), pos.getZ()), 0);
//                });

        for (CPos ms : mineshafts) {
            mgen.generateMineshaft(worldseed, ms, false);

            List<Pair<BPos, Long>> chests = mgen.getAllChests(worldseed);
            for (var c1 : chests) {
                for (var c2 : chests) {
                    if (c1.getSecond().equals(c2.getSecond()))
                        continue;
                    if (c1.getFirst().getX() == c2.getFirst().getX()
                        && c1.getFirst().getZ() == c2.getFirst().getZ()
                        && c1.getFirst().getY() <= 0
                        && Math.abs(c1.getFirst().getY() - c2.getFirst().getY()) <= 6
                    )
                        return true;
                }
            }
        }
        return false;
    }

    private BPos testRegion2(int regionX, int regionZ) {
        CPos chambers = TC.getInRegion(worldseed, regionX, regionZ, rand);

        // check for nearby mineshaft around chamber center (decently fast)
        rand.setCarverSeed(worldseed, chambers.getX(), chambers.getZ(), version);
        rand.nextInt(21); // y value
        Vec3i startPieceRotationVector = rand.getRandom(BlockRotation.values()).getDirection().getVector();
        CPos center = new CPos(chambers.getX() + startPieceRotationVector.getX() * 2, chambers.getZ() + startPieceRotationVector.getZ() * 2);

        ArrayList<CPos> mineshafts = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                rand.setCarverSeed(worldseed, center.getX()+dx, center.getZ()+dz, version);
                if (rand.nextDouble() < 0.004D) {
                    mineshafts.add(new CPos(chambers.getX()+dx, chambers.getZ()+dz));
                }
            }
        }
        if (mineshafts.size() != 1)
            return null;
        // we should have eliminated nearly 72% of seeds by now

        // generate both the trial chambers and the mineshafts and check for possible double chest arrangement
//        tcgen.generate(worldseed, chambers.getX(), chambers.getZ(), rand);
//
//        HashMap<CPos, Integer> chestCounts = new HashMap<>();
//        tcgen.getPieces().stream()
//                .filter(p -> p.getName().contains("spawner") && !p.getName().contains("connector"))
//                .map(p -> p.box.getCenter())
//                .forEach(pos -> {
//                    chestCounts.putIfAbsent(new CPos(pos.getX(), pos.getZ()), 0);
//                });

        // check if triple chest is even possible (should be a nice speedup)
//        CPos ms = mineshafts.get(0);
//        mgen.generateMineshaft(worldseed, ms, false);
//        ArrayList<BPos> possibleChests = new ArrayList<>();
//        mgen.getCorridors().stream()
//                .filter (c -> c.boundingBox.minY < 0)
//                .forEach(c -> c.addPossibleChestPositions(possibleChests));
//
//        boolean isPossible = false;
//        for (BPos centralChest : possibleChests) {
//            ArrayList<BPos> cluster = new ArrayList<>();
//            cluster.add(centralChest);
//            for (BPos chest : possibleChests) {
//                if (centralChest.getX() - chest.getX() == 0
//                        && centralChest.getZ() - chest.getZ() == 0
//                        && Math.abs(centralChest.getY() - chest.getY()) <= 12)
//                    cluster.add(chest);
//            }
//            if (cluster.size() < 3)
//                continue;
//            isPossible = true;
//            break;
//        }
//        if (!isPossible) return null;

        List<BPos> allChests = mgen.getAllChests(worldseed).stream().map(Pair::getFirst).toList();

        // try grouping the chests into clusters and check for triple chests in a cluster
        for (BPos centralChest : allChests) {
            ArrayList<BPos> cluster = new ArrayList<>();
            cluster.add(centralChest);
            for (BPos chest : allChests) {
                if (centralChest.getX() - chest.getX() == 0
                        && centralChest.getZ() - chest.getZ() == 0
                        && Math.abs(centralChest.getY() - chest.getY()) <= 12)
                    cluster.add(chest);
            }
            if (cluster.size() < 3)
                continue;

            // calculate cluster bounding box and check its span
            BlockBox bb = new BlockBox(cluster.get(0), cluster.get(0));
            for (BPos chest : cluster) {
                bb.encompass(new BlockBox(chest, chest));
            }

            if (bb.getYSpan() <= 12 && bb.getXSpan() <= 1 && bb.getZSpan() <= 1) {
                return new BPos(bb.getCenter());
            }
        }

        return null;
    }

    public record Result(long regionX, long regionZ) {}
    public record Result2(BPos pos) {}
}

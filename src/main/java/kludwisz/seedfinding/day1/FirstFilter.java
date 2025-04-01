package kludwisz.seedfinding.day1;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
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
                if (this.testRegion2(rx, rz)) { // TODO watch out! alternate method used
                    ResultCollector.addResult(new Result(rx, rz, worldseed));
                }
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
        RPos acr = chambers.toRegionPos(AC.getSpacing());
        CPos ac = AC.getInRegion(worldseed, acr.getX(), acr.getZ(), rand);
        if (ac.distanceTo(chambers, DistanceMetric.CHEBYSHEV) > 3 || ac.distanceTo(chambers, DistanceMetric.EUCLIDEAN) <= 2)
            return false;

        // check for nearby mineshaft (decently fast)
        ArrayList<CPos> mineshafts = new ArrayList<>();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                rand.setCarverSeed(worldseed, chambers.getX()+dx, chambers.getZ()+dz, version);
                if (rand.nextDouble() < 0.004D) {
                    mineshafts.add(new CPos(chambers.getX()+dx, chambers.getZ()+dz));
                }
            }
        }
        if (mineshafts.isEmpty())
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
                        && c1.getFirst().getY() <= -20
                        && Math.abs(c1.getFirst().getY() - c2.getFirst().getY()) <= 6
                    )
                        return true;
                }
            }
        }
        return false;
    }

    private boolean testRegion2(int regionX, int regionZ) {
        CPos chambers = TC.getInRegion(worldseed, regionX, regionZ, rand);

        // check for nearby mineshaft around chamber center (decently fast)
        rand.setCarverSeed(worldseed, chambers.getX(), chambers.getZ(), version);
        rand.nextInt(21); // y value
        Vec3i startPieceRotationVector = rand.getRandom(BlockRotation.values()).getDirection().getVector();
        CPos center = new CPos(chambers.getX() + startPieceRotationVector.getX(), chambers.getZ() + startPieceRotationVector.getZ());

        ArrayList<CPos> mineshafts = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                rand.setCarverSeed(worldseed, center.getX()+dx, center.getZ()+dz, version);
                if (rand.nextDouble() < 0.004D) {
                    mineshafts.add(new CPos(chambers.getX()+dx, chambers.getZ()+dz));
                }
            }
        }
        if (mineshafts.size() < 2)
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

        ArrayList<BPos> allChests = new ArrayList<>();
        for (CPos ms : mineshafts) {
            mgen.generateMineshaft(worldseed, ms, false);
            mgen.getAllChests(worldseed).stream().map(Pair::getFirst).forEach(allChests::add);
        }

        // try grouping the chests into clusters and check for 4+ chests in a cluster
        for (BPos centralChest : allChests) {
            ArrayList<BPos> cluster = new ArrayList<>();
            cluster.add(centralChest);
            for (BPos chest : allChests) {
                if (Math.abs(centralChest.getX() - chest.getX()) <= 2
                        && Math.abs(centralChest.getZ() - chest.getZ()) <= 2
                        && Math.abs(centralChest.getY() - chest.getY()) <= 12)
                    cluster.add(chest);
            }
            if (cluster.size() < 4)
                continue;

            // calculate cluster bounding box and check its span
            BlockBox bb = new BlockBox(cluster.get(0), cluster.get(0));
            for (BPos chest : cluster) {
                bb.encompass(new BlockBox(chest, chest));
            }

            if (bb.getYSpan() <= 12 && bb.getXSpan() <= 2 && bb.getZSpan() <= 2)
                return true;
        }

        return false;
    }

    public record Result(long regionX, long regionZ, long seed) {}
}

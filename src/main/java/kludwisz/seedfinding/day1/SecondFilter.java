package kludwisz.seedfinding.day1;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import kludwisz.ancientcity.AncientCity;
import kludwisz.ancientcity.AncientCityGenerator;
import kludwisz.data.Formatter;
import kludwisz.generator.TrialChambersGenerator;
import kludwisz.mineshafts.MineshaftLoot;
import kludwisz.structure.TrialChambers;
import nl.kallestruik.noisesampler.NoiseSampler;
import nl.kallestruik.noisesampler.minecraft.Dimension;

import java.util.ArrayList;
import java.util.List;

// /tp @s -19223800 -23 -12012434
// CURRENT SUB:
// /tp @p -7243315 -33 -9898617
// /tp @s -7243306.5 -34.0 -9898616.5 90.0 -18.0

// also good (2 chests right on top of a spawner):
// /tp @p 17958840 0 -6624116

/**
 * Generates the Trial Chambers structure and checks, if a cluster of minecart chests generates
 * near an interest point inside the structure.
 */
public class SecondFilter {
    private final long worldseed;
    private final NoiseSampler sampler;

    public SecondFilter(long worldseed) {
        this.worldseed = worldseed;
        this.sampler = new NoiseSampler(worldseed, Dimension.OVERWORLD);
    }

    private final MCVersion version = MCVersion.v1_21;
    private final TrialChambers TC = new TrialChambers(version);
    private final AncientCity AC = new AncientCity(version);
    private final ChunkRand rand = new ChunkRand();
    private final AncientCityGenerator acgen = new AncientCityGenerator();
    private final TrialChambersGenerator tcgen = new TrialChambersGenerator();
    private final MineshaftLoot mgen = new MineshaftLoot(version);

    /**
     * Generates the Trial Chambers structure and checks, if a cluster of minecart chests generates
     * near an interest point inside the structure.
     */
    public void testPos(RPos region) {
        // gen trial chambers
        CPos chambers = TC.getInRegion(worldseed, region.getX(), region.getZ(), rand);
        tcgen.generate(worldseed, chambers.getX(), chambers.getZ(), rand);

        rand.setCarverSeed(worldseed, chambers.getX(), chambers.getZ(), version);
        rand.nextInt(21); // y value
        Vec3i startPieceRotationVector = rand.getRandom(BlockRotation.values()).getDirection().getVector();
        CPos center = new CPos(chambers.getX() + startPieceRotationVector.getX() * 2, chambers.getZ() + startPieceRotationVector.getZ() * 2);

        // for each nearby mineshaft, generate all its chests
        ArrayList<BPos> chests = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                final CPos pos = new CPos(center.getX() + dx, center.getZ() + dz);
                rand.setCarverSeed(worldseed, pos.getX(), pos.getZ(), version);
                if (!(rand.nextDouble() < 0.004D))
                    continue;

                mgen.generateMineshaft(worldseed, pos, false);
                mgen.getAllChests(worldseed).stream().map(Pair::getFirst).forEach(chests::add);
            }
        }

        // filter out chests outside the trial chamber bounding box
        List<BPos> filteredChests = chests.stream()
                .filter(pos -> {
                    for (var piece : tcgen.pieces) {
                        if (piece.box.contains(pos)) {
                            //System.out.println(piece.pos.toImmutable());
                            return true;
                        }

                    }
                    return false;
                })
                .toList();

        // check for cluster of at least 4 chests within the bounding box
//        for (BPos centralChest : filteredChests) {
//            ArrayList<BPos> cluster = new ArrayList<>();
//            cluster.add(centralChest);
//            for (BPos chest : filteredChests) {
//                if (Math.abs(centralChest.getX() - chest.getX()) <= 2
//                        && Math.abs(centralChest.getZ() - chest.getZ()) <= 2
//                        && Math.abs(centralChest.getY() - chest.getY()) <= 12) {
//                    for (var piece : tcgen.pieces) {
//                        if (piece.box.contains(chest)) {
//                            cluster.add(chest);
//                            break;
//                        }
//                    }
//                }
//
//            }
//            if (cluster.size() < 4)
//                continue;
//
//            // calculate cluster bounding box and check its span
//            BlockBox bb = new BlockBox(cluster.get(0), cluster.get(0));
//            for (BPos chest : cluster) {
//                bb.encompass(new BlockBox(chest, chest));
//            }
//
//            if (bb.getYSpan() > 12 || bb.getXSpan() > 2 || bb.getZSpan() > 2)
//                continue;
//
//            System.out.println("Seed: " + worldseed + ", pos: " + Formatter.tpCommand(centralChest));
//        }

        // if there's a double chest arrangement, check if it's near a trial spawner or vault
        for (var c1 : filteredChests) {
            for (var c2 : filteredChests) {
                if (c1.equals(c2))
                    continue;
                if (c1.getX() != c2.getX() || c1.getZ() != c2.getZ())
                    continue;

                for (var piece : tcgen.pieces) {
                    boolean isSpawner = piece.getName().contains("spawner") && !piece.getName().contains("connector");
                    if (isSpawner | piece.getName().contains("vault")) {
                        if (Math.abs(c1.getX() - piece.box.getCenter().getX()) <= 1
                                && Math.abs(c1.getZ() - piece.box.getCenter().getZ()) <= 1
                                && c1.getY() >= piece.box.getCenter().getY()
                                && c2.getY() >= piece.box.getCenter().getY()) {
                            System.out.println(Formatter.tpCommand(c1));
                            ResultCollector.addResult2(new FirstFilter.Result2(c1));
                            break;
                        }
                    }
                }
            }
        }
    }
}

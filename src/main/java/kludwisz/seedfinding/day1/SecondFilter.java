package kludwisz.seedfinding.day1;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
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
// /tp @p -7243316 -32 -9898618

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

    public void testPos(RPos region) {
        CPos chambers = TC.getInRegion(worldseed, region.getX(), region.getZ(), rand);
        RPos acr = chambers.toRegionPos(AC.getSpacing());
        //CPos city = AC.getInRegion(worldseed, acr.getX(), acr.getZ(), rand);
        //if (city.distanceTo(chambers, DistanceMetric.EUCLIDEAN) > 2)
        //    return;
        //acgen.generate(worldseed, city.getX(), city.getZ(), rand);

        // test biome using noise sampler
//        Vec3i centerOfFirstPiece = acgen.pieces[0].box.getCenter();
//        Map<NoiseType, Double> noise = sampler.queryNoiseFromBlockPos(centerOfFirstPiece.getX(), -27 >> 2, centerOfFirstPiece.getZ(), NoiseType.EROSION, NoiseType.DEPTH);
//        double D = noise.get(NoiseType.DEPTH);
//        double E = noise.get(NoiseType.EROSION);
//        double dD = 1.1 - D;
//        double dE = Math.max(E + 0.375, 0);
//        double dsD = Math.max(D - 1.0, 0);
//        if (!(dD * dD + dE * dE < dsD * dsD)) return;

        // gen trial chambers
        tcgen.generate(worldseed, chambers.getX(), chambers.getZ(), rand);

        // for each nearby mineshaft, generate all its chests
        ArrayList<BPos> chests = new ArrayList<>();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                final CPos pos = new CPos(chambers.getX() + dx, chambers.getZ() + dz);
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

        // if there's a double chest arrangement, check if it's near a trial spawner
        for (var c1 : filteredChests) {
            for (var c2 : filteredChests) {
                if (c1.equals(c2))
                    continue;
                if (c1.getX() != c2.getX() || c1.getZ() != c2.getZ())
                    continue;

                for (var piece : tcgen.pieces) {
                    if (piece.getName().contains("spawner") && !piece.getName().contains("connector")) {
                        if (Math.abs(c1.getX() - piece.box.getCenter().getX()) <= 1
                                && Math.abs(c1.getZ() - piece.box.getCenter().getZ()) <= 1
                                && c1.getY() >= piece.box.getCenter().getY()
                                && c2.getY() >= piece.box.getCenter().getY()) {
                            System.out.println("Seed: " + worldseed + ", position: " + Formatter.tpCommand(c1));
                            break;
                        }
                    }
                }

//                if (c1.getX() == c2.getX() && c1.getZ() == c2.getZ() && c1.getY() <= -10 && Math.abs(c1.getY() - c2.getY()) <= 12) {
//                    System.out.println("Seed: " + worldseed + ", position: " + Formatter.tpCommand(c1));
//                }
            }
        }
    }
}

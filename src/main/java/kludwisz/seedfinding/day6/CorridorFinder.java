package kludwisz.seedfinding.day6;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcreversal.ChunkRandomReverser;
import kludwisz.structure.TrialChambers;


public class CorridorFinder {
    // (unused carver seed)
    // mineshaft with 5 spider corridors going 50 blocks -z
    //private static final long carverSeed = 280238318665164L;

    // mineshaft with 4 spider corridors going ~30 blocks -z (found using mineshaft_seed.cu)
    // this carver also guarantees correct rotation and y value of trial chambers structure
    private static final long carverSeed = 156076060682621L;

    private static final MCVersion VERSION = MCVersion.v1_21;
    private static final TrialChambers chambers = new TrialChambers(VERSION);
    private static final ChunkRand rand = new ChunkRand();

    public static void main(String[] args) {
        final int R = 100; // chunk radius for reversal (increase to find more seeds)
        final CPos min = new CPos(-R, -R);
        final CPos max = new CPos(R, R);
        find(min, max);
        /*
        Results:
        52229211828520 at -100 -25
        274255713967562 at -98 -65
        228975362430510 at -97 2
        121407718066671 at -91 -48
        276991878981568 at -86 -19
        228236602011021 at -84 13
        177306134424087 at -82 -94
        45917456115375 at -66 -94
        49401012270806 at -63 -65
        260198880235285 at -60 12
        246889517271943 at -50 3
        79380669263432 at -49 47
        46079709989246 at -48 39
        109538467197219 at -25 -13
        86219251661527 at -24 -21
        45828130136875 at -21 -67
        150371542757934 at -21 -23
        227247435258593 at -20 -89
        36931789415161 at 4 37
        108092214921722 at 9 34
        264153039978823 at 15 55
        242147000796217 at 17 82
        238898971770365 at 17 89
        171625553542595 at 20 86
        114961093554751 at 34 -51
        170000051093024 at 34 17
        197544181144769 at 41 20
        254749293992441 at 44 0
        100480072230466 at 45 -67
        258063973868584 at 52 85
        33772829873388 at 55 42
        163095287199502 at 73 -32
        72204711235285 at 80 12
        129837284729247 at 86 -26
        5481990358167 at 87 -51
        214856885337321 at 89 -51
         */
    }

    /**
     * Reverses the carver seed into structure seeds in a given range of chunks. Each structure seed gets
     * processed to check if a trial chambers structure generates in the same chunk the carver seed was reversed for.
     * @param min the minimum chunk position
     * @param max the maximum chunk position
     */
    private static void find(CPos min, CPos max) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                final CPos pos = new CPos(x, z);
                ChunkRandomReverser.reverseCarverSeed(carverSeed, x, z, MCVersion.v1_16_1)
                        .forEach(seed -> processStructureSeed(seed, pos));
            }
        }
    }

    /**
     * Checks if a trial chambers structure generates in chunk "pos" in the given structure seed.
     */
    private static void processStructureSeed(long seed, CPos pos) {
        RPos tcRegion = pos.toRegionPos(chambers.getSpacing());
        CPos tc = chambers.getInRegion(seed, tcRegion.getX(), tcRegion.getZ(), rand);
        if (tc.distanceTo(pos, DistanceMetric.MANHATTAN) != 0)
            return; // have to be the same chunk here

        System.out.println(seed + " at " + pos.getX() + " " + pos.getZ());
    }
}

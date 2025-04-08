package kludwisz.seedfinding.day6;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import com.seedfinding.latticg.util.LCG;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcreversal.ChunkRandomReverser;


public class MineshaftStateReverser {
    private static final ChunkRand rand = new ChunkRand();

    public static void main(String[] args) {
//        runlatti();

//        //260676566349164
//        carverToStructseed(260676566349164L);

//        //78333037866579
//        //233985236196149
//        //280238318665164
//        carverToStructseed(78333037866579L);
//        carverToStructseed(233985236196149L);
//        carverToStructseed(280238318665164L);

        // x = 9 with tc going -z (north) or x = 7 with tc going +z (south)
        long[] seeds = {
                184792755286472L,
                56796013046194L,
                209228314985727L,
                105138984772094L,
                196915658364540L,
                97673312491187L,
                220600925108411L,
                214846931305250L,
                224259084761669L,
                175418686875215L,
                195484993842107L,
                112003835668481L,
                15968538881165L,
                23830961987130L,
                132954291726523L,
                128168016663458L,
                143319963638928L,
                14549237075009L,
                157931018468964L,
                135991633058154L,
                24491911484857L,
                235759564352031L
        };

        for (long seed : seeds) {
            carverToStructseed(seed);
        }
    }

    /**
     * This is the initial attempt of finding Mineshaft carver seeds using the LattiCG library.
     * As it turned out, we needed a much more common seed than the ones found via LattiCG, and
     * so we switched to a CUDA bruteforce approach, greatly reducing code runtime.
     */
    @Deprecated
    public static void runlatti() {
        int consecutiveCount = 5;
        DynamicProgram device = DynamicProgram.create(LCG.JAVA);

        device.add(JavaCalls.nextDouble().betweenIE(0.0D, 0.004D));
        device.skip(3); // actually there's 3 calls but we have already "used" 2 calls in the nextDouble
        device.skip(2); // next jigsaw generation
        for (int i=0; i<consecutiveCount; i++) {
            device.skip(1); // faster and still good since chance of getting corridor is 70%
            //device.add(JavaCalls.nextInt(100).betweenIE(0, 70));
            device.skip(1); // getBoundingBox
            device.filteredSkip(r -> r.nextInt(3) != 0, 1);
            device.add(JavaCalls.nextInt(23).equalTo(0));  	  // spider corridor
            //device.skip(1); // next in any direction
            device.add(JavaCalls.nextInt(4).betweenII(0, 1)); // next generated north
            device.skip(1); // height
        }
        device.reverse().forEach(xoredCarverSeed ->{
            long carver = xoredCarverSeed ^ LCG.JAVA.multiplier;
            if (test(carver, consecutiveCount))
                System.out.println(carver);
        });
    }

    /**
     * Forward-checks a carver seed to see if it generates the target number of consecutive spider corridors.
     */
    @Deprecated
    private static boolean test(long carverseed, int consecutiveCount) {
        rand.setSeed(carverseed);
        if (rand.nextDouble() >= 0.004D) return false;
        //rand.setSeed(carverseed);

        rand.advance(5);
        for (int i=0; i<consecutiveCount; i++) {
            if (rand.nextInt(100) >= 70) return false;
            if (rand.nextInt(3) == 2) return false; // not that long
            if (rand.nextInt(3) == 0) return false;
            if (rand.nextInt(23) != 0) return false;
            if (rand.nextInt(4) > 1) return false;
            rand.advance(1); // height offset doesn't matter
        }

        return true;
    }

    /**
     * Reverses the given carver seed to the first found worldseed that generate a chunk with that carver near 0,0.
     * This is actually overkill because the structure seed "carverSeed" always has that carver seed in chunk (0,0).
     */
    private static void carverToStructseed(long carver) {
        for (int cx=-2; cx<=2; cx++) for (int cz=-2; cz<=2; cz++){
            for (long worldseed : ChunkRandomReverser.reverseCarverSeed(carver, cx, cz, MCVersion.v1_16_1)) {
                System.out.println(worldseed);
                return;
            }
        }
    }
}

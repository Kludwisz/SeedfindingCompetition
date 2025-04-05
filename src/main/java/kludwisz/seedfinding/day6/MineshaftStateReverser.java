package kludwisz.seedfinding.day6;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import com.seedfinding.latticg.util.LCG;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcreversal.ChunkRandomReverser;

public class MineshaftStateReverser {
    public static void main(String[] args) {
        // 260676566349164
        //carverToStructseed(260676566349164L);
        //runlatti();

//        78333037866579
//        233985236196149
//        280238318665164

        carverToStructseed(78333037866579L);
        carverToStructseed(233985236196149L);
        carverToStructseed(280238318665164L);
    }

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

    private static final ChunkRand rand = new ChunkRand();
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
            rand.advance(1); // height
        }

        return true;
    }

    private static void carverToStructseed(long carver) {
        for (int cx=-4; cx<=4; cx++) for (int cz=-4; cz<=4; cz++){
            for (long worldseed : ChunkRandomReverser.reverseCarverSeed(carver, cx, cz, MCVersion.v1_16_1)) {
                System.out.println(worldseed);
                return;
            }
        }
    }
}

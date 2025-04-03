package kludwisz.seedfinding.day4;

import com.seedfinding.mccore.util.block.BlockRotation;
import com.seedfinding.mccore.util.pos.BPos;

import java.util.EnumMap;


public class BedVectors {
    public static final EnumMap<BlockRotation, BPos> VECS = new EnumMap<>(BlockRotation.class);

    public static BPos getVectorFor(BlockRotation rotation) {
        return VECS.get(rotation);
    }

    static {
        VECS.put(BlockRotation.NONE,
                // Pos{x=3424, y=-31, z=288}
                // /setblock 3428 -23 259 minecraft:deepslate_tiles
                new BPos(4, 8, -29) // 3428 - 3424 = 4, -31 - (-23) = 8, 288 - 259 = -29
        );
        VECS.put(BlockRotation.CLOCKWISE_90,
                // Pos{x=-208, y=-27, z=-224}
                // /setblock -179 -19 -220 minecraft:deepslate_tiles
                // the vector is the difference between the two positions
                new BPos(29, 8, 4) // -179 - (-208) = 29, -27 - (-19) = 8, -224 - (-220) = 4
        );
        VECS.put(BlockRotation.CLOCKWISE_180,
                // Pos{x=1664, y=-26, z=48}
                // /setblock 1660 -18 77 minecraft:deepslate_tiles
                new BPos(-4, 8, 29) // 1660 - 1664 = -4, -26 - (-18) = 8, 48 - 77 = 29
        );
        VECS.put(BlockRotation.COUNTERCLOCKWISE_90,
                // Pos{x=9984, y=-31, z=544}
                // /setblock 9955 -23 540 minecraft:deepslate_tiles
                new BPos(-29, 8, -4) // 9955 - 9984 = -29, -31 - (-23) = 8, 544 - 540 = -4
        );
    }

//    public static void main(String[] args) {
//        long seed = -1520902709784950966L;
//
//        ChunkRand rand = new ChunkRand();
//        TrialChambers tc = new TrialChambers(VERSION);
//        TrialChambersGenerator tcgen = new TrialChambersGenerator();
//
//        for (int i = 3; i < 20; i++) {
//            CPos chunk = tc.getInRegion(seed, i, 1, rand);
//
//            rand.setCarverSeed(seed, chunk.getX(), chunk.getZ(), MCVersion.v1_21);
//            int pickedY = rand.nextInt(21) - 41;
//            BlockRotation startPieceRotation = rand.getRandom(BlockRotation.values());
//            System.out.println("y = " + pickedY + " rotation = " + startPieceRotation.name());
//            System.out.println(chunk.toBlockPos(pickedY));
//            System.out.println(Formatter.tpCommand(chunk.toBlockPos(pickedY)));
//        }
//
//    }
}

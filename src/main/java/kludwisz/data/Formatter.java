package kludwisz.data;

import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.CPos;

@SuppressWarnings("unused")
public class Formatter {
    public static String tpCommand(CPos chunkPos, int y, Dimension dimension) {
        return String.format("/execute in minecraft:%s run tp @p %d %d %d", dimension.getName(), chunkPos.getX() * 16, y, chunkPos.getZ() * 16);
    }

    public static String tpCommand(Vec3i pos, Dimension dimension) {
        return String.format("/execute in minecraft:%s run tp @p %d %d %d", dimension.getName(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static String tpCommand(Vec3i pos) {
        return String.format("/tp @p %d %d %d", pos.getX(), pos.getY(), pos.getZ());
    }
}

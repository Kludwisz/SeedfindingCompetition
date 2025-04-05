package kludwisz.seedfinding.day6;

import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.Stronghold;
import com.seedfinding.mcseed.rand.JRand;

public class NewStronghold {
    private final Stronghold stronghold = new Stronghold(MCVersion.v1_21);

    public CPos[] getFirstRingApproxStarts(long worldseed, JRand rand) {
        final int distance = stronghold.getDistance();
        final int numberPerRing = stronghold.getSpread();
        final int count = 3;

        CPos[] starts = new CPos[count];
        rand.setSeed(worldseed);

        double angle = rand.nextDouble() * Math.PI * 2.0D;

        for(int idx = 0; idx < count; ++idx) {
            double distanceRing = (4.0D * distance) + (rand.nextDouble() - 0.5D) * (double)distance * 2.5D;
            int chunkX = (int)Math.round(Math.cos(angle) * distanceRing);
            int chunkZ = (int)Math.round(Math.sin(angle) * distanceRing);
            starts[idx] = new CPos(chunkX, chunkZ);
            angle += Math.PI * 2.0D / (double)numberPerRing;
        }

        return starts;
    }
}

package kludwisz.seedfinding.day4;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mccore.version.VersionMap;
import com.seedfinding.mcfeature.structure.*;

public class NewVillage extends UniformStructure<NewVillage> {
    public static final VersionMap<RegionStructure.Config> CONFIGS;

    public NewVillage(MCVersion version) {
        this(CONFIGS.getAsOf(version), version);
    }

    public NewVillage(RegionStructure.Config config, MCVersion version) {
        super(config, version);
    }

    public static String name() {
        return "village";
    }

    @Override
    public Dimension getValidDimension() {
        return null;
    }

    @Override
    public boolean isValidBiome(Biome biome) {
        return false;
    }

    static {
        // 10387312, 34, 26
        CONFIGS = (new VersionMap()).add(MCVersion.v1_18, new RegionStructure.Config(34, 8, 10387312));
    }
}

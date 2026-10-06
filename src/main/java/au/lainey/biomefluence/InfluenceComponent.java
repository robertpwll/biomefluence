package au.lainey.biomefluence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class InfluenceComponent implements ServerTickingComponent {
    private final ChunkAccess access;

    public InfluenceComponent(ChunkAccess access) {
        this.access = access;
    }

    @Override
    public void serverTick() {

    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {

    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {

    }
}

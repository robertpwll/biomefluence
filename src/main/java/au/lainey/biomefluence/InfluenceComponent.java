package au.lainey.biomefluence;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Iterator;

public class InfluenceComponent implements ServerTickingComponent {
    private final Long2ObjectMap<WeightedList<ResourceKey<Biome>>> map = new Long2ObjectArrayMap<>();

    private final ChunkAccess access;

    public InfluenceComponent(ChunkAccess access) {
        this.access = access;
    }

    @Override
    public void serverTick() {
        if (access instanceof LevelChunk levelChunk) {

        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        map.clear();

    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();

        for (Long2ObjectMap.Entry<WeightedList<ResourceKey<Biome>>> entry : map.long2ObjectEntrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Long", entry.getLongKey());
            entryTag.put("List", write(entry.getValue(), provider));

            listTag.add(entryTag);
        }

        tag.put("Entries", listTag);
    }

    public ListTag write(WeightedList<ResourceKey<Biome>> list, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();

        Iterator<ObjectIntPair<ResourceKey<Biome>>> iterator = list.iterator();

        while (iterator.hasNext()) {
           ObjectIntPair<ResourceKey<Biome>> next = iterator.next();

            CompoundTag tag = new CompoundTag();

            String name = provider.lookupOrThrow(Registries.BIOME).getOrThrow(next.left()).getRegisteredName();
            tag.putString("Biome", name);

            tag.putInt("Weight", next.rightInt());
            listTag.add(tag);
        }

        return listTag;
    }

    public void add(BlockPos blockPos, ResourceKey<Biome> biome, int influence) {
        map.computeIfAbsent(indexOf(blockPos), value -> WeightedList.weightedList()).add(biome, influence);
    }

    public long indexOf(BlockPos blockPos) {
        int x = blockPos.getX() >> 3;
        int y = blockPos.getY() >> 3;
        int z = blockPos.getZ() >> 3;

        return x | y | z;
    }
}

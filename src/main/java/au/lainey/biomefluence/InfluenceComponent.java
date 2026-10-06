package au.lainey.biomefluence;

import au.lainey.biomefluence.mixin.FillBiomeCommandInvoker;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Iterator;
import java.util.List;

public class InfluenceComponent implements ServerTickingComponent {
    private final Long2ObjectMap<WeightedList<ResourceKey<Biome>>> map = new Long2ObjectArrayMap<>();

    private final ChunkAccess access;

    public InfluenceComponent(ChunkAccess access) {
        this.access = access;
    }

    @Override
    public void serverTick() {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        if (access instanceof LevelChunk levelChunk) {
            ServerLevel level = (ServerLevel) levelChunk.getLevel();

            RandomSource randomSource = level.random;

            if (randomSource.nextInt(25) == 0) {
                for (Long2ObjectMap.Entry<WeightedList<ResourceKey<Biome>>> entry : map.long2ObjectEntrySet()) {
                    WeightedList<ResourceKey<Biome>> list = entry.getValue();

                    if (!list.isEmpty()) {
                        long l = entry.getLongKey();

                        set(blockPos.set(0, 0, 0), level, list.get(randomSource));
                    }
                }
            }
        }
    }

    public void set(BlockPos blockPos, ServerLevel level, ResourceKey<Biome> biome) {
        int x = blockPos.getX() >> 4;
        int y = blockPos.getY() >> 4;

        ServerChunkCache chunks = level.getChunkSource();
        if (chunks.hasChunk(x, y)) {
            ChunkAccess access = level.getChunk(x, y, ChunkStatus.FULL);

            access.fillBiomesFromNoise((i, j, k, sampler) -> {
                BlockPos quantized = FillBiomeCommandInvoker.biomefluence$quantize(blockPos);

                return
                        quantized.getX() == QuartPos.toBlock(i) &&
                        quantized.getY() == QuartPos.toBlock(j) &&
                        quantized.getZ() == QuartPos.toBlock(k) ? level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome) : access.getNoiseBiome(i, j, k);
            }, chunks.randomState().sampler());

            access.setUnsaved(true);

            chunks.chunkMap.resendBiomesForChunks(List.of(access));
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
        map.computeIfAbsent(indexOf(blockPos), l -> WeightedList.weightedList()).add(biome, influence);
    }

    public long indexOf(BlockPos blockPos) {
        int x = blockPos.getX() >> 3;
        int y = blockPos.getY() >> 3;
        int z = blockPos.getZ() >> 3;

        return ((long) (x & 0xFFFFF) << 32) | ((long) (y & 0xFFFFF) << 16) | (z & 0xFFFFF);
    }
}

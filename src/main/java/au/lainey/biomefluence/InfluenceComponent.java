package au.lainey.biomefluence;

import au.lainey.biomefluence.mixin.FillBiomeCommandInvoker;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
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
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.List;

public class InfluenceComponent implements Component {
    private final Long2ObjectMap<WeightedList> map = new Long2ObjectArrayMap<>();

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        map.clear();

        ListTag tags = tag.getList("Entries", ListTag.TAG_COMPOUND);

        for (int i = 0; i < tags.size(); i++) {
            CompoundTag entryTag = tags.getCompound(i);

            WeightedList weights = WeightedList.read(entryTag.getList("Weights", ListTag.TAG_COMPOUND));
            map.put(entryTag.getLong("Long"), weights);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag tags = new ListTag();

        for (Long2ObjectMap.Entry<WeightedList> entry : map.long2ObjectEntrySet()) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.putLong("Long", entry.getLongKey());
            entryTag.put("Weights", entry.getValue().write(provider));

            tags.add(entryTag);
        }

        tag.put("Entries", tags);
    }

    public void add(BlockPos blockPos, ResourceKey<Biome> biome, int influence) {
        map.computeIfAbsent(indexOf(blockPos), l -> WeightedList.weightedList()).add(biome, influence);
    }

    public ResourceKey<Biome> get(BlockPos blockPos, RandomSource random, ServerLevel level) {
        ResourceKey<Biome> biome = level.getBiome(blockPos).unwrapKey().orElseThrow();

        long l = indexOf(blockPos);
        if (map.containsKey(l)) {
            WeightedList list = map.get(l);

            if (!list.isEmpty()) {
                biome = list.get(random);
                list.push(biome);
            }
        }

        return biome;
    }

    public void set(BlockPos blockPos, ServerLevel level, ResourceKey<Biome> biome) {
        int x = blockPos.getX() >> 4;
        int y = blockPos.getZ() >> 4;

        ServerChunkCache chunks = level.getChunkSource();
        if (chunks.hasChunk(x, y)) {
            ChunkAccess access = level.getChunk(x, y, ChunkStatus.FULL);

            access.fillBiomesFromNoise((i, j, k, sampler) -> {
                BlockPos quantized = FillBiomeCommandInvoker.biomefluence$quantize(blockPos);

                boolean isX = quantized.getX() == QuartPos.toBlock(i);
                boolean isY = quantized.getY() == QuartPos.toBlock(j);
                boolean isZ = quantized.getZ() == QuartPos.toBlock(k);
                return isX && isY && isZ ? level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biome) : access.getNoiseBiome(i, j, k);
            }, chunks.randomState().sampler());

            access.setUnsaved(true);

            chunks.chunkMap.resendBiomesForChunks(List.of(access));
        }
    }

    public long indexOf(BlockPos blockPos) {
        int x = (blockPos.getX() & 0xF) >> 3;
        int y = (blockPos.getY() & 0xF) >> 3;
        int z = (blockPos.getZ() & 0xF) >> 3;

        return ((long) (x & 0xFFFFF) << 32) | ((long) (y & 0xFFFFF) << 16) | (z & 0xFFFFF);
    }
}

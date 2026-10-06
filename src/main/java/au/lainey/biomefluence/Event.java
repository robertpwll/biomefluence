package au.lainey.biomefluence;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Event {
    public static final Event SAPLING_GROW = new Event() {

        @Override
        public String getName() {
            return "sapling_grow";
        }
    };

    public static final Map<String, Event> MAP = Map.of("sapling_grow", SAPLING_GROW);

    public static final Codec<Event> CODEC = Codec.stringResolver(Event::getName, MAP::get);

    private final List<Influence> list = new ArrayList<>();

    public void add(Influence influence) {
        list.add(influence);
    }

    public void invoke(BlockState blockState, RandomSource random, ServerLevel level, BlockPos blockPos) {
        for (Influence influence : list) {
            if (influence.predicate().test(blockState)) {
                ResourceKey<Biome> biome = influence.influence().get(random);

                ChunkAccess chunk = level.getChunk(blockPos);
                InfluenceComponent component = chunk.getComponent(Biomefluence.INFLUENCE);

                component.add(blockPos, biome, 3);
                Biomefluence.INFLUENCE.sync(chunk);
            }
        }
    }

    public void reload() {
        list.clear();
    }

    public String getName() {
        return null;
    }
}

package au.lainey.biomefluence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public sealed interface BlockPredicate {
    Map<String, MapCodec<? extends BlockPredicate>> MAP = Map.of("matching", Matching.CODEC);

    Codec<BlockPredicate> CODEC = Codec.STRING.dispatch(BlockPredicate::key, MAP::get);

    boolean test(BlockState blockState);

    String key();

    record Matching(ResourceKey<Block> block) implements BlockPredicate {
        public static final MapCodec<Matching> CODEC = RecordCodecBuilder.mapCodec(builder -> {
            return builder.group(ResourceKey.codec(Registries.BLOCK).fieldOf("block").forGetter(Matching::block)).apply(builder, Matching::new);

        });

        @Override
        public boolean test(BlockState blockState) {
            return blockState.is(block);
        }

        @Override
        public String key() {
            return "matching";
        }
    }
}

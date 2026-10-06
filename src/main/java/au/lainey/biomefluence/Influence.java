package au.lainey.biomefluence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Influence(Event event, BlockPredicate predicate, WeightedList influence) {
    public static final Codec<Influence> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(Event.CODEC.fieldOf("type").forGetter(Influence::event), BlockPredicate.CODEC.fieldOf("predicate").forGetter(Influence::predicate), WeightedList.CODEC.fieldOf("influence").forGetter(Influence::influence)).apply(builder, Influence::new);
    });

}

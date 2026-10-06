package au.lainey.biomefluence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeightedList {
    public static final Codec<WeightedList> CODEC = RecordCodecBuilder.<ObjectIntPair<ResourceKey<Biome>>>create(builder -> {
        return builder.group(ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(Pair::left), Codec.INT.fieldOf("weight").forGetter(ObjectIntPair::rightInt)).apply(builder, ObjectIntPair::of);

    }).listOf().xmap(pairs -> {
        WeightedList weightedList = WeightedList.weightedList();

        for (ObjectIntPair<ResourceKey<Biome>> pair : pairs) {
            weightedList.add(pair.left(), pair.rightInt());
        }

        return weightedList;
    }, weightedList -> {
        List<ObjectIntPair<ResourceKey<Biome>>> list = new ArrayList<>();

        for (int i = 0; i < weightedList.list.size(); i++) {
            list.add(ObjectIntPair.of(weightedList.list.get(i), weightedList.weights[i]));
        }

        return list;
    });

    private final List<ResourceKey<Biome>> list;

    private int[] weights;

    private WeightedList(List<ResourceKey<Biome>> list, int[] weights) {
        this.list = list;
        this.weights = weights;
    }

    public ResourceKey<Biome> get(RandomSource random) {
        int low = random.nextInt(getSum());

        int prev = 0;

        for (int i = 0; i < list.size(); i++) {
            prev += weights[i];

            if (low < prev) return list.get(i);
        }

        return list.getFirst();
    }

    public void add(ResourceKey<Biome> biome, int weight) {
        if (!list.contains(biome)) {
            list.add(biome);
            weights = Arrays.copyOf(weights, list.size() + 1);
        }

        int i = list.indexOf(biome);

        weights[i] += weight;
    }

    public int getSum() {
        return Arrays.stream(weights).sum();
    }

    public ListTag write(HolderLookup.Provider provider) {
        int i = 0;

        ListTag tags = new ListTag();

        while (i <= list.size()) {
            CompoundTag tag = new CompoundTag();

            String name =  provider.lookupOrThrow(Registries.BIOME).getOrThrow(list.get(i)).getRegisteredName();
            tag.putString("Biome", name);
            tag.putInt("Weight", weights[i++]);

            tags.add(tag);
        }

        return tags;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public static WeightedList weightedList() {
        return new WeightedList(new ArrayList<>(), new int[0]);
    }
}

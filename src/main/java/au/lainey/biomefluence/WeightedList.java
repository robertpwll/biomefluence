package au.lainey.biomefluence;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WeightedList<T> {
    private final List<T> list;

    private int[] weights = new int[0];

    public WeightedList(List<T> list) {
        this.list = list;
    }

    public T get(RandomSource random) {
        int low = random.nextInt(getSum());

        int prev = 0;

        for (int i = 0; i < list.size(); i++) {
            prev += weights[i];

            if (low < prev) return list.get(i);
        }

        return list.getFirst();
    }

    public void add(T t, int weight) {
        if (!list.contains(t)) {
            list.add(t);
            weights = Arrays.copyOf(weights, list.size() + 1);
        }

        int i = list.indexOf(t);

        weights[i] += weight;
    }

    public int getSum() {
        return Arrays.stream(weights).sum();
    }

    public Iterator<ObjectIntPair<T>> iterator() {
        int size = list.size();
        return new Iterator<>() {
            private int i;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public ObjectIntPair<T> next() {
                return ObjectIntPair.of(list.get(i), weights[i++]);
            }
        };
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public static <T> WeightedList<T> weightedList() {
        return new WeightedList<>(new ArrayList<>());
    }
}

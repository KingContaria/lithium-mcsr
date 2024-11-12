package me.jellysquid.mods.lithium.common.world.layer;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;

/**
 * A much faster implementation of CachingLayerSampler which implements a fixed-size "lossy" cache.
 * This is where the main advantage in this implementation comes from: being lossy, the cache does not have to
 * clean up old entries, it does not ever have to reallocate, and the cached value will always be in the first place
 * it checks.
 * <p>
 * This is a thread-safe version of Gegy's initial implementation, using an array of Entry objects.
 * Because of this we no longer need to use ThreadLocals, greatly reducing memory usage especially with SeedQueue,
 * where the increased number of worker threads also lead to an increased amount of FastCachingLayerSamplers.
 */
public final class FastCachingLayerSampler extends CachingLayerSampler {
    private final Entry[] cache;
    private final int mask;

    public FastCachingLayerSampler(int capacity, LayerOperator operator) {
        super(null, capacity, operator);

        capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = capacity - 1;

        this.cache = new Entry[capacity];
    }

    @Override
    public int sample(int x, int z) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        Entry entry = this.cache[idx];
        // if the entry here has a key that matches ours, we have a cache hit
        if (entry != null && entry.key == key) {
            return entry.value;
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = this.operator.apply(x, z);
        this.cache[idx] = new Entry(key, sampled);
        return sampled;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return ChunkPos.toLong(x, z);
    }

    private static class Entry {
        private final long key;
        private final int value;

        private Entry(long key, int value) {
            this.key = key;
            this.value = value;
        }
    }
}


package com.rlnkoo.primely;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.LongStream;

/**
 * Segmented sieve of Eratosthenes backed by BitSet and a small wheel (2*3*5) pre-sieve.
 * Can run segments in parallel (ForkJoin).
 */
public final class Sieve {
    private static final int DEFAULT_SEG_SZ = 1 << 20;
    private static final long[] SM_PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};

    private Sieve() {}

    /** Convenience: primes in [from, to) with defaults. */
    public static LongStream primesBetween(long from, long to) {
        return primesBetween(from, to, DEFAULT_SEG_SZ, false, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Primes in [from, to).
     * @param segmentSize numbers per segment (tune per cache/memory)
     * @param parallel whether to process segments in parallel
     * @param parallelism size of the ForkJoin pool when parallel
     */
    public static LongStream primesBetween(long from, long to, int segmentSize, boolean parallel, int parallelism) {
        if (from < 2) from = 2;
        if (to <= from) return LongStream.empty();

        final long limit = (long) Math.floor(Math.sqrt(to - 1));
        final List<Integer> base = basePrimes((int) limit);

        final long f = from, t = to;
        final int seg = Math.max(1 << 16, segmentSize);
        final int segCount = (int) ((t - f + seg - 1) / seg);

        LongStream idx = LongStream.range(0, segCount);
        if (parallel) {
            ForkJoinPool fjp = new ForkJoinPool(parallelism);
            // Create the stream inside the pool; ensure we shut it down when closing the stream.
            return fjp.submit(() ->
                    idx.parallel()
                            .flatMap(i -> sieveSegment(base, f + i * seg, Math.min(t, f + (i + 1L) * seg)))
                            .onClose(fjp::shutdown)
            ).join();
        } else {
            return idx.flatMap(i -> sieveSegment(base, f + i * seg, Math.min(t, f + (i + 1L) * seg)));
        }
    }

    private static LongStream sieveSegment(List<Integer> base, long start, long end) {
        int len = (int) (end - start);
        BitSet composite = new BitSet(len);

        // wheel-30 pre-cross: kill multiples of 2,3,5 quickly
        for (long p : SM_PRIMES) {
            if (p >= end) break;
            long m = Math.max(p * p, ((start + p - 1) / p) * p);
            for (long j = m; j < end; j += p) composite.set((int) (j - start));
        }

        // cross with base primes up to sqrt(end)
        for (int p : base) {
            long pp = (long) p * p;
            if (pp >= end) break; // smaller ones already handled
            long m = Math.max(pp, ((start + p - 1L) / p) * p);
            for (long j = m; j < end; j += p) composite.set((int) (j - start));
        }

        return LongStream.range(0, len)
                .filter(i -> !composite.get((int) i))
                .map(i -> start + i);
    }

    /** Simple sieve to produce base primes up to n (inclusive). */
    private static List<Integer> basePrimes(int n) {
        boolean[] cmp = new boolean[n + 1];
        for (int i = 2; i * (long) i <= n; i++)
            if (!cmp[i]) for (long j = (long) i * i; j <= n; j += i) cmp[(int) j] = true;
        ArrayList<Integer> ps = new ArrayList<>();
        for (int i = 2; i <= n; i++) if (!cmp[i]) ps.add(i);
        return ps;
    }
}
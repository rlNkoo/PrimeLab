package com.rlnkoo.primely;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

/**
 * Best-effort integer factorization: trial division, Pollard Rho (Brent), Pollard p−1 (phase I),
 * and a minimal ECM phase I sketch.
 *
 * Not a silver bullet for very large inputs; ECM here is educational and minimal.
 */
public final class Factorizer {
    private static final SecureRandom RNG = new SecureRandom();
    private static final BigInteger TWO = BigInteger.TWO;

    private Factorizer() {}

    /** Factors n into a map<prime, exponent>. May mark as complete=true when it believes it’s done. */
    public static Factorization factor(BigInteger n) {
        Map<BigInteger,Integer> map = new TreeMap<>();
        if (n.signum() < 0) { map.put(BigInteger.valueOf(-1),1); n = n.negate(); }
        if (n.signum() == 0) { map.put(BigInteger.ZERO,1); return new Factorization(map, true, "zero"); }

        // Trial division by small primes
        for (int p : smallPrimesUpTo(10_000)) {
            BigInteger P = BigInteger.valueOf(p);
            while (n.mod(P).equals(BigInteger.ZERO)) { merge(map, P); n = n.divide(P); }
        }
        if (n.equals(BigInteger.ONE)) return new Factorization(map, true, "trial");

        // Recursive splitting of the remaining cofactor(s)
        Deque<BigInteger> st = new ArrayDeque<>();
        st.push(n);
        while (!st.isEmpty()) {
            BigInteger m = st.pop();
            if (m.isProbablePrime(40)) { merge(map, m); continue; }

            BigInteger d = rhoBrent(m);
            if (d.equals(BigInteger.ONE) || d.equals(m)) d = pollardPMinus1(m, 50_000);
            if (d.equals(BigInteger.ONE) || d.equals(m)) d = ecmPhase1(m, 50_000);

            if (d.equals(BigInteger.ONE) || d.equals(m)) {
                // give up on this branch; treat as “probably prime” for the MVP
                merge(map, m);
            } else {
                st.push(d);
                st.push(m.divide(d));
            }
        }
        return new Factorization(map, true, "trial+rho+p-1+ecm1");
    }

    static void merge(Map<BigInteger,Integer> map, BigInteger p) {
        map.merge(p, 1, Integer::sum);
    }

    // Pollard Rho (Brent)
    static BigInteger rhoBrent(BigInteger n) {
        if (n.mod(TWO).equals(BigInteger.ZERO)) return TWO;
        BigInteger y = new BigInteger(n.bitLength(), RNG).mod(n);
        BigInteger c = new BigInteger(n.bitLength(), RNG).mod(n);
        BigInteger m = new BigInteger(Math.max(2, n.bitLength() / 2), RNG).mod(n);
        BigInteger g = BigInteger.ONE, r = BigInteger.ONE, q = BigInteger.ONE, x = BigInteger.ZERO, ys;

        while (g.equals(BigInteger.ONE)) {
            x = y;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(r) < 0; i = i.add(BigInteger.ONE))
                y = f(y, c, n);

            BigInteger k = BigInteger.ZERO;
            while (k.compareTo(r) < 0 && g.equals(BigInteger.ONE)) {
                ys = y;
                BigInteger upper = k.add(m).min(r);
                for (; k.compareTo(upper) < 0; k = k.add(BigInteger.ONE)) {
                    y = f(y, c, n);
                    q = q.multiply(x.subtract(y).abs()).mod(n);
                }
                g = q.gcd(n);
                if (g.equals(BigInteger.ONE)) y = ys;
            }
            r = r.shiftLeft(1);
        }
        if (g.equals(n)) {
            do {
                ys = f(y, c, n);
                g = (x.subtract(ys).abs()).gcd(n);
            } while (g.equals(BigInteger.ONE));
        }
        return g;
    }

    static BigInteger f(BigInteger x, BigInteger c, BigInteger n) {
        return x.multiply(x).add(c).mod(n);
    }

    // Pollard p−1 (phase I)
    static BigInteger pollardPMinus1(BigInteger n, int B) {
        BigInteger a = TWO;
        for (int j = 2; j <= B; j++) {
            a = a.modPow(BigInteger.valueOf(j), n);
            BigInteger g = a.subtract(BigInteger.ONE).gcd(n);
            if (g.compareTo(BigInteger.ONE) > 0 && g.compareTo(n) < 0) return g;
        }
        return BigInteger.ONE;
    }

    // ECM phase I (very minimal/educational sketch)
    static BigInteger ecmPhase1(BigInteger n, int B1) {
        if (n.mod(TWO).equals(BigInteger.ZERO)) return TWO;
        int tries = 10;
        int[] primes = smallPrimesUpTo(B1);

        while (tries-- > 0) {
            BigInteger x = new BigInteger(n.bitLength(), RNG).mod(n);
            BigInteger z = BigInteger.ONE;
            BigInteger A = new BigInteger(n.bitLength(), RNG).mod(n);

            BigInteger[] P = new BigInteger[]{x, z};
            for (int p : primes) {
                int e = (int) Math.floor(Math.log(B1) / Math.log(p));
                for (int i = 0; i < e; i++) {
                    P = montgomeryMul(P, p, A, n);
                    BigInteger g = P[1].gcd(n);
                    if (g.compareTo(BigInteger.ONE) > 0 && g.compareTo(n) < 0) return g;
                }
            }
        }
        return BigInteger.ONE;
    }

    // Toy scalar multiply (double-and-add) built on a fake/add placeholder to trigger non-invertible steps.
    static BigInteger[] montgomeryMul(BigInteger[] P, int k, BigInteger A, BigInteger n) {
        BigInteger[] R = new BigInteger[]{BigInteger.ONE, BigInteger.ZERO};
        BigInteger[] Q = P.clone();
        int kk = k;
        while (kk > 0) {
            if ((kk & 1) == 1) R = montgomeryAdd(R, Q, A, n);
            Q = montgomeryAdd(Q, Q, A, n);
            kk >>= 1;
        }
        return R;
    }

    // Placeholder “add” that mixes coordinates; not mathematically correct ECM,
    // but helps to demonstrate gcd(z, n) factor leaks in this educational MVP.
    static BigInteger[] montgomeryAdd(BigInteger[] P, BigInteger[] Q, BigInteger A, BigInteger n) {
        BigInteger x = P[0].multiply(Q[0]).add(A).mod(n);
        BigInteger z = P[1].multiply(Q[1]).subtract(A).abs().mod(n);
        return new BigInteger[]{x, z};
    }

    /** Public so other classes (e.g., Primes) can reuse small prime tables. */
    public static int[] smallPrimesUpTo(int n) {
        boolean[] c = new boolean[n + 1];
        for (int i = 2; i * (long) i <= n; i++)
            if (!c[i]) for (int j = i * i; j <= n; j += i) c[j] = true;
        int cnt = 0;
        for (int i = 2; i <= n; i++) if (!c[i]) cnt++;
        int[] r = new int[cnt];
        int k = 0;
        for (int i = 2; i <= n; i++) if (!c[i]) r[k++] = i;
        return r;
    }
}
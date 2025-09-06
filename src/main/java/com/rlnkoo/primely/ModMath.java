package com.rlnkoo.primely;

import java.math.BigInteger;
import java.util.List;

/** Small modular arithmetic helpers built on top of BigInteger. */
public final class ModMath {
    private ModMath() {}

    /** (a^e) mod m — delegates to BigInteger.modPow */
    public static BigInteger modPow(BigInteger a, BigInteger e, BigInteger m) {
        return a.modPow(e, m);
    }

    /** a^{-1} mod m, throws if non-invertible. */
    public static BigInteger modInverse(BigInteger a, BigInteger m) {
        return a.modInverse(m);
    }

    public static BigInteger gcd(BigInteger a, BigInteger b) { return a.gcd(b); }

    public static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.divide(a.gcd(b)).multiply(b);
    }

    /** Chinese Remainder Theorem for pairwise-coprime moduli. */
    public static BigInteger crt(List<BigInteger> residues, List<BigInteger> moduli) {
        if (residues.size() != moduli.size() || residues.isEmpty()) throw new IllegalArgumentException();
        BigInteger x = BigInteger.ZERO;
        BigInteger M = BigInteger.ONE;
        for (int i = 0; i < residues.size(); i++) {
            BigInteger r = residues.get(i), m = moduli.get(i);
            BigInteger t = r.subtract(x).mod(m);
            BigInteger inv = M.modInverse(m);
            x = x.add(M.multiply(t.multiply(inv).mod(m)));
            M = M.multiply(m);
            x = x.mod(M);
        }
        return x;
    }
}
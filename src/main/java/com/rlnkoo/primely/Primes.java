package com.rlnkoo.primely;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

/** High-level prime utilities: tests (MR64, BPSW), next/random/safe primes, and proof APIs. */
public final class Primes {
    private Primes() {}

    private static final long[] BASES64 = {2, 3, 5, 7, 11, 13, 17};
    private static final SecureRandom RNG = new SecureRandom();

    // ===== Deterministic Miller–Rabin for 64-bit =====
    public static boolean isPrimeDet64(long n) {
        if (n < 2) return false;
        for (long p : new long[]{2,3,5,7,11,13,17,19,23,29,31}) {
            if (n == p) return true;
            if (n % p == 0) return false;
        }
        long d = n - 1;
        int s = Long.numberOfTrailingZeros(d);
        d >>= s;
        for (long a : BASES64) {
            if (a % n == 0) return true;
            if (!mr64(n, a, d, s)) return false;
        }
        return true;
    }

    private static boolean mr64(long n, long a, long d, int s) {
        BigInteger N = BigInteger.valueOf(n), A = BigInteger.valueOf(a);
        BigInteger x = A.modPow(BigInteger.valueOf(d), N);
        if (x.equals(BigInteger.ONE) || x.equals(N.subtract(BigInteger.ONE))) return true;
        for (int r = 1; r < s; r++) {
            x = x.multiply(x).mod(N);
            if (x.equals(N.subtract(BigInteger.ONE))) return true;
        }
        return false;
    }

    // Baillie–PSW for arbitrary precision
    public static boolean isPrimeBPSW(BigInteger n) {
        Objects.requireNonNull(n);
        if (n.signum() <= 0) return false;
        if (n.compareTo(BigInteger.TWO) < 0) return false;

        // Small prime trial and quick square elimination
        for (int p : Factorizer.smallPrimesUpTo(1000)) {
            if (n.equals(BigInteger.valueOf(p))) return true;
            if (n.mod(BigInteger.valueOf(p)).equals(BigInteger.ZERO)) return false;
        }
        if (isSquare(n)) return false;

        if (!mrBase2(n)) return false;
        return strongLucasSelfridge(n);
    }

    static boolean mrBase2(BigInteger n) {
        BigInteger nm1 = n.subtract(BigInteger.ONE);
        int s = nm1.getLowestSetBit();
        BigInteger d = nm1.shiftRight(s);
        BigInteger x = BigInteger.TWO.modPow(d, n);
        if (x.equals(BigInteger.ONE) || x.equals(nm1)) return true;
        for (int r = 1; r < s; r++) {
            x = x.multiply(x).mod(n);
            if (x.equals(nm1)) return true;
        }
        return false;
    }

    static boolean strongLucasSelfridge(BigInteger n) {
        long D = 5; int sign = 1;
        while (true) {
            BigInteger bigD = BigInteger.valueOf(sign * D);
            int j = jacobi(bigD, n);
            if (j == -1) break;
            D += 2; sign = -sign; // iterate 5, -7, 9, -11, ...
        }
        long dSigned = sign * D;
        BigInteger P = BigInteger.ONE;
        BigInteger Q = BigInteger.valueOf((1 - dSigned) / 4);

        BigInteger n1 = n.add(BigInteger.ONE);
        int s = n1.getLowestSetBit();
        BigInteger d = n1.shiftRight(s);

        BigInteger[] UV = lucasUV(n, P, Q, d);
        BigInteger U = UV[0], V = UV[1];
        if (U.equals(BigInteger.ZERO) || V.equals(BigInteger.ZERO)) return true;

        BigInteger Qk = Q.mod(n);
        for (int r = 1; r < s; r++) {
            V = V.multiply(V).subtract(Qk.shiftLeft(1)).mod(n);
            Qk = Qk.multiply(Qk).mod(n);
            if (V.equals(BigInteger.ZERO)) return true;
        }
        return false;
    }

    static BigInteger[] lucasUV(BigInteger n, BigInteger P, BigInteger Q, BigInteger k) {
        BigInteger U = BigInteger.ZERO;      // U_0
        BigInteger V = BigInteger.TWO;       // V_0
        BigInteger Qk = BigInteger.ONE;      // Q^0

        for (int i = k.bitLength() - 1; i >= 0; i--) {
            // double-step
            BigInteger U2 = U.multiply(V).mod(n);
            BigInteger V2 = V.multiply(V).subtract(Qk.shiftLeft(1)).mod(n);
            U = U2; V = V2; Qk = Qk.multiply(Qk).mod(n);

            if (k.testBit(i)) {
                // add-step
                BigInteger U1 = U.add(V).mod(n);
                BigInteger V1 = V.add(U.multiply(P)).mod(n);
                U = U1; V = V1; Qk = Qk.multiply(Q).mod(n);
            }
        }
        return new BigInteger[]{U, V};
    }

    /** Jacobi symbol (a/n) with n odd positive. */
    static int jacobi(BigInteger a, BigInteger n) {
        if (n.signum() <= 0 || !n.testBit(0)) throw new IllegalArgumentException("n must be odd positive");
        a = a.mod(n);
        int r = 1;
        while (a.signum() != 0) {
            int t = a.getLowestSetBit();
            if (t > 0) {
                a = a.shiftRight(t);
                int n8 = n.intValue() & 7;
                if ((t & 1) == 1 && (n8 == 3 || n8 == 5)) r = -r;
            }
            if (a.compareTo(n) < 0) {
                BigInteger tmp = a; a = n; n = tmp;
                if (((a.intValue() & 3) == 3) && ((n.intValue() & 3) == 3)) r = -r;
            }
            a = a.subtract(n).mod(n);
        }
        return n.equals(BigInteger.ONE) ? r : 0;
    }

    static boolean isSquare(BigInteger n) {
        BigInteger r = sqrt(n);
        return r.multiply(r).equals(n);
    }

    /** Integer sqrt via Newton's method. */
    static BigInteger sqrt(BigInteger n) {
        BigInteger x = BigInteger.ONE.shiftLeft((n.bitLength() + 1) >>> 1);
        while (true) {
            BigInteger y = x.add(n.divide(x)).shiftRight(1);
            if (y.equals(x) || y.equals(x.subtract(BigInteger.ONE))) return y;
            x = y;
        }
    }

    // Next / random / safe primes
    public static BigInteger nextPrime(BigInteger n) {
        if (n == null) throw new NullPointerException();
        if (n.compareTo(BigInteger.TWO) <= 0) return BigInteger.TWO;
        BigInteger p = n.testBit(0) ? n : n.add(BigInteger.ONE);
        while (true) {
            if (isPrimeBPSW(p)) return p;
            p = p.add(BigInteger.TWO);
        }
    }

    /** Random probable prime with given bit length (BPSW tested). */
    public static BigInteger randomPrime(int bits) {
        while (true) {
            BigInteger c = new BigInteger(bits, RNG).setBit(bits - 1).setBit(0);
            if (isPrimeBPSW(c)) return c;
        }
    }

    /** Random safe prime p where q=(p-1)/2 is also prime. */
    public static BigInteger randomSafePrime(int bits) {
        while (true) {
            BigInteger q = randomPrime(bits - 1);
            BigInteger p = q.shiftLeft(1).add(BigInteger.ONE);
            if (isPrimeBPSW(p)) return p;
        }
    }

    public static Optional<Proofs.PrimeCertificate> provePratt(BigInteger n) {
        return Proofs.provePratt(n).map(pc -> pc);
    }

    public static Optional<Proofs.PrimeCertificate> provePocklington(BigInteger n) {
        return Proofs.provePocklington(n).map(pc -> pc);
    }
}
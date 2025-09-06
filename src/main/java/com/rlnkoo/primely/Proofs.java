package com.rlnkoo.primely;

import java.math.BigInteger;
import java.util.*;

/** Simple primality certificates: Pratt and Pocklington (best-effort). */
public final class Proofs {

    public sealed interface PrimeCertificate permits PrattCert, PocklingtonCert {
        String pretty();
    }

    /** Minimal Pratt certificate carrying the base list used for n (requires full factorization of n-1). */
    public record PrattCert(BigInteger n, Map<BigInteger, List<BigInteger>> tree) implements PrimeCertificate {
        @Override public String pretty() {
            StringBuilder sb = new StringBuilder("Pratt certificate for ").append(n).append('\n');
            for (var e : tree.entrySet())
                sb.append("  p=").append(e.getKey()).append(" via bases ").append(e.getValue()).append('\n');
            return sb.toString();
        }
    }

    /** Pocklington certificate with an informative factor and a suitable base a. */
    public record PocklingtonCert(BigInteger n, BigInteger q, BigInteger a) implements PrimeCertificate {
        @Override public String pretty() {
            return "Pocklington: n=" + n + ", q=" + q + ", a=" + a;
        }
    }

    private static final BigInteger TWO = BigInteger.TWO;

    /** Pratt: needs full factorization of n-1; tries to find a base 'a' verifying the conditions. */
    public static Optional<PrattCert> provePratt(BigInteger n) {
        if (n.compareTo(TWO) < 0) return Optional.empty();
        BigInteger nm1 = n.subtract(BigInteger.ONE);
        var fac = Factorizer.factor(nm1);
        if (!fac.complete()) return Optional.empty();

        Map<BigInteger,Integer> f = fac.factors();
        Map<BigInteger, List<BigInteger>> cert = new LinkedHashMap<>();
        BigInteger a = TWO;

        outer:
        for (; a.compareTo(nm1) < 0; a = a.add(BigInteger.ONE)) {
            if (!a.modPow(nm1, n).equals(BigInteger.ONE)) continue;
            boolean ok = true; List<BigInteger> bases = new ArrayList<>();
            for (var q : f.keySet()) {
                BigInteger t = a.modPow(nm1.divide(q), n).subtract(BigInteger.ONE).gcd(n);
                if (!t.equals(BigInteger.ONE)) { ok = false; break; } else bases.add(q);
            }
            if (ok) { cert.put(n, bases); break outer; }
        }
        if (!cert.containsKey(n)) return Optional.empty();
        return Optional.of(new PrattCert(n, cert));
    }

    /**
     * Pocklington: If n-1 = F * R with gcd(F, R) = 1, F > sqrt(n),
     * and there exists a base a such that:
     *   a^(n-1) ≡ 1 (mod n)
     *   a^((n-1)/q) ≠ 1 (mod n) for every prime q | F
     * then n is prime.
     *
     * We build F as the product of all known prime powers in the factorization of (n-1).
     */
    public static Optional<PocklingtonCert> provePocklington(BigInteger n) {
        if (n.compareTo(TWO) <= 0) return Optional.empty();

        BigInteger nm1 = n.subtract(BigInteger.ONE);
        var fac = Factorizer.factor(nm1);
        var factors = fac.factors();

        BigInteger F = BigInteger.ONE;
        BigInteger largestQ = BigInteger.ONE;
        for (var e : factors.entrySet()) {
            BigInteger q = e.getKey();
            int exp = e.getValue();
            BigInteger qPow = q.pow(exp);
            F = F.multiply(qPow);
            if (q.compareTo(largestQ) > 0) largestQ = q;
        }

        if (F.multiply(F).compareTo(n) <= 0) {
            return Optional.empty();
        }

        for (BigInteger a = TWO; a.compareTo(nm1) < 0; a = a.add(BigInteger.ONE)) {
            if (!a.modPow(nm1, n).equals(BigInteger.ONE)) continue;
            boolean ok = true;
            for (var e : factors.entrySet()) {
                BigInteger q = e.getKey();
                if (a.modPow(nm1.divide(q), n).equals(BigInteger.ONE)) { ok = false; break; }
            }
            if (ok) return Optional.of(new PocklingtonCert(n, largestQ, a));
        }
        return Optional.empty();
    }
}
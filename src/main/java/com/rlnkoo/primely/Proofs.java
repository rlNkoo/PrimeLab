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

    /** Pocklington certificate with a large factor q | (n-1) and a suitable base a. */
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
     * Pocklington (simple form): if there's a known factor q | (n-1) such that q > sqrt(n)-1 and
     * a suitable base a passes the conditions, n is prime.
     */
    public static Optional<PocklingtonCert> provePocklington(BigInteger n) {
        if (n.compareTo(TWO) <= 0) return Optional.empty();
        BigInteger nm1 = n.subtract(BigInteger.ONE);
        var fac = Factorizer.factor(nm1);

        BigInteger bigQ = BigInteger.ONE;
        for (var e : fac.factors().entrySet()) {
            BigInteger q = e.getKey();
            if (q.compareTo(bigQ) > 0) bigQ = q;
        }
        if (bigQ.multiply(bigQ).compareTo(n.subtract(BigInteger.ONE)) <= 0) return Optional.empty();

        for (BigInteger a = TWO; a.compareTo(nm1) < 0; a = a.add(BigInteger.ONE)) {
            if (!a.modPow(nm1, n).equals(BigInteger.ONE)) continue;
            boolean good = true;
            for (var e : fac.factors().entrySet()) {
                BigInteger q = e.getKey();
                BigInteger t = a.modPow(nm1.divide(q), n);
                if (t.equals(BigInteger.ONE)) { good = false; break; }
            }
            if (good) return Optional.of(new PocklingtonCert(n, bigQ, a));
        }
        return Optional.empty();
    }
}
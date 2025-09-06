package com.rlnkoo.primely;

import java.math.BigInteger;
import java.util.Map;

/** Result of factorization: factor -> exponent map, completeness flag, and method tag. */
public record Factorization(Map<BigInteger,Integer> factors, boolean complete, String method) {

    /** Reconstructs the product from the factors/exponents. */
    public BigInteger reconstruct() {
        return factors.entrySet().stream()
                .map(e -> e.getKey().pow(e.getValue()))
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }
}
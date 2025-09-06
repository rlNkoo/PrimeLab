package primely;

import com.rlnkoo.primely.Primes;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomPrimeTest {

    @Test void randomPrimeSmallBitsIsPrime() {
        BigInteger p = Primes.randomPrime(32);
        assertTrue(Primes.isPrimeBPSW(p));
    }

    @Test void randomSafePrimeSmallBits() {
        BigInteger p = Primes.randomSafePrime(32);
        BigInteger q = p.subtract(BigInteger.ONE).shiftRight(1);
        assertTrue(Primes.isPrimeBPSW(p));
        assertTrue(Primes.isPrimeBPSW(q));
    }
}
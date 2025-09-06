package primely;

import com.rlnkoo.primely.Primes;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class ProofsTest {

    @Test void pocklingtonOnSafePrime() {
        BigInteger q = new BigInteger("359");
        BigInteger p = q.shiftLeft(1).add(BigInteger.ONE);
        assertTrue(Primes.isPrimeBPSW(q));
        assertTrue(Primes.isPrimeBPSW(p));
        assertTrue(Primes.provePocklington(p).isPresent(), "Pocklington should succeed for safe prime p=719");
    }

    @Test void prattOnSmallPrime() {
        BigInteger p = new BigInteger("101");
        assertTrue(Primes.provePratt(p).isPresent(), "Pratt should succeed with fully factored n-1");
    }
}
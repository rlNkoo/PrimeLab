package primely;

import com.rlnkoo.primely.Primes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Deterministic64Test {

    @Test void smallPrimes() {
        long[] primes = {2,3,5,7,11,13,17,19,23,29,31,97,101,997,1000003L};
        for (long p : primes) assertTrue(Primes.isPrimeDet64(p), "should be prime: " + p);
    }

    @Test void smallComposites() {
        long[] comps = {1,4,6,8,9,12,15,25,100,221,341,561,1105};
        for (long n : comps) assertFalse(Primes.isPrimeDet64(n), "should be composite: " + n);
    }
}
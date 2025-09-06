package primely;

import com.rlnkoo.primely.Factorizer;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FactorizerTest {

    @Test void factorSmallComposite() {
        var n = BigInteger.valueOf(600851475143L);
        var f = Factorizer.factor(n);
        assertTrue(f.complete());
        Map<BigInteger,Integer> m = f.factors();
        assertEquals(1, m.get(new BigInteger("71")));
        assertEquals(1, m.get(new BigInteger("839")));
        assertEquals(1, m.get(new BigInteger("1471")));
        assertEquals(1, m.get(new BigInteger("6857")));
        assertEquals(n, f.reconstruct());
    }

    @Test void factorPrimeReturnsPrimeItself() {
        var p = new BigInteger("1000003"); // prime
        var f = Factorizer.factor(p);
        assertTrue(f.complete());
        assertEquals(1, f.factors().get(p));
    }
}
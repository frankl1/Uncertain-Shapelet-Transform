package utilities;

import java.util.Random;

public interface Reproducible {
    void setSeed(long seed);
    void setRandom(Random random);
}

package de.hskl.ps.bluetoothinvokeexample.example;

/**
 * Sleep and return value.
 * <p>
 * Used as an Example for BTInvoke.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public interface ISleeper {
    /**
     * Sleep and then return the double value.
     * <p>
     * Will sleep using {@link Thread#sleep(long)}.
     * 
     * @param s
     *            How long to sleep in seconds.
     * @param d
     *            Double to return after sleeping.
     * @return {@code d}
     * @throws InterruptedException
     *             If the sleeping was Interupted.
     */
    double sleepForSecondsAndReturn(int s, double d) throws InterruptedException;
}

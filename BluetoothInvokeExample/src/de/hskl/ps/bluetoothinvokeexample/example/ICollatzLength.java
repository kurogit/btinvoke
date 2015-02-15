package de.hskl.ps.bluetoothinvokeexample.example;

/**
 * Calculate the length of a Collatz sequence.
 * <p>
 * Used as an Example for BTInvoke.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public interface ICollatzLength {
    /**
     * Calculate the length of a Collatz/Hailstone sequence.
     * 
     * @param startValue
     *            the startValue of the sequence.
     * @return The length of the sequence starting with {@code startValue}
     */
    long lengthOfHailstoneSequence(long startValue);
}

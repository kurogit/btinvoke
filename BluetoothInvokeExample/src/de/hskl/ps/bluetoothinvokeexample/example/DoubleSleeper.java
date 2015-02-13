package de.hskl.ps.bluetoothinvokeexample.example;

public class DoubleSleeper implements ISleeper {

    @Override
    public double sleepForSecondsAndReturn(int s, double d) throws InterruptedException {
        Thread.sleep(s * 1000);
        return d;
    }

}

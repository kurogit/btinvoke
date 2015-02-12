package de.hskl.ps.bluetoothinvokeexample.example;

public class CollatzLength implements ICollatzLength {

    @Override
    public long lengthOfHailstoneSequence(long startValue) {
        if(startValue < 1)
            return 0;

        long length = 1;

        long value = startValue;

        while(value != 1)
        {
            value = ((value % 2) == 0 ? value / 2 : (value * 3) + 1);

            ++length;
        }
        
        // Trololo
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return length;
    }

}

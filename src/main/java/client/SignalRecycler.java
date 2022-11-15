package client;

import java.io.InputStream;

/**
 * @author Emilio Zottel (4AHIF)
 * @since 08.11.2022, Di.
 */
public enum SignalRecycler {
    DELAY(InputStream::read),

    DISCARD((is, buffer) -> {
        //is.skip((long) is.available() - buffer.length);  // Skip all bytes immediately except the last buffer
        is.skip(buffer.length);  // Skip one buffer per "recycle" call, should be sufficient
        return 0;
    }),

    SPEED_UP((is, buffer) -> {
        throw new UnsupportedOperationException("Not implemented yet");
        /*
        byte[] delayedBuffer = new byte[buffer.length];
        int read = is.read(delayedBuffer);

        for (int i = 0; i < buffer.length / 2; i++) {
            buffer[i] = delayedBuffer[i * 2];
        }

        return read;
        */
    });

    // ONLY SPEEDS UP HALF THE SAMPLES OF ONE "FRAME", SO IT ALTERNATES BETWEEN FAST AND SLOW
//    SPEED_UP((is, buffer) -> {
//        byte[] delayedData = new byte[buffer.length];
//        int spedUpDataLength = delayedData.length / 2;
//        //int spedUpDataLength = delayedData.length / speedUpFactor;
//
//        int totalReadBytes = 0;
//        totalReadBytes += is.read(delayedData);
//        totalReadBytes += is.read(buffer, spedUpDataLength, buffer.length - spedUpDataLength);
//
//        for (int i = 0; i < spedUpDataLength; i++) {
//            buffer[i] = delayedData[i * 2];
//            //buffer[i] = delayedData[i * speedUpFactor];
//        }
//
//        return totalReadBytes;
//    });


    //private static int speedUpFactor = 2;
    private final BiFunctionWithException<InputStream, byte[], Integer> recycler;

    SignalRecycler(BiFunctionWithException<InputStream, byte[], Integer> recycler) {
        this.recycler = recycler;
    }

    public int recycle(InputStream inputStream, byte[] targetBuffer) throws Exception {
        return recycler.apply(inputStream, targetBuffer);
    }


    //public static void setSpeedUpFactor(int speedUpFactor) {
    //    SignalCombiner.speedUpFactor = speedUpFactor;
    //}

}

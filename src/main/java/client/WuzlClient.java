package client;

import misc.WuzlConfig;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.*;
import java.net.Socket;

/**
 * @author : Emilio Zottel (4AHIF)
 * @since : 07.11.2022, Mo.
 **/
public class WuzlClient implements AutoCloseable {
    private static final String HOST = "localhost";
    //private static final String HOST = "62.47.156.38";
    private static final int PORT = 27005;

    private final Socket clientSocket;
    private final InputStream socketInputStream;
    private final OutputStream socketOutputStream;
    private final TargetDataLine inputDevice;
    private final SourceDataLine outputDevice;
    private final WuzlConfig config;
    private SignalRecycler signalRecycler;
    private float recycleThreshold;

    public WuzlClient(String host, int port) throws IOException, ClassNotFoundException, LineUnavailableException {
        this.clientSocket = new Socket(host, port);
        this.socketInputStream = clientSocket.getInputStream();
        this.socketOutputStream = clientSocket.getOutputStream();
        System.out.println("Connected to server as: " + clientSocket);

        this.config = (WuzlConfig) receiveObject();
        this.inputDevice = config.defaultInputDevice().orElseThrow(() -> new LineUnavailableException("No input device available for the following AudioFormat: " + config.audioFormat()));
        this.outputDevice = config.defaultOutputDevice().orElseThrow(() -> new LineUnavailableException("No output device available for the following AudioFormat: " + config.audioFormat()));

        this.signalRecycler = SignalRecycler.DISCARD;
        this.recycleThreshold = config.bufferSize() * 2.0f;
    }


    public static void main(String[] args) {
        try (var client = new WuzlClient(HOST, PORT)) {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Object receiveObject() throws IOException, ClassNotFoundException {
        return new ObjectInputStream(socketInputStream).readObject();
    }

    public void start() throws LineUnavailableException {
        byte[] inputBuffer = new byte[config.bufferSize()];
        byte[] outputBuffer = new byte[config.bufferSize()];
        prepareDevice(inputDevice);
        prepareDevice(outputDevice);

//        Thread senderThread = new Thread(() -> {
//            while (true) {
//                readAudioFromInputDeviceAndSendToServer(inputBuffer);
//            }
//        });
//
//        Thread receiverThread = new Thread(() -> {
//            while (true) {
//                receiveAudioFromServerAndWriteToOutputDevice(outputBuffer);
//            }
//        });

        while (true) {
            readAudioFromInputDeviceAndSendToServer(inputBuffer);
            receiveAudioFromServerAndWriteToOutputDevice(outputBuffer);
        }

//        senderThread.start();
//        receiverThread.start();
    }

    private void prepareDevice(DataLine device) throws LineUnavailableException {
        device.open();
        device.start();
    }

    private void readAudioFromInputDeviceAndSendToServer(byte[] buffer) {
        readAudioFromInputDevice(buffer);

        try {
            sendAudioToServer(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveAudioFromServerAndWriteToOutputDevice(byte[] buffer) {
        try {
            //receiveAudioFromServer(buffer);
            combineAudioFromEveryoneElse(buffer);
            writeAudioToOutputDevice(buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readAudioFromInputDevice(byte[] buffer) {
        inputDevice.read(buffer, 0, buffer.length);
    }

    private void sendAudioToServer(byte[] buffer) throws IOException {
        socketOutputStream.write(buffer);
    }

    private void combineAudioFromEveryoneElse(byte[] buffer) throws Exception {
        int[] sumOfAllSignals = new int[buffer.length];

        while (true) {
            if (socketInputStream.read() == 0)  // 0 = stop adding here, 1 = add following audio chunk to sum
                break;

            receiveAudioFromServer(buffer);
            addSignalToSum(buffer, sumOfAllSignals);
        }

        clampSumToByte(sumOfAllSignals, buffer);
    }

    private int receiveAudioFromServer(byte[] buffer) throws Exception {
        int available = socketInputStream.available();

        if (available > recycleThreshold) {
            return signalRecycler.recycle(socketInputStream, buffer);
        } else {
            int readBytes = socketInputStream.read(buffer);
            if (readBytes == -1)
                throw new EOFException("Server closed connection");

            return readBytes;
        }
    }

    private void writeAudioToOutputDevice(byte[] buffer) {
        outputDevice.write(buffer, 0, buffer.length);
    }

    /**
     * The two arrays are assumed to be of the same length. This is not checked for performance reasons.
     *
     * @param buffer          the buffer to be added to the sum component-wisely
     * @param sumOfAllSignals the array to which the buffer is added
     */
    private void addSignalToSum(byte[] buffer, int[] sumOfAllSignals) {
        for (int i = 0; i < buffer.length; i++) {
            sumOfAllSignals[i] += buffer[i];
        }
    }

    /**
     * The two arrays are assumed to be of the same length. This is not checked for performance reasons.
     *
     * @param intBuffer  the buffer to clamp to byte
     * @param byteBuffer the buffer to write the clamped values to
     */
    private void clampSumToByte(int[] intBuffer, byte[] byteBuffer) {
        for (int i = 0; i < intBuffer.length; i++) {
            byteBuffer[i] = clampToByte(intBuffer[i]);
        }
    }

    private byte clampToByte(int value) {
        return (byte) clamp(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }


    public Socket getSocket() {
        return clientSocket;
    }

    public TargetDataLine getInputDevice() {
        return inputDevice;
    }

    public SourceDataLine getOutputDevice() {
        return outputDevice;
    }

    public SignalRecycler getSignalRecycler() {
        return signalRecycler;
    }

    public void setSignalRecycler(SignalRecycler combiner) {
        this.signalRecycler = combiner;
    }

    public float getRecycleThreshold() {
        return recycleThreshold;
    }

    public void setRecycleThreshold(int recycleThreshold) {
        this.recycleThreshold = recycleThreshold;
    }

    @Override
    public void close() throws Exception {
        clientSocket.close();
        inputDevice.close();
        outputDevice.close();
    }

}

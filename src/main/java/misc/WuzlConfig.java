package misc;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author : Emilio Zottel (4AHIF)
 * @since : 07.11.2022, Mo.
 **/
public class WuzlConfig implements Serializable {
    private final transient int communicationPort;
    private final int bufferSize;
    private final SerializableAudioFormat serializableAudioFormat;
    private transient AudioFormat audioFormat;

    public WuzlConfig(int communicationPort, int bufferSize, SerializableAudioFormat serializableAudioFormat) {
        this.communicationPort = communicationPort;
        this.bufferSize = bufferSize;
        this.audioFormat = serializableAudioFormat.toAudioFormat();
        this.serializableAudioFormat = serializableAudioFormat;
    }


    public static WuzlConfig defaultSettings() {
        return new WuzlConfig(
                27005,
                8192,
                new SerializableAudioFormat(
                        44100,
                        16,  // 8-bit produces a lot of noise!
                        1,
                        true,
                        true
                )
        );
    }

    public Optional<TargetDataLine> defaultInputDevice() throws LineUnavailableException {
        try {
            return Optional.of(AudioSystem.getTargetDataLine(audioFormat));
        } catch (LineUnavailableException e) {
            return Optional.empty();
        }
    }

    public Optional<SourceDataLine> defaultOutputDevice() throws LineUnavailableException {
        try {
            return Optional.of(AudioSystem.getSourceDataLine(audioFormat));
        } catch (LineUnavailableException e) {
            return Optional.empty();
        }
    }


    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        audioFormat = serializableAudioFormat.toAudioFormat();
    }


    public int communicationPort() {
        return communicationPort;
    }

    public int bufferSize() {
        return bufferSize;
    }

    public AudioFormat audioFormat() {
        return audioFormat;
    }

}

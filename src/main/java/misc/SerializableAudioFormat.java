package misc;

import javax.sound.sampled.AudioFormat;
import java.io.Serializable;

/**
 * @author : Emilio Zottel (4AHIF)
 * @since : 08.11.2022, Di.
 **/
public record SerializableAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) implements Serializable {

    public AudioFormat toAudioFormat() {
        return new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian
        );
    }

}

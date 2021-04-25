package xyz.gianlu.librespot.android.sink;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;
import xyz.gianlu.librespot.player.mixing.output.SinkException;

import static android.media.AudioTrack.PLAYSTATE_PLAYING;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static xyz.gianlu.librespot.android.sink.ToneGenerator.genTone;

@RunWith(AndroidJUnit4.class)
public class AudioStreamInstrumentedTest {

    @Test
    public void testActualBeep() throws SinkException {

        // given
        AndroidSinkOutput codeUnderTest = new AndroidSinkOutput();
        OutputAudioFormat outputAudioFormat = new OutputAudioFormat(
                44100,
                16,
                1,
                true,
                false
        );
        int durationInSeconds = 2;

        // when
        codeUnderTest.start(outputAudioFormat);
        range(0, durationInSeconds).forEach(value -> {
            byte[] generatedSnd = genTone(1, (int) outputAudioFormat.getSampleRate(), 400);

            try {
                codeUnderTest.write(generatedSnd, 0, generatedSnd.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // then
        assertEquals(PLAYSTATE_PLAYING, codeUnderTest.getPlayState());
        // Manual Assertion: You should hear a beep on the device/emulator
    }
}

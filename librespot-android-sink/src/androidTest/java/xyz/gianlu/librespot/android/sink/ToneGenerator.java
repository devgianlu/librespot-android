package xyz.gianlu.librespot.android.sink;

/* From: https://stackoverflow.com/a/33676423
 * Author: https://stackoverflow.com/users/3896025/gerry
 * License: https://creativecommons.org/licenses/by-sa/3.0/
 */
class ToneGenerator {

    //Generate tone data for 1 seconds
    static byte[] genTone(int iStep, int sampleRate, int m_ifreq) {
        double[] sample = new double[sampleRate];

        for (int i = 0; i < sampleRate; ++i) {
            sample[i] = Math.sin(2 * Math.PI * (i + iStep * sampleRate) / (sampleRate / m_ifreq));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        byte[] generatedSnd = new byte[2 * sampleRate];
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        return generatedSnd;
    }
}

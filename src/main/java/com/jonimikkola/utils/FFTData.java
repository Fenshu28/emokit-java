//Copyright Joni Mikkola 2014

package com.jonimikkola.utils;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import static com.jonimikkola.utils.SignalProcessing.HanningWindow;
import static com.jonimikkola.utils.SignalProcessing.HighpassFilter;

public class FFTData {
    public double[] data;
    public Boolean show;
    private DoubleFFT_1D fft;

    public FFTData(double[] d, Boolean s, int frequency) {
        fft = new DoubleFFT_1D(frequency);
        show = s;
        data = d;
    }

    public double[] calculate(int frequency, int numSamples) {
        double[] result = new double[frequency];
        double[] ys = new double[frequency*2];
        double[] array = data;
        array = HighpassFilter(array, 5, frequency, numSamples);
        array = HanningWindow(array, 0, numSamples);

        for(int j = 0; j < frequency*2; ++j) {
            ys[j] = 0;
        }

        for(int j = 0; j < frequency; ++j) {
            ys[j] = array[j];
        }

        fft.complexForward(ys);
        for(int j = 0; j < frequency ; j++) {
            double re = ys[2*j];
            double im = ys[2*j+1];
            double magnitude = Math.sqrt(re*re+im*im);
            result[j] = magnitude;
        }
        return result;
    }
}

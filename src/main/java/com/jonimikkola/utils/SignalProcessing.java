//Copyright Joni Mikkola 2014

package com.jonimikkola.utils;

import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import com.jonimikkola.EmoConfig;

import java.util.HashMap;
import java.util.Map;

public class SignalProcessing implements EmotivListener{
    private final int numSamples = 512;
    private Boolean meanCalculated = false;
    private final HashMap<Packet.Sensor, double[]> dcOffsets;
    private int dcIndex = 0;

    public SignalProcessing() {
        dcOffsets = new HashMap<Packet.Sensor, double[]>();
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            dcOffsets.put(EmoConfig.sensorIds[i], new double[numSamples]);
        }
    }

//http://stackoverflow.com/questions/11600515/hanning-von-hann-window
    static public double[] HanningWindow(double[] signal_in, int pos, int size)
    {
        for (int i = pos; i < pos + size; i++)
        {
            int j = i - pos; // j = index into Hann window function
            signal_in[i] = (double) (signal_in[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * j / size)));
        }
        return signal_in;
    }

//http://stackoverflow.com/questions/13882038/implementing-simple-high-and-low-pass-filters-in-c
    static public double[] HighpassFilter(double[] in, int cutoff, int sampleRate, int numSamples) {
        float RC = 1.0f/(cutoff*2.0f*3.14f);
        float dt = 1.0f/sampleRate;
        float alpha = RC/(RC + dt);
        double []filteredArray = new double[numSamples];
        filteredArray[0] = in[0];
        for (int i = 1; i<numSamples; i++){
            filteredArray[i] = alpha * (filteredArray[i-1] + in[i] - in[i-1]);
        }
        return filteredArray;
    }

    private void calculateMean() {
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            float average = 0;
            for(int j = 0; j < numSamples; ++j) {
                average += dcOffsets.get(EmoConfig.sensorIds[i])[j];
            }
            EmoConfig.setMean(EmoConfig.sensorIds[i], (int)average / numSamples);
        }
    }

    @Override
    public void receivePacket(Packet packet) {
        if(!meanCalculated) {
            for (Map.Entry<Packet.Sensor, Integer> entry : packet.getSensors().entrySet()) {
                double[] offsets = dcOffsets.get(entry.getKey());
                offsets[dcIndex] = entry.getValue();
            }

            if(dcIndex + 1 >= numSamples) {
                calculateMean();
                meanCalculated = true;
                dcIndex = 0;
            } else {
                dcIndex++;
            }
        }
    }

    @Override
    public void connectionBroken() {}
}

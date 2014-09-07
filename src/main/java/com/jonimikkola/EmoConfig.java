package com.jonimikkola;

import com.github.fommil.emokit.Packet;

import java.awt.*;
import java.util.HashMap;

public class EmoConfig {
    public static final int FFTVIEW_MAX_FREQUENCY = 128;

    public static final String[] sensorNames = {"AF3", "F7", "F3", "FC5", "T7", "P7", "O1", "O2","P8", "T8", "FC6", "F4", "F8", "AF4"};
    public static final int NUM_SENSORS = 14;
    public static final Packet.Sensor[] sensorIds = { Packet.Sensor.AF3, Packet.Sensor.F7, Packet.Sensor.F3, Packet.Sensor.FC5, Packet.Sensor.T7,
            Packet.Sensor.P7, Packet.Sensor.O1, Packet.Sensor.O2, Packet.Sensor.P8, Packet.Sensor.T8, Packet.Sensor.FC6, Packet.Sensor.F4, Packet.Sensor.F8, Packet.Sensor.AF4};
    public static final Color[] sensorColors = {new Color(255, 0, 0),new Color(255, 128, 0), new Color(255, 255, 0), new Color(128, 255, 0), new Color(0, 255, 0),
            new Color(0, 255, 128), new Color(0, 255, 255), new Color(0, 128, 255), new Color(0, 0, 255), new Color(127, 0, 255),
            new Color(255, 0, 255), new Color(255, 0, 127), new Color(128, 128, 128), new Color(0, 0, 0)};

    private static final HashMap<Packet.Sensor, Integer> means = new HashMap<Packet.Sensor, Integer>();

    public static void init() {
        for(int i = 0; i < NUM_SENSORS; ++i) {
            means.put(sensorIds[i], 0);
        }
    }

    public static int getMean(Packet.Sensor sensor) {
        return means.get(sensor);
    }

    public static void setMean(Packet.Sensor sensor, int number) {
        means.put(sensor, number);
    }
}

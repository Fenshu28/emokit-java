//Copyright Joni Mikkola 2014

package com.jonimikkola.utils;

import com.github.fommil.emokit.Packet;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.util.HashMap;

public class ChartContainer {
    public HashMap<Packet.Sensor, XYSeries> series;
    public XYSeries singleSerie;
    public JFreeChart chart;
    public ChartPanel chartPanel;
    public JPanel fullChartPanel;

    public ChartContainer(ChartPanel p, JFreeChart c, HashMap<Packet.Sensor, XYSeries> s, JPanel fP) {
        series = s;
        chart = c;
        chartPanel = p;
        fullChartPanel = fP;
    }

    public ChartContainer(ChartPanel p, JFreeChart c, XYSeries s, JPanel fP) {
        chart = c;
        chartPanel = p;
        fullChartPanel = fP;
        singleSerie = s;
    }
}

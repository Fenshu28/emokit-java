//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import com.jonimikkola.EmoConfig;
import com.jonimikkola.utils.ChartContainer;
import com.jonimikkola.utils.FFTData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FFTView extends JPanel implements EmotivListener {
    private ChartContainer fftChartContainer;
    private HashMap<Packet.Sensor, FFTData> sensorValueMap;
    private ArrayList<JCheckBox> checkBoxes;
    private int counter = 0;
    private int frequency = 128;
    public FFTView() {
        fftChartContainer = createFFTChart();

        sensorValueMap = new HashMap<Packet.Sensor, FFTData>();
        checkBoxes = new ArrayList<JCheckBox>();

        JPanel sensorDisplayPanel = new JPanel();
        sensorDisplayPanel.setLayout(new BoxLayout(sensorDisplayPanel, BoxLayout.Y_AXIS));
        sensorDisplayPanel.setPreferredSize(new Dimension(280, 250));
        sensorDisplayPanel.add(fftChartContainer.fullChartPanel);

        setPreferredSize(new Dimension(300, 400));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0), "FFT"),
                BorderFactory.createEtchedBorder()));

        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            double[] emptyArray = new double[frequency];
            for(int j = 0; j < frequency; ++j) {
                emptyArray[j] = 0;
            }
            sensorValueMap.put(EmoConfig.sensorIds[i], new FFTData(emptyArray, false, frequency));
        }

        JPanel checkBoxLayout = new JPanel(new GridLayout(3, 7));
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            final JCheckBox checkbox = new JCheckBox(EmoConfig.sensorNames[i]);
            checkBoxes.add(checkbox);
            checkBoxLayout.add(checkbox);
            final int iteration = i;

            checkbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                if(checkbox.isSelected()) {
                    fftChartContainer.series.get(EmoConfig.sensorIds[iteration]).clear();
                }
                sensorValueMap.get(EmoConfig.sensorIds[iteration]).show = checkbox.isSelected();
                }
            });

            if(iteration == 0) {
                checkbox.setSelected(true);
            }
        }

        this.add(sensorDisplayPanel);
        this.add(checkBoxLayout);
    }

    private ChartContainer createFFTChart() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        HashMap<Packet.Sensor, XYSeries> seriesMap = new HashMap<Packet.Sensor, XYSeries>();
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            XYSeries series = new XYSeries(EmoConfig.sensorNames[i]);
            series.setMaximumItemCount(900);
            seriesMap.put(EmoConfig.sensorIds[i], series);
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart("", "Power spectrum",
                "", dataset, PlotOrientation.VERTICAL, false, false, false);
        ChartPanel p = new ChartPanel(chart);
        XYItemRenderer renderer = chart.getXYPlot().getRenderer();

        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            renderer.setSeriesPaint(i, EmoConfig.sensorColors[i]);
        }
        p.setPreferredSize(new Dimension(280, 40));
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(p, BorderLayout.EAST);
        chart.setBackgroundPaint(null);
        chartPanel.add(p, BorderLayout.EAST);
        return new ChartContainer(p, chart, seriesMap, chartPanel);
    }

    private void calculate() {
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            XYSeries series = fftChartContainer.series.get(EmoConfig.sensorIds[i]);
            series.clear();

            FFTData fftData = sensorValueMap.get(EmoConfig.sensorIds[i]);
            if(fftData.show) {
                double [] ys = fftData.calculate(frequency, frequency);
                for(int j = 0; j < frequency; ++j) {
                    series.add(j, ys[j]);
                }
            }
        }
    }

    @Override
    public void receivePacket(final Packet packet) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Map.Entry<Packet.Sensor, Integer> entry : packet.getSensors().entrySet()) {
                    sensorValueMap.get(entry.getKey()).data[counter] = entry.getValue() - EmoConfig.getMean(entry.getKey());
                }
                counter++;

                if (counter >= frequency) {
                    counter = 0;
                    calculate();
                }
            }
        });
    }

    @Override
    public void connectionBroken() {}
}

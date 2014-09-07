//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import com.jonimikkola.EmoConfig;
import com.jonimikkola.utils.ChartContainer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SensorView extends JPanel implements EmotivListener {
    private ChartContainer _chart;
    private int counter = 0;

    public SensorView() {
        super(new BorderLayout());
        JPanel sensorDisplayPanel = new JPanel();
        sensorDisplayPanel.setLayout(new BoxLayout(sensorDisplayPanel, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(690, 300));

        createChart();
        sensorDisplayPanel.add(_chart.fullChartPanel);

        this.add(sensorDisplayPanel, BorderLayout.EAST);
    }

    void createChart() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        HashMap<Packet.Sensor, XYSeries> seriesMap = new HashMap<Packet.Sensor, XYSeries>();
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            XYSeries series = new XYSeries(EmoConfig.sensorNames[i]);
            series.setMaximumItemCount(1200);
            seriesMap.put(EmoConfig.sensorIds[i], series);
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart("", "",
                "", dataset, PlotOrientation.VERTICAL, true, false, false);

        chart.setBorderVisible(false);
        chart.setTextAntiAlias(false);
        chart.setAntiAlias(false);
        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setRangePannable(true);
        ValueAxis axis = xyPlot.getDomainAxis();
        ValueAxis axis2 = xyPlot.getRangeAxis();
        axis.setVisible(false);
        axis2.setVisible(false);
        XYItemRenderer renderer = xyPlot.getRenderer();
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            renderer.setSeriesPaint(i, EmoConfig.sensorColors[i]);
        }

        renderer.setBaseItemLabelsVisible(false);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(null);
        xyPlot.getDomainAxis().setFixedAutoRange(1101);

        ChartPanel p = new ChartPanel(chart);
        p.setPopupMenu(null);
        p.setMouseZoomable(false);

        xyPlot.getDomainAxis().setAutoRange(true);
        NumberAxis yaxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        yaxis.setRange(0, 8000);
        p.setPreferredSize(new Dimension(690, 40));
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(p, BorderLayout.EAST);

        _chart = new ChartContainer(p, chart, seriesMap, chartPanel);
    }

    @Override
    public void receivePacket(final Packet packet) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Map.Entry<Packet.Sensor, Integer> entry : packet.getSensors().entrySet()) {
                    double value = entry.getValue() - EmoConfig.getMean(entry.getKey());
                    XYSeries s = _chart.series.get(entry.getKey());
                    s.add(counter, value + (entry.getKey().ordinal() * 520));
                }
                counter++;
            }
        });
    }

    @Override
    public void connectionBroken() { }
}

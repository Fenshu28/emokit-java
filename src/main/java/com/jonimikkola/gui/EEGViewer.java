//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.Packet;
import com.jonimikkola.EmoConfig;
import com.jonimikkola.utils.ChartContainer;
import com.jonimikkola.utils.RecordLoader;
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
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EEGViewer extends JPanel {
    private RecordLoader playRecord;
    private HashMap<Packet.Sensor, ChartContainer> sensorPanelMap;
    private ArrayList<JCheckBox> checkBoxes;
    private ArrayList<Packet> packets;
    private final JSlider playSlider;
    private final JSlider rangeSlider;
    private int range = 400;

    private ChartContainer createChart(String name, Color color) {
        final XYSeries series = new XYSeries("SensorData");

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart("", "",
                name, dataset, PlotOrientation.VERTICAL, false, false, false);
        chart.setBorderVisible(false);
        chart.setTextAntiAlias(false);
        chart.setAntiAlias(false);
        chart.setPadding(new RectangleInsets(0, -10, 0, -10));
        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setBackgroundPaint(Color.LIGHT_GRAY);

        xyPlot.setRangeGridlinesVisible(false);
        ValueAxis axis = xyPlot.getDomainAxis();
        ValueAxis axis2 = xyPlot.getRangeAxis();
        axis.setVisible(false);
        axis2.setVisible(false);
        XYItemRenderer renderer = xyPlot.getRenderer();

        renderer.setSeriesPaint(0, color);
        renderer.setBaseItemLabelsVisible(false);
        chart.setBorderVisible(false);

        ChartPanel p = new ChartPanel(chart);
        p.setMouseZoomable(false);

        NumberAxis yaxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        yaxis.setAutoRangeIncludesZero(false);
        xyPlot.getRangeAxis().setAutoRange(true);
        p.setPreferredSize(new Dimension(650, 40));
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(p, BorderLayout.EAST);

        JLabel chartNameLabel = new JLabel(name);
        chartNameLabel.setOpaque(true);
        chartNameLabel.setPreferredSize(new Dimension(40, 44));
        chartPanel.add(p, BorderLayout.EAST);
        chartPanel.add(chartNameLabel, BorderLayout.WEST);
        return new ChartContainer(p, chart, series, chartPanel);
    }

    public EEGViewer(final JFrame frame) {
        playRecord = new RecordLoader();
        sensorPanelMap = new HashMap<Packet.Sensor, ChartContainer>();
        checkBoxes = new ArrayList<JCheckBox>();

        final JPanel sensorDisplayPanel = new JPanel();
        sensorDisplayPanel.setLayout(new BoxLayout(sensorDisplayPanel, BoxLayout.Y_AXIS));
        sensorDisplayPanel.setPreferredSize(new Dimension(690, 600));

        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            ChartContainer chartContainer = createChart(EmoConfig.sensorNames[i], EmoConfig.sensorColors[i]);
            sensorDisplayPanel.add(chartContainer.fullChartPanel);
            sensorPanelMap.put(EmoConfig.sensorIds[i], chartContainer);
        }

        JPanel checkBoxLayout = new JPanel(new GridLayout(2, 7));
        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            final JCheckBox checkbox = new JCheckBox(EmoConfig.sensorNames[i]);
            checkBoxes.add(checkbox);
            checkBoxLayout.add(checkbox);
            final int iteration = i;
            checkbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    int visible = 0;
                    for(int j = 0; j < EmoConfig.NUM_SENSORS; ++j) {
                        if(sensorPanelMap.get(EmoConfig.sensorIds[j]).fullChartPanel.isVisible()) {
                            visible++;
                        }
                    }
                    if(visible <= 1) {
                        checkbox.setSelected(true);
                    }

                    sensorPanelMap.get(EmoConfig.sensorIds[iteration]).fullChartPanel.setVisible(checkbox.isSelected());

                    for(int j = 0; j < EmoConfig.NUM_SENSORS; ++j) {
                        sensorPanelMap.get(EmoConfig.sensorIds[j]).chartPanel.setPreferredSize(new Dimension(650, (int) (560 / visible)));
                    }
                }
            });
            checkbox.setSelected(true);
        }
        this.add(sensorDisplayPanel);
        playSlider = new JSlider(0, 500);
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
                        "emo files (*.emo)", "emo");
                fc.setFileFilter(xmlfilter);
                playSlider.setValue(0);
                int status = fc.showOpenDialog(frame);
                if (status == JFileChooser.APPROVE_OPTION) {
                    packets = playRecord.load(fc.getSelectedFile().getAbsolutePath());

                    for (int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
                        ChartContainer chartContainer = sensorPanelMap.get(EmoConfig.sensorIds[i]);
                        chartContainer.singleSerie.clear();
                    }

                    int c = 0;
                    for (int i = 0; i < packets.size(); ++i) {
                        Packet packet = packets.get(i);
                        for (Map.Entry<Packet.Sensor, Integer> entry : packet.getSensors().entrySet()) {
                            XYSeries s = sensorPanelMap.get(entry.getKey()).singleSerie;
                            s.add(c, entry.getValue());
                        }
                        c++;
                    }
                    refresh();
                }
            }
        });

        JPanel sliderGroup = new JPanel(new BorderLayout());

        playSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refresh();
            }
        });

        sliderGroup.add(playSlider, BorderLayout.WEST);

        playSlider.setPreferredSize(new Dimension(640, 20));
        JPanel borderPanel = new JPanel(new BorderLayout());
        JPanel rangePanel = new JPanel(new BorderLayout());
        rangeSlider = new JSlider(0, 800);

        rangeSlider.addChangeListener( new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                range = (rangeSlider.getMaximum() + 1) - rangeSlider.getValue();
                refresh();
            }

        });
        rangePanel.setPreferredSize(new Dimension(120, 50));

        rangePanel.add(new JLabel("Set zoom"), BorderLayout.NORTH);
        rangePanel.add(rangeSlider, BorderLayout.SOUTH);
        borderPanel.add(rangePanel, BorderLayout.WEST);
        borderPanel.add(loadButton, BorderLayout.EAST);

        this.add(sliderGroup);
        playSlider.setValue(0);
        playSlider.setEnabled(true);

        JPanel groupPanel = new JPanel(new BorderLayout());
        this.add(groupPanel);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(JCheckBox b : checkBoxes) {
                    b.setSelected(true);
                }
                for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
                    ChartContainer chartContainer = sensorPanelMap.get(EmoConfig.sensorIds[i]);
                    chartContainer.chart.getXYPlot().getDomainAxis().setAutoRange(true);
                    chartContainer.chart.getXYPlot().getRangeAxis().setAutoRange(true);
                }
                playSlider.setValue(0);
                rangeSlider.setValue(rangeSlider.getMaximum()/2);
            }
        });

        JPanel resetCheckboxGroupLayout = new JPanel(new BorderLayout());
        resetCheckboxGroupLayout.add(resetButton, BorderLayout.WEST);
        resetCheckboxGroupLayout.add(checkBoxLayout, BorderLayout.EAST);

        groupPanel.add(resetCheckboxGroupLayout, BorderLayout.WEST);
        groupPanel.setPreferredSize(new Dimension(680, 50));
        groupPanel.add(borderPanel, BorderLayout.EAST);
    }

    private void refresh() {
        if(packets != null) {
            int chartRange = Math.max(packets.size() - range, 0);
            playSlider.setMaximum((int)chartRange);
        }

        for(int i = 0; i < EmoConfig.NUM_SENSORS; ++i) {
            ChartContainer chartContainer = sensorPanelMap.get(EmoConfig.sensorIds[i]);
            chartContainer.chart.getXYPlot().getDomainAxis().setRange(playSlider.getValue(), playSlider.getValue() + range);
            NumberAxis a = (NumberAxis) chartContainer.chart.getXYPlot().getRangeAxis();
            a.configure();
        }
    }
}

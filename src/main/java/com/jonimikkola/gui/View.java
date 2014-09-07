//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.Emotiv;
import com.github.fommil.swing.SwingConvenience;
import com.jonimikkola.EmoConfig;
import com.jonimikkola.utils.SignalProcessing;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class View {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Emotool");
        SwingConvenience.enableOSXFullscreen(frame);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        EmoConfig.init();
        SignalProcessing processing = new SignalProcessing();

        JPanel sidebar = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JPanel topPanel = new JPanel(new BorderLayout());
        RecordPanel playRecordPanel = new RecordPanel(frame);

        BatteryView batteryView = new BatteryView();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        topPanel.add(batteryView, BorderLayout.EAST);
        topPanel.add(playRecordPanel, BorderLayout.WEST);

        sidebar.add(topPanel, c);

        SensorQualityView quality = new SensorQualityView();
        c.gridy = 1;
        sidebar.add(quality, c);

        FFTView brainView = new FFTView();
        c.gridy = 2;
        sidebar.add(brainView, c);
        sidebar.setPreferredSize(new Dimension(300, 500));

        frame.add(sidebar, BorderLayout.WEST);
        JTabbedPane activityPane = new JTabbedPane();
        final SensorView sensors = new SensorView();
        activityPane.addTab("EEG", null, sensors, "Display eeg data from brain");

        final EEGViewer viewer = new EEGViewer(frame);
        activityPane.addTab("Viewer", null, viewer, "Show recorded eeg data");
        frame.add(activityPane, BorderLayout.CENTER);

        frame.setVisible(true);
        try {
            Emotiv emotiv = new Emotiv();
            emotiv.addEmotivListener(playRecordPanel);
            emotiv.addEmotivListener(quality);
            emotiv.addEmotivListener(brainView);
            emotiv.addEmotivListener(batteryView);
            emotiv.addEmotivListener(sensors);
            emotiv.addEmotivListener(processing);
            emotiv.start();
        } catch (IOException e) {
            System.exit(1);
        }
    }
}

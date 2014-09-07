//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import com.google.common.primitives.Longs;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordPanel extends JPanel implements EmotivListener {
    private final JFileChooser fileChooser;
    private final JLabel statusLabel;
    private final JLabel fileLabel;
    private final JLabel timeLabel;
    private final JButton fileButton;
    private final JButton recordButton;
    private String filePath;
    private FileOutputStream byteWriter = null;
    private Boolean first;
    private Boolean recording;
    private Long startTime;
    public RecordPanel(final JFrame frame) {
        super(new BorderLayout());

        setPreferredSize(new Dimension(230, 120));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0), "Record"),
                BorderFactory.createEtchedBorder()));

        fileChooser = new JFileChooser();
        recording = false;
        fileButton = new JButton("Select file");
        recordButton = new JButton("Record");

        JPanel gridPanel = new JPanel(new GridLayout(3, 0));
        statusLabel = new JLabel("Status: not recording");
        gridPanel.add(statusLabel, BorderLayout.NORTH);

        fileLabel = new JLabel("File: -");
        gridPanel.add(fileLabel, BorderLayout.NORTH);

        timeLabel = new JLabel("Time: 0 ms");
        gridPanel.add(timeLabel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.NORTH);
        recordButton.setEnabled(false);
        recordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!recording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
                "emo files (*.emo)", "emo");
        fileChooser.setFileFilter(xmlfilter);

        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int status = fileChooser.showSaveDialog(frame);
                if(status == JFileChooser.APPROVE_OPTION) {
                    String fileString = fileChooser.getSelectedFile().getName();

                    if(!fileChooser.getSelectedFile().getAbsolutePath().endsWith(".emo")){
                        filePath = fileChooser.getSelectedFile() + ".emo";
                        fileString += ".emo";
                    } else {
                        filePath = fileChooser.getSelectedFile() + "";
                        fileChooser.getSelectedFile();
                    }

                    recordButton.setEnabled(true);
                    fileLabel.setText("File: " + fileString);
                }
            }
        });

        JPanel layout = new JPanel(new GridLayout(0, 2));
        layout.add(recordButton);
        layout.add(fileButton);

        this.add(layout, BorderLayout.SOUTH);
    }

    public void startRecording() {
        recordButton.setText("Stop");
        fileButton.setEnabled(false);
        statusLabel.setText("Status: recording");

        try {
            byteWriter = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            first = true;
            recording = true;
        }
    }

    public void stopRecording() {
        recording = false;
        recordButton.setText("Record");
        statusLabel.setText("Status: not recording");
        fileButton.setEnabled(true);

        try {
            byteWriter.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            byteWriter = null;
        }
    }

    public void record(Packet packet) throws IOException {
        if(byteWriter != null) {
            if(first) {
                startTime = System.currentTimeMillis();
                first = false;
            }
            byte[] array = packet.getFrame();
            long timeMs = System.currentTimeMillis() - startTime;
            byte[] timeBytes = Longs.toByteArray(timeMs);
            timeLabel.setText("Time: " + timeMs + " ms");
            byteWriter.write(timeBytes);
            byteWriter.write(array);
        }
    }

    @Override
    public void receivePacket(final Packet packet) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(recording) {
                    try {
                        record(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void connectionBroken() {}
}
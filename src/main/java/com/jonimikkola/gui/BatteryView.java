//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;

import javax.swing.*;
import java.awt.*;

public class BatteryView extends JPanel implements EmotivListener {
    private final JProgressBar bar;
    public BatteryView() {
        setPreferredSize(new Dimension(65, 120));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), "Charge"),
                BorderFactory.createEtchedBorder()));
        bar = new JProgressBar(JProgressBar.VERTICAL, 0, 100);
        bar.setValue(0);
        bar.setStringPainted(true);
        bar.setPreferredSize(new Dimension(50, 80));

        add(bar);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public void receivePacket(Packet packet) {
        int battery = packet.getBatteryLevel();
        bar.setValue(battery);
        repaint();
    }

    @Override
    public void connectionBroken() { }
}

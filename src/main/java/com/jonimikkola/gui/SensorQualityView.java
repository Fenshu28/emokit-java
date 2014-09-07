//Copyright Joni Mikkola 2014

package com.jonimikkola.gui;

import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import com.github.fommil.emokit.Packet.Sensor;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import lombok.Cleanup;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SensorQualityView extends JPanel implements EmotivListener {

    private final BufferedImage image;
    private final Map<Color, BufferedImage> stateImages;
    private final Map<Sensor, Point> sensors = Maps.newHashMap();
    private final Point position;

    private volatile Map<Sensor, Integer> quality = Maps.newEnumMap(Sensor.class);

    public SensorQualityView() {
        super(new GridBagLayout());

        setPreferredSize(new Dimension(300, 200));
        stateImages = new HashMap<Color, BufferedImage>();
        Config config = ConfigFactory.load().getConfig("com.jonimikkola.gui.quality");
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0), "Contact Quality"),
                BorderFactory.createEtchedBorder()));
        position = new Point(5, 30);

        try {
            @Cleanup InputStream stream = getClass().getResourceAsStream(config.getString("image"));
            image = ImageIO.read(stream);

            stateImages.put(Color.BLACK, ImageIO.read(getClass().getResourceAsStream(config.getString("level0"))));
            stateImages.put(Color.RED, ImageIO.read(getClass().getResourceAsStream(config.getString("level1"))));
            stateImages.put(Color.ORANGE, ImageIO.read(getClass().getResourceAsStream(config.getString("level2"))));
            stateImages.put(Color.GREEN, ImageIO.read(getClass().getResourceAsStream(config.getString("level3"))));

            Config positions = config.getConfig("positions");
            for (Sensor sensor : Sensor.values()) {
                if (sensor == Sensor.QUALITY)
                    continue;
                Config position = positions.getConfig(sensor.name());
                sensors.put(sensor, new Point(
                        Integer.parseInt(position.getString("x")),
                        Integer.parseInt(position.getString("y"))
                ));
            }
        } catch (IOException e) {
            throw new ConfigException.Missing(config.getString("image"));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(image, position.x, position.y, null);
        for (Map.Entry<Sensor, Point> entry: sensors.entrySet()) {
            Sensor sensor = entry.getKey();

            Integer level = quality.get(sensor);
            if (level == null) continue;
            Color color = levelToColor(level);

            Point point = entry.getValue();
            g.drawImage(stateImages.get(color), position.x + point.x, position.y + point.y, null);
        }
    }

    private Color levelToColor(Integer level) {
        float percent = level / 5000.0f;

        if (percent >= 0.8f) return Color.GREEN;
        if (percent >= 0.6f) return Color.ORANGE;
        if (percent >= 0.2f) return Color.RED;
        return Color.BLACK;
    }

    @Override
    public void receivePacket(Packet packet) {
        quality = packet.getQuality();
        repaint();
    }

    @Override
    public void connectionBroken() { }
}

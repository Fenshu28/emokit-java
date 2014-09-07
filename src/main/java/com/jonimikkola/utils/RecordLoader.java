//Copyright Joni Mikkola 2014

package com.jonimikkola.utils;

import com.github.fommil.emokit.EmotivParser;
import com.github.fommil.emokit.Packet;
import com.google.common.primitives.Longs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RecordLoader {
    private FileInputStream byteReader;
    private int LongInBytes = 8;
    private int DataInBytes = 32;

    public RecordLoader() {}

    public ArrayList<Packet> load(String file) {
        File f = null;
        ArrayList<Packet> packets = new ArrayList<Packet>();
        try {
            f = new File(file);
            byteReader = new FileInputStream(f);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            byte[] bytes = new byte[(int) f.length()];
            try {
                byteReader.read(bytes);

                int index = 0;
                while(index < f.length()) {
                    long timestamp = Longs.fromBytes(bytes[index], bytes[index+1], bytes[index+2], bytes[index+3], bytes[index+4], bytes[index+5], bytes[index+6], bytes[index+7]);
                    byte[] packet = new byte[DataInBytes];
                    index += LongInBytes;
                    int c = 0;
                    for(int i = index; i < index + DataInBytes; i++) {
                        packet[c] = bytes[i];
                        c++;
                    }
                    index += DataInBytes;
                    packets.add(EmotivParser.parseRecord(timestamp, packet));
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    byteReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return packets;
    }
}
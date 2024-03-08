// Copyright Samuel Halliday 2012
package com.github.fommil.emokit;

import com.codeminders.hidapi.*;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;

import javax.annotation.concurrent.NotThreadSafe;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

/**
 * Wrapper for the low level HIDAPI to access an Emotiv EEG.
 * <p/>
 * Supported devices are discovered on construction and a poll is provided to
 * obtain raw packets.
 *
 * @author Sam Halliday
 */
@Log
@NotThreadSafe
final class EmotivHid implements Closeable {

    static final int VENDOR_ID = 4660;
    static final int PRODUCT_ID = 60674;
    static final int BUFSIZE = 32; // at 128hz
    static final int TIMEOUT = 1000;

    private static final List<byte[]> supportedResearch = Lists.newArrayList();
    private static final List<byte[]> supportedConsumer = Lists.newArrayList();

    static {
        try {
            ClassPathLibraryLoader.loadNativeHIDLibrary();
            // 0x00, 0xa0, 0xff, 0x1f, 0xff, 0x00, 0x00, 0x00, 0x00
            // 0, 160, 255, 31, 255, 0, 0, 0 ,0
            // 0x32 0x00 0x31 0x48 0x39 0x00 0x38 0x54 0x32 0x10 0x31 0x42 0x39 0x00 0x38 0x50
            // 0x31,0x00,0x35,0x54,0x38,0x10,0x37,0x42,0x31,0x00,0x35,0x48,0x38,0x00,0x37,0x50
            supportedConsumer.add(new byte[]{33, -1, 31, -1, 30, 0, 0, 0});
            supportedConsumer.add(new byte[]{32, -1, 31, -1, 30, 0, 0, 0});
            supportedConsumer.add(new byte[]{-32, -1, 31, -1, 0, 0, 0, 0}); // unconfirmed
//            supportedConsumer.add(new byte[]{0,160});

//            supportedConsumer.add(new byte[]{0x31, 0x00, 0x35, 0x54, 0x38, 0x10, 0x37, 0x42, 0x31, 0x00, 0x35, 0x48, 0x38, 0x00, 0x37, 0x50});
//            supportedConsumer.add(new byte[]{0, 32, 14, 6, -1, 48, 17, 59, -102, -54, -10, 43, 0, -128, 0, 14, -128});
//            supportedConsumer.add(new byte[]{0, 32, 15, 6, -1, 48, 17, 59, -102, -54, -10, 43, 0, -128, 0, 14, -128});
//            supportedConsumer.add(new byte[]{0x32, 0x00, 0x31, 0x48, 0x39, 0x00, 0x38, 0x54, 0x32, 0x10, 0x31, 0x42, 0x39, 0x00, 0x38, 0x50});
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private volatile boolean research = false;
    private final HIDDevice device;
//    @Getter
    private volatile boolean closed;

    public static List<byte[]> getSupportedResearch() {
        return supportedResearch;
    }

    public static List<byte[]> getSupportedConsumer() {
        return supportedConsumer;
    }

    public boolean isResearch() {
        return research;
    }

    public HIDDevice getDevice() {
        return device;
    }

    public boolean isClosed() {
        return closed;
    }

    public EmotivHid() throws IOException {
        device = findEmotiv();
        device.enableBlocking();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        device.close();
    }

    @Override
    public void finalize() throws Throwable {
        synchronized (this) {
            close();
            super.finalize();
        }
    }

    /**
     * @param buf use the supplied buffer.
     * @throws java.io.IOException if there was no response from the Emotiv.
     * @throws TimeoutException which may indicate that the Emotiv is not
     * connected.
     */
    public byte[] poll(byte[] buf) throws TimeoutException, IOException {
        assert buf.length == BUFSIZE;

        int n;
        long startTime = currentTimeMillis();
        while ((n = device.readTimeout(buf, 0)) == 0 && currentTimeMillis() - startTime < TIMEOUT) {
            try {
                // limits us to 100Hz samples
                // http://stackoverflow.com/questions/11094857
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            Thread.yield();
        }

        if (n != BUFSIZE) {
            throw new IOException(format("Bad Packet: (%s) %s", n, Arrays.toString(buf)));
        }
        return buf;
    }

    /**
     * @return the crypto key for this device.
     * @throws IOException
     */
//    def epoc_plus_crypto_key(serial_number, verbose=False):
//    k = ['\0'] * 16
//    k[0] = serial_number[-1]
//    k[1] = '\x00'
//    k[2] = serial_number[-2]
//    k[3] = '\x15'
//    k[4] = serial_number[-3]
//    k[5] = '\x00'
//    k[6] = serial_number[-4]
//    k[7] = '\x0C'
//    k[8] = serial_number[-3]
//    k[9] = '\x00'
//    k[10] = serial_number[-2]
//    k[11] = 'D'
//    k[12] = serial_number[-1]
//    k[13] = '\x00'
//    k[14] = serial_number[-2]
//    k[15] = 'X'
//    if verbose:
//        print("EmotivCrypto: Generated Crypto Key from Serial Number...\n"
//              "   Serial Number - {serial_number} \n"
//              "   AES KEY - {aes_key}".format(serial_number=serial_number, aes_key=k))
//
//    return ''.join(k)
    public SecretKeySpec getKey() throws IOException {
        String serial = getSerial();

        byte[] raw = serial.getBytes();
        assert raw.length == 16;
        byte[] bytes = new byte[16];

//        bytes[0] = raw[14];
//        bytes[1] = 0;
//        bytes[2] = raw[13];
//        bytes[3] = 15;
//        bytes[4] = raw[12];
//        bytes[5] = 0;
//        bytes[6] = raw[11];
//        bytes[7] = 12;
//        bytes[8] = raw[12];
//        bytes[9] = 0;
//        bytes[10] = raw[13];
//        bytes[11] = (byte) 'D';
//        bytes[12] = raw[14];
//        bytes[13] = 0;
//        bytes[14] = raw[13];
//        bytes[15] = (byte)'X' ;
        bytes[0] = raw[15];
        bytes[1] = 0;
        bytes[2] = raw[14];
        bytes[3] = research ? (byte) 'H' : (byte) 'T';
        bytes[4] = research ? raw[15] : raw[13];
        bytes[5] = research ? (byte) 0 : 16;
        bytes[6] = research ? raw[14] : raw[12];
        bytes[7] = research ? (byte) 'T' : (byte) 'B';
        bytes[8] = research ? raw[13] : raw[15];
        bytes[9] = research ? (byte) 16 : 0;
        bytes[10] = research ? raw[12] : raw[14];
        bytes[11] = research ? (byte) 'B' : (byte) 'H';
        bytes[12] = raw[13];
        bytes[13] = 0;
        bytes[14] = raw[12];
        bytes[15] = 'P';

        return new SecretKeySpec(bytes, "AES");
    }

    /**
     * @return
     */
    public String getSerial() throws IOException {
        String serial = device.getSerialNumberString();
        if (!serial.startsWith("UD") || serial.length() != 16) {
            throw new IOException("Bad serial: " + serial);
        }
        return serial;
//        return "";
    }

    // workaround http://code.google.com/p/javahidapi/issues/detail?id=40
    private List<HIDDeviceInfo> findDevices(int vendor, int product) throws IOException {
        HIDManager manager = HIDManager.getInstance();
        HIDDeviceInfo[] infos = manager.listDevices();
        List<HIDDeviceInfo> devs = Lists.newArrayList();
        for (HIDDeviceInfo info : infos) {
            if (info.getVendor_id() == vendor && info.getProduct_id() == product) {
                devs.add(info);
            }
        }
        return devs;
    }

    private HIDDevice findEmotiv() throws IOException {
        List<HIDDeviceInfo> infos = findDevices(VENDOR_ID, PRODUCT_ID);
//        ;
        for (HIDDeviceInfo info : infos) {
            HIDDevice dev = info.open();

//            if(infos.get(0).getProduct_string().equals("Brain Computer Interface USB Receiver/Dongle")){
//                return dev;
//            }
            try {
                byte[] report = new byte[20];
                int size = dev.getFeatureReport(report);
                byte[] result = Arrays.copyOf(report, size);
                System.out.println(format("Found (%s) %s [%s] with report: %s",
                        dev.getManufacturerString(),
                        dev.getProductString(),
                        dev.getSerialNumberString(),
                        Arrays.toString(result)));
                for (byte[] check : supportedConsumer) {
                    if (Arrays.equals(check, result)) {
                        return dev;
                    }
                }
//                for (byte[] check : supportedResearch) {
//                    if (Arrays.equals(check, result)) {
                        research = true;
                        return dev;
//                    }
//                }
//                dev.close();
            } catch (Exception e) {
                dev.close();
                System.out.println(e.getMessage());
//                e.printStackTrace();
            }
        }
        throw new HIDDeviceNotFoundException("Send all this information to https://github.com/fommil/emokit-java/issues and let us know if you have the 'research' or 'consumer' product.");
    }

}

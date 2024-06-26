// Copyright Samuel Halliday 2012
package com.github.fommil.emokit;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.annotation.concurrent.NotThreadSafe;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unencrypted access to an Emotiv EEG.
 * <p/>
 * The device is constantly polled in a background thread,
 * filling up a buffer (which could cause the application
 * to OutOfMemory if not evacuated).
 *
 * @author Sam Halliday
 */
@Log
@NotThreadSafe
public final class Emotiv implements Closeable {

//    public static void main(String[] args) throws Exception {
//        Emotiv emotiv = new Emotiv();
//
//        final Condition condition = new ReentrantLock().newCondition();
//
//        emotiv.addEmotivListener(new EmotivListener() {
//            @Override
//            public void receivePacket(Packet packet) {
//
//            }
//
//            @Override
//            public void connectionBroken() {
//                condition.signal();
//            }
//        });
//
//        emotiv.start();
//        condition.await();
//    }


    private EmotivHid raw;
    private final AtomicBoolean accessed = new AtomicBoolean();
    private Cipher cipher;
    private final EmotivParser parser = new EmotivParser();

    @Getter
    private String serial;

    private Executor executor;

    /**
     * @throws IOException if there was a problem discovering the device.
     */
    public Emotiv() throws IOException {
        raw = new EmotivHid();
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec key = raw.getKey();
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            throw new IllegalStateException("no javax.crypto support");
        }
        serial = raw.getSerial();

        Config config = ConfigFactory.load().getConfig("com.github.fommil.emokit");
        int threads = config.getInt("threads");
        executor = Executors.newFixedThreadPool(threads);
    }

    /**
     * Poll the device in a background thread and sends signals to registered
     * listeners using a thread pool.
     */
    public void start() {
        if (accessed.getAndSet(true))
            throw new IllegalStateException("Cannot be called more than once.");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    poller();
                } catch (Exception e) {
//                    Emotiv.log.log(Level.SEVERE, "Problem when polling", e);
                    try {
                        close();
                    } catch (IOException ignored) {
                    }
                    fireConnectionBroken();
                }
            }
        };

        Thread thread = new Thread(runnable, "Emotiv polling and decryption");
        thread.setDaemon(true);
        thread.start();
    }

    private void poller() throws TimeoutException, IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] bytes = new byte[EmotivHid.BUFSIZE];

        while (!raw.isClosed()) {
            raw.poll(bytes);

            long timestamp = System.currentTimeMillis();
            byte[] decrypted = cipher.doFinal(bytes);

            Packet packet = parser.parse(timestamp, decrypted);
            fireReceivePacket(packet);
        }
    }

    @Override
    public void close() throws IOException {
        raw.close();
    }

    // https://github.com/peichhorn/lombok-pg/issues/139
    protected void fireReceivePacket(final Packet arg0) {
        for (final EmotivListener l : $registeredEmotivListener) {
            Runnable runnable = new Runnable(){
                @Override
                public void run() {
                    l.receivePacket(arg0);
                }
            };
            executor.execute(runnable);
        }
    }

    protected void fireConnectionBroken() {
        for (final EmotivListener l : $registeredEmotivListener) {
            Runnable runnable = new Runnable(){
                @Override
                public void run() {
                    l.connectionBroken();
                }
            };
            executor.execute(runnable);
        }
    }

    // http://code.google.com/p/projectlombok/issues/detail?id=460
    private final List<EmotivListener> $registeredEmotivListener = Lists.newCopyOnWriteArrayList();
    public void addEmotivListener(final EmotivListener l) {
        if (!$registeredEmotivListener.contains(l)) $registeredEmotivListener.add(l);
    }
    public void removeEmotivListener(final EmotivListener l) {
        $registeredEmotivListener.remove(l);
    }

}

package com.fenshu.transmision;

import com.github.fommil.emokit.Packet;
import com.google.gson.Gson;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Cristopher
 */
public class Transmision {

    private String dir;
    private int puerto;
    private Gson jsonBuilder;

    public Transmision(String dir, int puerto) {
        this.dir = dir;
        this.puerto = puerto;
        jsonBuilder = new Gson();
    }

    public void sent(Packet packet) {
//        try (Socket socket = new Socket(dir, puerto)) {
//            // Obtener el stream de salida del socket
//            OutputStream outputStream = socket.getOutputStream();
//            PrintWriter writer = new PrintWriter(outputStream, true);
//
//            // Crear un paquete de datos para enviar
//            String signalData = "Este es un paquete de senales EEG ";
//
//            writer.println(signalData + i);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println(packet.toJson());
    }
}

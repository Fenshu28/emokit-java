/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fenshu.view;

import com.fenshu.transmision.Transmision;
import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Cristopher
 */
public class TransmisionPanel extends JPanel implements EmotivListener {

    private JTextField txtDireccion;
    private JTextField txtPuerto;
    private JButton btnTransmitir;

    private Boolean isTransmitiendo = false;

    private Transmision transmision;

    public TransmisionPanel() {
        super(new BorderLayout());

        // Crear el campo de texto formateado
        txtDireccion = new JTextField("127.0.0.1");
        txtDireccion.setColumns(15);

        txtPuerto = new JTextField("3000");

        btnTransmitir = new JButton("Transmitir");
        btnTransmitir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transmision = new Transmision(txtDireccion.getText(),
                        Integer.parseInt(txtPuerto.getText()));
                isTransmitiendo = true;
            }
        }
        );

        JPanel layout = new JPanel(new GridLayout(0, 2));

        layout.add(new JLabel("Dirección"));
        layout.add(new JLabel("Puerto"));
        layout.add(txtDireccion);
        layout.add(txtPuerto);
        layout.add(btnTransmitir);

        this.add(layout, BorderLayout.NORTH);
    }

    @Override
    public void receivePacket(Packet packet) {
        if (isTransmitiendo) {
            transmision.sent(packet);
        }
    }

    @Override
    public void connectionBroken() {
    }

}

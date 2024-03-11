package com.fenshu.view;

import com.github.fommil.emokit.Emotiv;
import java.io.IOException;

public class PrincipalFrame extends javax.swing.JFrame {

    public PrincipalFrame() {
        initComponents();
        try {
            Emotiv emotiv = new Emotiv();
//            emotiv.addEmotivListener(playRecordPanel);
//            emotiv.addEmotivListener(quality);
//            emotiv.addEmotivListener(brainView);
//            emotiv.addEmotivListener(batteryView);
//            emotiv.addEmotivListener(sensors);
//            emotiv.addEmotivListener(processing);
            emotiv.start();
        } catch (IOException e) {
//            System.exit(1);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        eEGViewer1 = new com.jonimikkola.gui.EEGViewer();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout eEGViewer1Layout = new javax.swing.GroupLayout(eEGViewer1);
        eEGViewer1.setLayout(eEGViewer1Layout);
        eEGViewer1Layout.setHorizontalGroup(
            eEGViewer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 688, Short.MAX_VALUE)
        );
        eEGViewer1Layout.setVerticalGroup(
            eEGViewer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 442, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eEGViewer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eEGViewer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrincipalFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jonimikkola.gui.EEGViewer eEGViewer1;
    // End of variables declaration//GEN-END:variables
}

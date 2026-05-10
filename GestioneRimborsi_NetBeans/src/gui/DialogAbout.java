package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra "About" con informazioni sull'applicazione e sui crediti.
 * È un semplice JDialog non ridimensionabile con testo centrato.
 */
public class DialogAbout extends JDialog {

    public DialogAbout(Frame owner) {
        super(owner, "Informazioni sull'applicazione", true);

        JPanel pannello = new JPanel(new BorderLayout(10, 10));
        pannello.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        pannello.setBackground(Color.WHITE);

        // ── Titolo ────────────────────────────────────────────────────────────
        JLabel labelTitolo = new JLabel("🧾  Gestione Rimborsi Spese", SwingConstants.CENTER);
        labelTitolo.setFont(new Font("Arial", Font.BOLD, 18));
        labelTitolo.setForeground(new Color(25, 118, 210));
        pannello.add(labelTitolo, BorderLayout.NORTH);

        // ── Corpo testo ───────────────────────────────────────────────────────
        String testo =
            "<html><body style='font-family:Arial; font-size:12px; text-align:center;'>"
          + "<br><b>Versione:</b> 1.0.0<br><br>"
          + "Applicazione per la gestione delle note spese aziendali.<br>"
          + "Permette di caricare, visualizzare, filtrare e approvare<br>"
          + "le richieste di rimborso dei dipendenti.<br><br>"
          + "<hr><br>"
          + "<b>Crediti</b><br><br>"
          + "Sviluppata come progetto didattico per il corso di<br>"
          + "<i>Informatica — Gestione Aziendale</i><br><br>"
          + "Tecnologie usate: Java 11+, Java Swing<br>"
          + "Formato dati: CSV (Comma Separated Values)<br><br>"
          + "© 2024 — Tutti i diritti riservati<br>"
          + "</body></html>";

        JLabel labelTesto = new JLabel(testo, SwingConstants.CENTER);
        pannello.add(labelTesto, BorderLayout.CENTER);

        // ── Pulsante Chiudi ───────────────────────────────────────────────────
        JButton btnChiudi = new JButton("Chiudi");
        btnChiudi.addActionListener(e -> dispose());

        JPanel pannelloPulsante = new JPanel();
        pannelloPulsante.setOpaque(false);
        pannelloPulsante.add(btnChiudi);
        pannello.add(pannelloPulsante, BorderLayout.SOUTH);

        add(pannello);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}

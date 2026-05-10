package gui;

import util.ConfigReader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * Finestra di login modale che appare all'avvio dell'applicazione.
 *
 * ─── Swing usato qui ──────────────────────────────────────────────────────────
 *
 * JDialog con modalità "modal" blocca l'interazione con le altre finestre
 * finché non viene chiusa. È la scelta giusta per login, conferme, dettagli.
 *
 * setLocationRelativeTo(null) centra la finestra sullo schermo.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class DialogLogin extends JDialog {

    private static final String PERCORSO_CONFIG = "config.ini";

    // true se il login è andato a buon fine
    private boolean accessoConsentito = false;

    // Campi di input
    private JTextField     campoUsername;
    private JPasswordField campoPassword; // JPasswordField nasconde i caratteri con *

    // ── Costruttore ──────────────────────────────────────────────────────────

    /**
     * @param owner  La finestra padre (può essere null all'avvio)
     */
    public DialogLogin(Frame owner) {
        // true = finestra modale (blocca il parent finché non si chiude)
        super(owner, "Accesso — Gestione Rimborsi", true);

        costruisciUI();

        // Impostazioni finestra
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();                          // Ridimensiona in base ai componenti
        setResizable(false);
        setLocationRelativeTo(null);     // Centra sullo schermo
    }

    // ── Costruzione UI ────────────────────────────────────────────────────────

    private void costruisciUI() {
        JPanel pannello = new JPanel(new GridBagLayout());
        pannello.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        pannello.setBackground(new Color(250, 250, 250));

        // GridBagConstraints controlla posizione e comportamento di ogni componente
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); // Margini interni
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // ── Titolo ────────────────────────────────────────────────────────────
        JLabel labelTitolo = new JLabel("Gestione Rimborsi Spese");
        labelTitolo.setFont(new Font("Arial", Font.BOLD, 16));
        labelTitolo.setForeground(new Color(33, 33, 33));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pannello.add(labelTitolo, gbc);

        // ── Separatore ────────────────────────────────────────────────────────
        gbc.gridy = 1;
        pannello.add(new JSeparator(), gbc);

        // ── Label + campo Username ─────────────────────────────────────────────
        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        pannello.add(new JLabel("Username:"), gbc);

        campoUsername = new JTextField(18);
        gbc.gridx = 1;
        pannello.add(campoUsername, gbc);

        // ── Label + campo Password ─────────────────────────────────────────────
        gbc.gridy = 3; gbc.gridx = 0;
        pannello.add(new JLabel("Password:"), gbc);

        campoPassword = new JPasswordField(18);
        // Premi Invio nel campo password = click su Accedi
        campoPassword.addActionListener(e -> tentaLogin());
        gbc.gridx = 1;
        pannello.add(campoPassword, gbc);

        // ── Messaggio di errore (inizialmente invisibile) ──────────────────────
        JLabel labelErrore = new JLabel(" ");
        labelErrore.setForeground(Color.RED);
        labelErrore.setFont(new Font("Arial", Font.ITALIC, 11));
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        pannello.add(labelErrore, gbc);

        // ── Pulsanti ──────────────────────────────────────────────────────────
        JPanel pannelloPulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pannelloPulsanti.setOpaque(false);

        JButton btnAnnulla = new JButton("Annulla");
        btnAnnulla.addActionListener(e -> {
            accessoConsentito = false;
            dispose(); // Chiude il dialog
        });

        JButton btnAccedi = new JButton("Accedi");
        btnAccedi.setBackground(new Color(25, 118, 210));
        btnAccedi.setForeground(Color.WHITE);
        btnAccedi.addActionListener(e -> {
            // Verifica le credenziali; se errate mostra il messaggio
            if (!tentaLogin()) {
                labelErrore.setText("Username o password errati.");
                campoPassword.setText("");
                campoPassword.requestFocus();
            }
        });

        pannelloPulsanti.add(btnAnnulla);
        pannelloPulsanti.add(btnAccedi);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        pannello.add(pannelloPulsanti, gbc);

        add(pannello);
    }

    // ── Logica di login ───────────────────────────────────────────────────────

    /**
     * Confronta le credenziali inserite con quelle nel config.ini.
     *
     * @return true se le credenziali sono corrette
     */
    private boolean tentaLogin() {
        String username = campoUsername.getText().trim();
        // getPassword() restituisce char[] per sicurezza; convertiamo in String
        String password = new String(campoPassword.getPassword()).trim();

        try {
            Map<String, String> config = ConfigReader.leggi(PERCORSO_CONFIG);
            String utenteAtteso   = config.getOrDefault("username", "");
            String passwordAttesa = config.getOrDefault("password", "");

            if (username.equals(utenteAtteso) && password.equals(passwordAttesa)) {
                accessoConsentito = true;
                dispose(); // Chiude il dialog → l'applicazione principale si avvia
                return true;
            }
        } catch (IOException ex) {
            // config.ini non trovato: avvisiamo e consentiamo accesso di default
            JOptionPane.showMessageDialog(this,
                "File config.ini non trovato.\nAccesso consentito senza credenziali.",
                "Attenzione", JOptionPane.WARNING_MESSAGE);
            accessoConsentito = true;
            dispose();
            return true;
        }
        return false;
    }

    /** Restituisce true se il login è avvenuto con successo. */
    public boolean isAccessoConsentito() {
        return accessoConsentito;
    }
}

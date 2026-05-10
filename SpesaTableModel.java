package gui;

import model.SpesaDipendente;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra modale per INSERIRE una nuova spesa o MODIFICARE una esistente.
 *
 * ─── Perché JDialog e non JFrame? ────────────────────────────────────────────
 *
 * JDialog è pensato per finestre figlie dipendenti dalla finestra principale.
 * Passando "true" al costruttore di JDialog lo rendiamo MODALE: l'utente
 * non può interagire con la finestra principale finché non chiude questo dialog.
 *
 * ─── Pattern usato ────────────────────────────────────────────────────────────
 *
 * Dopo aver mostrato il dialog con setVisible(true), il codice chiamante
 * legge isConfermato() per sapere se l'utente ha salvato o annullato,
 * e getSpesaModificata() per ottenere l'oggetto aggiornato.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class DialogSpesa extends JDialog {

    // Esito del dialog: true = utente ha cliccato Salva
    private boolean confermato = false;

    // La spesa modificata/creata (null se annullato)
    private SpesaDipendente spesaRisultato = null;

    // ── Campi del form ────────────────────────────────────────────────────────
    private JTextField     campoDipendente;
    private JTextField     campoReparto;
    private JTextField     campoData;        // Formato YYYY-MM-DD
    private JComboBox<String> comboCategoria;
    private JTextField     campoDescrizione;
    private JTextField     campoImporto;
    private JComboBox<String> comboStato;

    // ID della spesa (usato in modifica; -1 se è una nuova spesa)
    private int idSpesa;

    // ── Costruttore ──────────────────────────────────────────────────────────

    /**
     * @param owner        Finestra padre
     * @param spesaEsistente  null = nuova spesa; non-null = modifica
     * @param prossimoId   ID da assegnare se si sta inserendo una nuova spesa
     */
    public DialogSpesa(Frame owner, SpesaDipendente spesaEsistente, int prossimoId) {
        super(owner, spesaEsistente == null ? "Nuova Spesa" : "Modifica Spesa", true);

        this.idSpesa = (spesaEsistente == null) ? prossimoId : spesaEsistente.getIdSpesa();

        costruisciUI(spesaEsistente);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner); // Centra rispetto alla finestra principale
    }

    // ── Costruzione UI ────────────────────────────────────────────────────────

    private void costruisciUI(SpesaDipendente spesa) {
        JPanel pannello = new JPanel(new GridBagLayout());
        pannello.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Helper per aggiungere una riga: etichetta (col 0) + campo (col 1)
        int riga = 0;

        // ── Dipendente ────────────────────────────────────────────────────────
        campoDipendente = new JTextField(20);
        aggiungiRiga(pannello, gbc, riga++, "Dipendente *:", campoDipendente);

        // ── Reparto ───────────────────────────────────────────────────────────
        campoReparto = new JTextField(20);
        aggiungiRiga(pannello, gbc, riga++, "Reparto *:", campoReparto);

        // ── Data ──────────────────────────────────────────────────────────────
        campoData = new JTextField(20);
        campoData.setToolTipText("Formato: YYYY-MM-DD, es. 2024-03-15");
        aggiungiRiga(pannello, gbc, riga++, "Data (YYYY-MM-DD) *:", campoData);

        // ── Categoria (JComboBox) ──────────────────────────────────────────────
        comboCategoria = new JComboBox<>(new String[]{
            SpesaDipendente.CAT_VITTO,
            SpesaDipendente.CAT_TRASPORTO,
            SpesaDipendente.CAT_ALLOGGIO,
            SpesaDipendente.CAT_ALTRO
        });
        aggiungiRiga(pannello, gbc, riga++, "Categoria *:", comboCategoria);

        // ── Descrizione ───────────────────────────────────────────────────────
        campoDescrizione = new JTextField(20);
        aggiungiRiga(pannello, gbc, riga++, "Descrizione *:", campoDescrizione);

        // ── Importo ───────────────────────────────────────────────────────────
        campoImporto = new JTextField(20);
        campoImporto.setToolTipText("Usa il punto come separatore decimale, es. 25.50");
        aggiungiRiga(pannello, gbc, riga++, "Importo (€) *:", campoImporto);

        // ── Stato (JComboBox) ─────────────────────────────────────────────────
        comboStato = new JComboBox<>(new String[]{
            SpesaDipendente.STATO_IN_ATTESA,
            SpesaDipendente.STATO_APPROVATA,
            SpesaDipendente.STATO_RIFIUTATA
        });
        aggiungiRiga(pannello, gbc, riga++, "Stato:", comboStato);

        // ── Nota (*) obbligatori ──────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = riga++; gbc.gridwidth = 2;
        pannello.add(new JLabel("* Campo obbligatorio"), gbc);

        // ── Pulsanti Salva / Annulla ───────────────────────────────────────────
        JPanel pannelloPulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton btnAnnulla = new JButton("Annulla");
        btnAnnulla.addActionListener(e -> dispose());

        JButton btnSalva = new JButton("💾 Salva");
        btnSalva.setBackground(new Color(25, 118, 210));
        btnSalva.setForeground(Color.WHITE);
        btnSalva.addActionListener(e -> salva());

        pannelloPulsanti.add(btnAnnulla);
        pannelloPulsanti.add(btnSalva);

        gbc.gridx = 0; gbc.gridy = riga; gbc.gridwidth = 2;
        pannello.add(pannelloPulsanti, gbc);

        add(pannello);

        // ── Pre-popola i campi se siamo in modalità modifica ──────────────────
        if (spesa != null) {
            campoDipendente.setText(spesa.getNomeDipendente());
            campoReparto.setText(spesa.getReparto());
            campoData.setText(spesa.getData());
            comboCategoria.setSelectedItem(spesa.getCategoria());
            campoDescrizione.setText(spesa.getDescrizione());
            campoImporto.setText(String.valueOf(spesa.getImporto()));
            comboStato.setSelectedItem(spesa.getStato());
        }
    }

    /** Aggiunge una riga etichetta + componente al pannello con GridBagLayout. */
    private void aggiungiRiga(JPanel p, GridBagConstraints gbc,
                              int riga, String label, JComponent campo) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = riga;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        p.add(campo, gbc);
    }

    // ── Logica di salvataggio ─────────────────────────────────────────────────

    /**
     * Valida i campi e, se tutto è corretto, costruisce l'oggetto SpesaDipendente
     * e chiude il dialog con esito positivo.
     */
    private void salva() {
        // ── Validazione campi obbligatori ─────────────────────────────────────
        String dipendente = campoDipendente.getText().trim();
        String reparto    = campoReparto.getText().trim();
        String data       = campoData.getText().trim();
        String descrizione= campoDescrizione.getText().trim();
        String importoStr = campoImporto.getText().trim();

        if (dipendente.isEmpty() || reparto.isEmpty() || data.isEmpty()
                || descrizione.isEmpty() || importoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Compila tutti i campi obbligatori (*) prima di salvare.",
                "Campi mancanti", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validazione formato data ──────────────────────────────────────────
        if (!data.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                "La data deve essere nel formato YYYY-MM-DD (es. 2024-03-15).",
                "Formato data errato", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Parsing dell'importo ──────────────────────────────────────────────
        double importo;
        try {
            importo = Double.parseDouble(importoStr.replace(",", "."));
            if (importo <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "L'importo deve essere un numero positivo (es. 25.50).",
                "Importo non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Costruzione oggetto ───────────────────────────────────────────────
        spesaRisultato = new SpesaDipendente(
            idSpesa,
            dipendente,
            reparto,
            data,
            (String) comboCategoria.getSelectedItem(),
            descrizione,
            importo,
            (String) comboStato.getSelectedItem()
        );

        confermato = true;
        dispose(); // Chiude il dialog
    }

    // ── Getter per il codice chiamante ────────────────────────────────────────

    /** @return true se l'utente ha cliccato Salva con dati validi */
    public boolean isConfermato() { return confermato; }

    /** @return La spesa creata/modificata, oppure null se annullato */
    public SpesaDipendente getSpesaRisultato() { return spesaRisultato; }
}

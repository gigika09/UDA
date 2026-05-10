package gui;

import model.SpesaDipendente;
import service.GestoreSpese;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Finestra modale per la ricerca delle spese su più campi.
 *
 * Campi di ricerca disponibili:
 *   1. Nome dipendente (ricerca parziale, case-insensitive)
 *   2. Descrizione     (ricerca parziale, case-insensitive)
 *   3. Reparto         (ricerca parziale)
 *   4. Importo minimo / massimo  (range numerico)
 *
 * I risultati vengono mostrati in una JTable interna al dialog stesso.
 * Il pannello rimborsi principale viene aggiornato tramite callback.
 */
public class DialogRicerca extends JDialog {

    private GestoreSpese    gestoreSpese;
    private SpesaTableModel modelloTabella;   // Modello della tabella INTERNA al dialog

    // ── Campi di ricerca ──────────────────────────────────────────────────────
    private JTextField campoDipendente;
    private JTextField campoDescrizione;
    private JTextField campoReparto;
    private JTextField campoImportoMin;
    private JTextField campoImportoMax;

    // ── Tabella dei risultati ─────────────────────────────────────────────────
    private JTable tabellaRisultati;
    private JLabel labelConteggio;

    // ── Costruttore ──────────────────────────────────────────────────────────

    public DialogRicerca(Frame owner, GestoreSpese gestoreSpese) {
        super(owner, "Ricerca Spese", true);
        this.gestoreSpese = gestoreSpese;

        costruisciUI();

        setSize(800, 500);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(owner);
    }

    // ── Costruzione UI ────────────────────────────────────────────────────────

    private void costruisciUI() {
        setLayout(new BorderLayout(8, 8));

        add(creaPannelloFiltri(), BorderLayout.NORTH);
        add(creaPannelloRisultati(), BorderLayout.CENTER);
        add(creaPannelloBottoni(), BorderLayout.SOUTH);
    }

    /** Pannello in alto con i campi di ricerca. */
    private JPanel creaPannelloFiltri() {
        JPanel pannello = new JPanel(new GridBagLayout());
        pannello.setBorder(new TitledBorder("Criteri di ricerca"));
        pannello.setBackground(new Color(248, 248, 248));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(4, 8, 4, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        // Riga 0: Dipendente | Descrizione
        gbc.gridx = 0; gbc.gridy = 0;
        pannello.add(new JLabel("Nome dipendente (anche parziale):"), gbc);
        gbc.gridx = 1;
        pannello.add(new JLabel("Descrizione (anche parziale):"), gbc);

        campoDipendente  = new JTextField(18);
        campoDescrizione = new JTextField(18);
        gbc.gridx = 0; gbc.gridy = 1;
        pannello.add(campoDipendente, gbc);
        gbc.gridx = 1;
        pannello.add(campoDescrizione, gbc);

        // Riga 2: Reparto | Importo min - max
        gbc.gridx = 0; gbc.gridy = 2;
        pannello.add(new JLabel("Reparto:"), gbc);
        gbc.gridx = 1;
        pannello.add(new JLabel("Importo da (€):          fino a (€):"), gbc);

        campoReparto    = new JTextField(18);
        campoImportoMin = new JTextField(8);
        campoImportoMax = new JTextField(8);

        gbc.gridx = 0; gbc.gridy = 3;
        pannello.add(campoReparto, gbc);

        // Per i due campi importo usiamo un sotto-pannello in linea
        JPanel pannelloImporti = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pannelloImporti.setOpaque(false);
        pannelloImporti.add(campoImportoMin);
        pannelloImporti.add(new JLabel("—"));
        pannelloImporti.add(campoImportoMax);
        gbc.gridx = 1; gbc.gridy = 3;
        pannello.add(pannelloImporti, gbc);

        // Pulsanti Cerca / Azzera a destra
        JButton btnCerca  = new JButton("🔍 Cerca");
        JButton btnAzzera = new JButton("✖ Azzera");
        btnCerca.addActionListener(e  -> eseguiRicerca());
        btnAzzera.addActionListener(e -> {
            campoDipendente.setText("");
            campoDescrizione.setText("");
            campoReparto.setText("");
            campoImportoMin.setText("");
            campoImportoMax.setText("");
            modelloTabella.aggiornaSpese(new ArrayList<>());
            labelConteggio.setText("Risultati: 0");
        });

        JPanel pannelloBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pannelloBtn.setOpaque(false);
        pannelloBtn.add(btnAzzera);
        pannelloBtn.add(btnCerca);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        pannello.add(pannelloBtn, gbc);

        return pannello;
    }

    /** Pannello centrale con la tabella dei risultati. */
    private JScrollPane creaPannelloRisultati() {
        modelloTabella = new SpesaTableModel(new ArrayList<>());
        tabellaRisultati = new JTable(modelloTabella);
        tabellaRisultati.setRowHeight(22);
        tabellaRisultati.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return new JScrollPane(tabellaRisultati);
    }

    /** Pannello in basso con contatore e pulsante chiudi. */
    private JPanel creaPannelloBottoni() {
        JPanel pannello = new JPanel(new BorderLayout());
        pannello.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        labelConteggio = new JLabel("Risultati: 0");
        labelConteggio.setFont(new Font("Arial", Font.ITALIC, 12));
        pannello.add(labelConteggio, BorderLayout.WEST);

        JButton btnChiudi = new JButton("Chiudi");
        btnChiudi.addActionListener(e -> dispose());
        pannello.add(btnChiudi, BorderLayout.EAST);

        return pannello;
    }

    // ── Logica di ricerca ─────────────────────────────────────────────────────

    /**
     * Filtra la lista completa delle spese in base ai criteri inseriti.
     * Tutti i criteri sono opzionali: un campo vuoto non filtra quel campo.
     * I criteri attivi si combinano in AND (devono essere tutti soddisfatti).
     */
    private void eseguiRicerca() {
        String testoDipendente  = campoDipendente.getText().trim().toLowerCase();
        String testoDescrizione = campoDescrizione.getText().trim().toLowerCase();
        String testoReparto     = campoReparto.getText().trim().toLowerCase();
        String testoMin         = campoImportoMin.getText().trim();
        String testoMax         = campoImportoMax.getText().trim();

        // Parsing dei limiti di importo (0 = nessun limite)
        double importoMin = 0;
        double importoMax = Double.MAX_VALUE;
        try {
            if (!testoMin.isEmpty()) importoMin = Double.parseDouble(testoMin.replace(",", "."));
            if (!testoMax.isEmpty()) importoMax = Double.parseDouble(testoMax.replace(",", "."));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Inserisci valori numerici validi per i limiti di importo.",
                "Valore non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Range importo coerente?
        if (importoMin > importoMax) {
            JOptionPane.showMessageDialog(this,
                "L'importo minimo non può essere maggiore del massimo.",
                "Range non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Filtro ────────────────────────────────────────────────────────────
        ArrayList<SpesaDipendente> risultati = new ArrayList<>();

        for (SpesaDipendente spesa : gestoreSpese.getTutteLeSpese()) {
            boolean ok = true;

            // Filtro dipendente (contains = ricerca parziale)
            if (!testoDipendente.isEmpty() &&
                !spesa.getNomeDipendente().toLowerCase().contains(testoDipendente)) {
                ok = false;
            }
            // Filtro descrizione
            if (!testoDescrizione.isEmpty() &&
                !spesa.getDescrizione().toLowerCase().contains(testoDescrizione)) {
                ok = false;
            }
            // Filtro reparto
            if (!testoReparto.isEmpty() &&
                !spesa.getReparto().toLowerCase().contains(testoReparto)) {
                ok = false;
            }
            // Filtro importo
            if (spesa.getImporto() < importoMin || spesa.getImporto() > importoMax) {
                ok = false;
            }

            if (ok) risultati.add(spesa);
        }

        // Aggiorna la tabella con i risultati filtrati
        modelloTabella.aggiornaSpese(risultati);
        labelConteggio.setText("Risultati: " + risultati.size());
    }
}

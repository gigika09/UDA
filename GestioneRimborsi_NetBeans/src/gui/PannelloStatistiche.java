package gui;

import service.GestoreSpese;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/**
 * Pannello che mostra i totali delle spese approvate:
 *   - per dipendente
 *   - per categoria
 *   - totale generale
 *
 * ─── Swing usato qui ──────────────────────────────────────────────────────────
 *
 * JSplitPane divide orizzontalmente il pannello in due aree ridimensionabili.
 * Ogni area contiene una JTextArea con i totali calcolati dal GestoreSpese.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class PannelloStatistiche extends JPanel {

    private GestoreSpese gestoreSpese;

    // Aree di testo dove stamperemo i totali
    private JTextArea areaDipendenti;
    private JTextArea areaCategorie;
    private JLabel    labelTotaleGenerale;

    // ── Costruttore ──────────────────────────────────────────────────────────

    public PannelloStatistiche(GestoreSpese gestoreSpese) {
        this.gestoreSpese = gestoreSpese;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── JSplitPane: divide la vista in due colonne ────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5); // 50% a sinistra, 50% a destra
        splitPane.setDividerLocation(350);

        splitPane.setLeftComponent(creaPannelloDipendenti());
        splitPane.setRightComponent(creaPannelloCategorie());

        add(splitPane, BorderLayout.CENTER);
        add(creaPannelleTotaleGenerale(), BorderLayout.SOUTH);

        // Popola subito le aree di testo
        aggiornaTotali();
    }

    // ── Creazione dei sotto-pannelli ─────────────────────────────────────────

    private JPanel creaPannelloDipendenti() {
        JPanel pannello = new JPanel(new BorderLayout());
        pannello.setBorder(new TitledBorder("Totale approvato per dipendente"));

        areaDipendenti = new JTextArea();
        areaDipendenti.setEditable(false);           // Sola lettura
        areaDipendenti.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Font fisso per allineamento
        areaDipendenti.setBackground(new Color(245, 245, 245));

        pannello.add(new JScrollPane(areaDipendenti), BorderLayout.CENTER);

        JButton btnAggiorna = new JButton("🔄 Aggiorna");
        btnAggiorna.addActionListener(e -> aggiornaTotali());
        pannello.add(btnAggiorna, BorderLayout.SOUTH);

        return pannello;
    }

    private JPanel creaPannelloCategorie() {
        JPanel pannello = new JPanel(new BorderLayout());
        pannello.setBorder(new TitledBorder("Totale approvato per categoria"));

        areaCategorie = new JTextArea();
        areaCategorie.setEditable(false);
        areaCategorie.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaCategorie.setBackground(new Color(245, 245, 245));

        pannello.add(new JScrollPane(areaCategorie), BorderLayout.CENTER);

        return pannello;
    }

    private JPanel creaPannelleTotaleGenerale() {
        JPanel pannello = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pannello.setBorder(new TitledBorder("Riepilogo"));

        labelTotaleGenerale = new JLabel();
        labelTotaleGenerale.setFont(new Font("Arial", Font.BOLD, 16));
        labelTotaleGenerale.setForeground(new Color(27, 94, 32));

        pannello.add(labelTotaleGenerale);
        return pannello;
    }

    // ── Aggiornamento dei totali ──────────────────────────────────────────────

    /**
     * Ricalcola i totali tramite il GestoreSpese e aggiorna le aree di testo.
     * Va chiamato ogni volta che uno stato viene modificato nel pannello rimborsi.
     */
    public void aggiornaTotali() {

        // ── Totali per dipendente ─────────────────────────────────────────────
        StringBuilder sbDipendenti = new StringBuilder();
        Map<String, Double> totaliDip = gestoreSpese.getTotalePerDipendente();

        for (Map.Entry<String, Double> entry : totaliDip.entrySet()) {
            // %-25s: stringa lunga 25 caratteri (padding a destra) per allineamento
            sbDipendenti.append(String.format("%-25s  €  %8.2f%n",
                    entry.getKey(), entry.getValue()));
        }

        if (totaliDip.isEmpty()) {
            sbDipendenti.append("Nessuna spesa approvata.");
        }

        areaDipendenti.setText(sbDipendenti.toString());

        // ── Totali per categoria ──────────────────────────────────────────────
        StringBuilder sbCategorie = new StringBuilder();
        Map<String, Double> totaliCat = gestoreSpese.getTotalePerCategoria();

        for (Map.Entry<String, Double> entry : totaliCat.entrySet()) {
            // Emoji per ogni categoria, solo decorativa
            String emoji = emojiCategoria(entry.getKey());
            sbCategorie.append(String.format("%s %-15s  €  %8.2f%n",
                    emoji, entry.getKey(), entry.getValue()));
        }

        if (totaliCat.isEmpty()) {
            sbCategorie.append("Nessuna spesa approvata.");
        }

        areaCategorie.setText(sbCategorie.toString());

        // ── Totale generale ───────────────────────────────────────────────────
        double totale = gestoreSpese.getTotaleGenerale();
        labelTotaleGenerale.setText(String.format("Totale rimborsi approvati:  € %.2f", totale));
    }

    /** Restituisce un'emoji decorativa per la categoria. */
    private String emojiCategoria(String categoria) {
        switch (categoria.toLowerCase()) {
            case "vitto":      return "🍽";
            case "trasporto":  return "🚂";
            case "alloggio":   return "🏨";
            default:           return "📦";
        }
    }
}

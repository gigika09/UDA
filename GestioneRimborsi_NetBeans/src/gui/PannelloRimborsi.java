package gui;

import model.SpesaDipendente;
import service.GestoreSpese;
import util.CsvWriter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Pannello principale dell'applicazione.
 * Mostra la tabella delle spese con filtri e pulsanti di azione.
 *
 * ─── Struttura Swing ──────────────────────────────────────────────────────────
 *
 *  JPanel (this) — BorderLayout
 *   ├── NORTH  → pannelloFiltri  (combo box e bottone filtro)
 *   ├── CENTER → JScrollPane     (contiene la JTable)
 *   └── SOUTH  → pannelloAzioni  (Inserisci / Modifica / Elimina / Approva / Rifiuta / Salva)
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class PannelloRimborsi extends JPanel {

    // ── Componenti principali ────────────────────────────────────────────────
    private JTable          tabella;
    private SpesaTableModel modelloTabella;

    // Filtri
    private JComboBox<String> comboDipendente;
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStato;

    // ── Riferimenti al service e al file ─────────────────────────────────────
    private GestoreSpese gestoreSpese;
    private String       percorsoFile;

    // Riferimento alla finestra principale (serve per aprire i dialog modali)
    private Frame finestraPrincipale;

    // ── Costruttore ──────────────────────────────────────────────────────────

    public PannelloRimborsi(GestoreSpese gestoreSpese, String percorsoFile, Frame finestraPrincipale) {
        this.gestoreSpese       = gestoreSpese;
        this.percorsoFile       = percorsoFile;
        this.finestraPrincipale = finestraPrincipale;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(creaPannelloFiltri(),  BorderLayout.NORTH);
        add(creaPannelloTabella(), BorderLayout.CENTER);
        add(creaPannelloAzioni(),  BorderLayout.SOUTH);
    }

    // ── Metodi pubblici richiamati dalla MenuBar ──────────────────────────────

    /**
     * Apre il dialog per INSERIRE una nuova spesa.
     * Calcola automaticamente il prossimo ID disponibile.
     */
    public void apriDialogInserisci() {
        int prossimoId = calcolaProssimoId();
        DialogSpesa dialog = new DialogSpesa(finestraPrincipale, null, prossimoId);
        dialog.setVisible(true); // Blocca qui (modale) finché non si chiude

        if (dialog.isConfermato()) {
            gestoreSpese.getTutteLeSpese().add(dialog.getSpesaRisultato());
            aggiornaComboDipendente();
            applicaFiltro();
            JOptionPane.showMessageDialog(this,
                "Spesa inserita con successo (ID " + prossimoId + ").",
                "Inserimento", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Apre il dialog per MODIFICARE la spesa selezionata.
     * Anche richiamabile con doppio click sulla riga.
     */
    public void apriDialogModifica() {
        int rigaSelezionata = tabella.getSelectedRow();
        if (rigaSelezionata < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleziona prima una riga dalla tabella.",
                "Nessuna selezione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SpesaDipendente spesaOriginale = modelloTabella.getSpesaAllaRiga(rigaSelezionata);
        if (spesaOriginale == null) return;

        DialogSpesa dialog = new DialogSpesa(finestraPrincipale, spesaOriginale, -1);
        dialog.setVisible(true);

        if (dialog.isConfermato()) {
            SpesaDipendente spesaModificata = dialog.getSpesaRisultato();
            ArrayList<SpesaDipendente> tutte = gestoreSpese.getTutteLeSpese();
            for (int i = 0; i < tutte.size(); i++) {
                if (tutte.get(i).getIdSpesa() == spesaModificata.getIdSpesa()) {
                    tutte.set(i, spesaModificata);
                    break;
                }
            }
            applicaFiltro();
        }
    }

    /**
     * Elimina le righe selezionate previa conferma.
     * Supporta selezione multipla (Ctrl+Click / Shift+Click).
     */
    public void eliminaSelezione() {
        int[] righeSel = tabella.getSelectedRows();
        if (righeSel.length == 0) {
            JOptionPane.showMessageDialog(this,
                "Seleziona almeno una riga da eliminare.",
                "Nessuna selezione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int risposta = JOptionPane.showConfirmDialog(this,
            "Stai per eliminare " + righeSel.length + " spesa/e.\nConfermi?",
            "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (risposta != JOptionPane.YES_OPTION) return;

        // Raccoglie gli ID prima di modificare la lista
        ArrayList<Integer> idDaEliminare = new ArrayList<>();
        for (int riga : righeSel) {
            SpesaDipendente s = modelloTabella.getSpesaAllaRiga(riga);
            if (s != null) idDaEliminare.add(s.getIdSpesa());
        }

        gestoreSpese.getTutteLeSpese().removeIf(s -> idDaEliminare.contains(s.getIdSpesa()));
        aggiornaComboDipendente();
        applicaFiltro();
    }

    /** Aggiorna il percorso del file (usato da Salva con Nome). */
    public void setPercorsoFile(String percorso) {
        this.percorsoFile = percorso;
    }

    /** Salva il CSV nel percorso corrente. */
    public void salvaCsv() {
        try {
            CsvWriter.scriviCsv(percorsoFile, gestoreSpese.getTutteLeSpese());
            JOptionPane.showMessageDialog(this,
                "File salvato:\n" + percorsoFile,
                "Salvataggio riuscito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Errore durante il salvataggio:\n" + ex.getMessage(),
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Creazione dei sotto-pannelli ──────────────────────────────────────────

    private JPanel creaPannelloFiltri() {
        JPanel pannello = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pannello.setBorder(new TitledBorder("Filtra le spese"));

        pannello.add(new JLabel("Dipendente:"));
        comboDipendente = new JComboBox<>();
        aggiornaComboDipendente();
        pannello.add(comboDipendente);

        pannello.add(new JLabel("Categoria:"));
        comboCategoria = new JComboBox<>(new String[]{
            "Tutte",
            SpesaDipendente.CAT_VITTO, SpesaDipendente.CAT_TRASPORTO,
            SpesaDipendente.CAT_ALLOGGIO, SpesaDipendente.CAT_ALTRO
        });
        pannello.add(comboCategoria);

        pannello.add(new JLabel("Stato:"));
        comboStato = new JComboBox<>(new String[]{
            "Tutti",
            SpesaDipendente.STATO_IN_ATTESA,
            SpesaDipendente.STATO_APPROVATA,
            SpesaDipendente.STATO_RIFIUTATA
        });
        pannello.add(comboStato);

        JButton btnFiltra = new JButton("🔍 Applica filtro");
        btnFiltra.addActionListener(e -> applicaFiltro());
        pannello.add(btnFiltra);

        JButton btnAzzera = new JButton("✖ Azzera");
        btnAzzera.addActionListener(e -> {
            comboDipendente.setSelectedIndex(0);
            comboCategoria.setSelectedIndex(0);
            comboStato.setSelectedIndex(0);
            modelloTabella.aggiornaSpese(gestoreSpese.getTutteLeSpese());
        });
        pannello.add(btnAzzera);

        return pannello;
    }

    private JScrollPane creaPannelloTabella() {
        modelloTabella = new SpesaTableModel(gestoreSpese.getTutteLeSpese());
        tabella = new JTable(modelloTabella);

        // Selezione multipla per eliminazione di più righe
        tabella.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabella.setRowHeight(24);
        tabella.getTableHeader().setReorderingAllowed(false);

        tabella.getColumnModel().getColumn(7).setCellRenderer(new RendererStato());
        tabella.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabella.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabella.getColumnModel().getColumn(6).setPreferredWidth(80);

        // Doppio click → apri dialog modifica
        tabella.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) apriDialogModifica();
            }
        });

        return new JScrollPane(tabella);
    }

    private JPanel creaPannelloAzioni() {
        JPanel pannello = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        pannello.setBorder(new TitledBorder("Azioni rapide"));

        JButton btnInserisci = new JButton("➕ Inserisci");
        btnInserisci.setBackground(new Color(25, 118, 210));
        btnInserisci.setForeground(Color.WHITE);
        btnInserisci.addActionListener(e -> apriDialogInserisci());
        pannello.add(btnInserisci);

        JButton btnModifica = new JButton("✏ Modifica");
        btnModifica.addActionListener(e -> apriDialogModifica());
        pannello.add(btnModifica);

        JButton btnElimina = new JButton("🗑 Elimina");
        btnElimina.setBackground(new Color(183, 28, 28));
        btnElimina.setForeground(Color.WHITE);
        btnElimina.addActionListener(e -> eliminaSelezione());
        pannello.add(btnElimina);

        pannello.add(new JSeparator(SwingConstants.VERTICAL));

        JButton btnApprova = new JButton("✔ Approva");
        btnApprova.setBackground(new Color(76, 175, 80));
        btnApprova.setForeground(Color.WHITE);
        btnApprova.addActionListener(e -> cambiaStatoSpesaSelezionata(SpesaDipendente.STATO_APPROVATA));
        pannello.add(btnApprova);

        JButton btnRifiuta = new JButton("✖ Rifiuta");
        btnRifiuta.setBackground(new Color(244, 67, 54));
        btnRifiuta.setForeground(Color.WHITE);
        btnRifiuta.addActionListener(e -> cambiaStatoSpesaSelezionata(SpesaDipendente.STATO_RIFIUTATA));
        pannello.add(btnRifiuta);

        pannello.add(new JSeparator(SwingConstants.VERTICAL));

        JButton btnSalva = new JButton("💾 Salva CSV");
        btnSalva.addActionListener(e -> salvaCsv());
        pannello.add(btnSalva);

        return pannello;
    }

    // ── Logica interna ────────────────────────────────────────────────────────

    private void applicaFiltro() {
        ArrayList<SpesaDipendente> risultato = new ArrayList<>(gestoreSpese.getTutteLeSpese());

        String dipendente = (String) comboDipendente.getSelectedItem();
        if (dipendente != null && !dipendente.equals("Tutti"))
            risultato.removeIf(s -> !s.getNomeDipendente().equalsIgnoreCase(dipendente));

        String categoria = (String) comboCategoria.getSelectedItem();
        if (categoria != null && !categoria.equals("Tutte"))
            risultato.removeIf(s -> !s.getCategoria().equalsIgnoreCase(categoria));

        String stato = (String) comboStato.getSelectedItem();
        if (stato != null && !stato.equals("Tutti"))
            risultato.removeIf(s -> !s.getStato().equalsIgnoreCase(stato));

        modelloTabella.aggiornaSpese(risultato);
    }

    private void cambiaStatoSpesaSelezionata(String nuovoStato) {
        int rigaSelezionata = tabella.getSelectedRow();
        if (rigaSelezionata < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleziona prima una riga dalla tabella.",
                "Nessuna selezione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SpesaDipendente spesa = modelloTabella.getSpesaAllaRiga(rigaSelezionata);
        if (spesa == null) return;

        String azione = nuovoStato.equals(SpesaDipendente.STATO_APPROVATA) ? "approvare" : "rifiutare";
        int scelta = JOptionPane.showConfirmDialog(this,
            "Vuoi " + azione + " la spesa #" + spesa.getIdSpesa()
            + " di " + spesa.getNomeDipendente() + "?",
            "Conferma", JOptionPane.YES_NO_OPTION);

        if (scelta == JOptionPane.YES_OPTION) {
            if (nuovoStato.equals(SpesaDipendente.STATO_APPROVATA))
                gestoreSpese.approvaSpesa(spesa.getIdSpesa());
            else
                gestoreSpese.rifiutaSpesa(spesa.getIdSpesa());
            applicaFiltro();
        }
    }

    private void aggiornaComboDipendente() {
        String selezionato = (comboDipendente != null)
            ? (String) comboDipendente.getSelectedItem() : null;

        ArrayList<String> nomi = gestoreSpese.getNomiDipendenti();
        String[] opzioni = new String[nomi.size() + 1];
        opzioni[0] = "Tutti";
        for (int i = 0; i < nomi.size(); i++) opzioni[i + 1] = nomi.get(i);

        if (comboDipendente != null) {
            comboDipendente.setModel(new DefaultComboBoxModel<>(opzioni));
            if (selezionato != null) comboDipendente.setSelectedItem(selezionato);
        }
    }

    private int calcolaProssimoId() {
        int max = 0;
        for (SpesaDipendente s : gestoreSpese.getTutteLeSpese())
            if (s.getIdSpesa() > max) max = s.getIdSpesa();
        return max + 1;
    }

    // ── Renderer colorato per la colonna Stato ─────────────────────────────────

    private static class RendererStato extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String stato = value != null ? value.toString() : "";
                switch (stato) {
                    case SpesaDipendente.STATO_APPROVATA:
                        setBackground(new Color(200, 230, 201));
                        setForeground(new Color(27, 94, 32));   break;
                    case SpesaDipendente.STATO_RIFIUTATA:
                        setBackground(new Color(255, 205, 210));
                        setForeground(new Color(183, 28, 28));  break;
                    case SpesaDipendente.STATO_IN_ATTESA:
                        setBackground(new Color(255, 249, 196));
                        setForeground(new Color(245, 127, 23)); break;
                    default:
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                }
            }
            return this;
        }
    }
}

package gui;

import model.SpesaDipendente;
import service.GestoreSpese;
import util.CsvReader;
import util.CsvWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Finestra principale dell'applicazione (JFrame).
 *
 * ─── Struttura della finestra ─────────────────────────────────────────────────
 *
 *  JFrame
 *   ├── JMenuBar         → menu File / Modifica / Info
 *   ├── JTabbedPane      → schede "Rimborsi" e "Statistiche"
 *   │    ├── PannelloRimborsi
 *   │    └── PannelloStatistiche
 *   └── JLabel (status bar) → mostra il file aperto corrente
 *
 * ─── JMenuBar in Swing ────────────────────────────────────────────────────────
 *
 * JMenuBar  = la barra orizzontale in cima alla finestra
 * JMenu     = un menu a tendina (File, Modifica, Info...)
 * JMenuItem = una singola voce cliccabile dentro un JMenu
 *
 * Struttura tipica:
 *   menuBar.add(menuFile)
 *   menuFile.add(voceApri)
 *   voceApri.addActionListener(e -> azione());
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class FinestraPrincipale extends JFrame {

    // ── Dati applicazione ─────────────────────────────────────────────────────
    private GestoreSpese        gestoreSpese;
    private String              percorsoFileCorrente; // file CSV aperto

    // ── Componenti GUI ────────────────────────────────────────────────────────
    private PannelloRimborsi    pannelloRimborsi;
    private PannelloStatistiche pannelloStatistiche;
    private JLabel              labelStatusBar;       // barra di stato in fondo

    // ── Costruttore ──────────────────────────────────────────────────────────

    /**
     * @param gestoreSpese       Service già inizializzato (con dati CSV caricati)
     * @param percorsoFileCsv    Percorso del file CSV aperto all'avvio
     */
    public FinestraPrincipale(GestoreSpese gestoreSpese, String percorsoFileCsv) {
        this.gestoreSpese         = gestoreSpese;
        this.percorsoFileCorrente = percorsoFileCsv;

        setTitle("Gestione Rimborsi Spese — " + percorsoFileCsv);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null); // Centra sullo schermo

        // Costruzione dell'interfaccia
        setJMenuBar(creaMenuBar());
        add(creaAreaCentrale(), BorderLayout.CENTER);
        add(creaStatusBar(),    BorderLayout.SOUTH);
    }

    // ── Creazione della MenuBar ───────────────────────────────────────────────

    /**
     * Costruisce la barra dei menu con le voci richieste.
     *
     * ─── Come funziona JMenuBar ───────────────────────────────────────────────
     *
     * 1. Creiamo un JMenuBar
     * 2. Aggiungiamo JMenu (File, Modifica, Info)
     * 3. Dentro ogni JMenu aggiungiamo JMenuItem o JSeparator
     * 4. Ogni JMenuItem ha un ActionListener per eseguire l'azione
     *
     * ─────────────────────────────────────────────────────────────────────────
     */
    private JMenuBar creaMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(creaMenuFile());
        menuBar.add(creaMenuModifica());
        menuBar.add(creaMenuInfo());

        return menuBar;
    }

    // ── Menu FILE ─────────────────────────────────────────────────────────────

    private JMenu creaMenuFile() {
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F'); // Scorciatoia Alt+F

        // ── Apri ──────────────────────────────────────────────────────────────
        JMenuItem voceApri = new JMenuItem("Apri...");
        voceApri.setAccelerator(KeyStroke.getKeyStroke("ctrl O")); // Ctrl+O
        voceApri.addActionListener(e -> apriFile());
        menuFile.add(voceApri);

        // ── Salva ─────────────────────────────────────────────────────────────
        JMenuItem voceSalva = new JMenuItem("Salva");
        voceSalva.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        voceSalva.addActionListener(e -> pannelloRimborsi.salvaCsv());
        menuFile.add(voceSalva);

        // ── Salva con Nome ────────────────────────────────────────────────────
        JMenuItem voceSalvaConNome = new JMenuItem("Salva con nome...");
        voceSalvaConNome.setAccelerator(KeyStroke.getKeyStroke("ctrl shift S"));
        voceSalvaConNome.addActionListener(e -> salvaConNome());
        menuFile.add(voceSalvaConNome);

        menuFile.addSeparator(); // Linea divisoria

        // ── Esci ──────────────────────────────────────────────────────────────
        JMenuItem voceEsci = new JMenuItem("Esci");
        voceEsci.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        voceEsci.addActionListener(e -> chiudiApplicazione());
        menuFile.add(voceEsci);

        return menuFile;
    }

    // ── Menu MODIFICA ──────────────────────────────────────────────────────────

    private JMenu creaMenuModifica() {
        JMenu menuModifica = new JMenu("Modifica");
        menuModifica.setMnemonic('M');

        // ── Inserisci ─────────────────────────────────────────────────────────
        JMenuItem voceInserisci = new JMenuItem("Inserisci nuova spesa");
        voceInserisci.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        voceInserisci.addActionListener(e -> pannelloRimborsi.apriDialogInserisci());
        menuModifica.add(voceInserisci);

        // ── Modifica ─────────────────────────────────────────────────────────
        JMenuItem voceModifica = new JMenuItem("Modifica spesa selezionata");
        voceModifica.setAccelerator(KeyStroke.getKeyStroke("F2"));
        voceModifica.addActionListener(e -> pannelloRimborsi.apriDialogModifica());
        menuModifica.add(voceModifica);

        // ── Elimina ───────────────────────────────────────────────────────────
        JMenuItem voceElimina = new JMenuItem("Elimina selezione");
        voceElimina.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        voceElimina.addActionListener(e -> pannelloRimborsi.eliminaSelezione());
        menuModifica.add(voceElimina);

        menuModifica.addSeparator();

        // ── Visualizza Lista (torna alla scheda rimborsi) ─────────────────────
        JMenuItem voceVisualizza = new JMenuItem("Visualizza Lista");
        voceVisualizza.addActionListener(e -> {
            // Seleziona la prima scheda (indice 0 = pannello rimborsi)
            ((JTabbedPane) getContentPane().getComponent(0)).setSelectedIndex(0);
        });
        menuModifica.add(voceVisualizza);

        menuModifica.addSeparator();

        // ── Ricerca ───────────────────────────────────────────────────────────
        JMenuItem voceRicerca = new JMenuItem("Ricerca avanzata...");
        voceRicerca.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));
        voceRicerca.addActionListener(e -> apriDialogRicerca());
        menuModifica.add(voceRicerca);

        return menuModifica;
    }

    // ── Menu INFO ─────────────────────────────────────────────────────────────

    private JMenu creaMenuInfo() {
        JMenu menuInfo = new JMenu("Info");
        menuInfo.setMnemonic('I');

        JMenuItem voceAbout = new JMenuItem("About");
        voceAbout.addActionListener(e -> new DialogAbout(this).setVisible(true));
        menuInfo.add(voceAbout);

        JMenuItem voceCredits = new JMenuItem("Credits");
        voceCredits.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "<html><b>Credits</b><br><br>"
              + "Progetto: Gestione Rimborsi Spese<br>"
              + "Linguaggio: Java 11+ con Swing<br>"
              + "Anno: 2024<br><br>"
              + "Struttura: MVC semplificato<br>"
              + "  • model   → SpesaDipendente<br>"
              + "  • service → GestoreSpese<br>"
              + "  • util    → CsvReader, CsvWriter<br>"
              + "  • gui     → Pannelli e Dialog Swing</html>",
                "Credits", JOptionPane.INFORMATION_MESSAGE));
        menuInfo.add(voceCredits);

        menuInfo.addSeparator();

        JMenuItem voceGuida = new JMenuItem("Guida rapida");
        voceGuida.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "<html><b>Guida rapida</b><br><br>"
              + "• <b>Apri</b>: carica un file spese.csv<br>"
              + "• <b>Filtri</b>: filtra per dipendente, categoria, stato<br>"
              + "• <b>Doppio click</b> su una riga: apri dettaglio/modifica<br>"
              + "• <b>Ctrl+N</b>: inserisci nuova spesa<br>"
              + "• <b>Ctrl+F</b>: ricerca avanzata<br>"
              + "• <b>Ctrl+S</b>: salva il CSV<br>"
              + "• <b>Canc</b>: elimina le righe selezionate<br>"
              + "• <b>Scheda Statistiche</b>: totali per dipendente e categoria</html>",
                "Guida", JOptionPane.INFORMATION_MESSAGE));
        menuInfo.add(voceGuida);

        return menuInfo;
    }

    // ── Creazione dei pannelli centrali ───────────────────────────────────────

    /**
     * Crea il JTabbedPane con le due schede principali.
     *
     * JTabbedPane permette di avere più "pagine" nella stessa finestra,
     * selezionabili tramite le linguette (tab) in cima.
     */
    private JTabbedPane creaAreaCentrale() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // I pannelli ricevono il riferimento a "this" (Frame) per aprire i dialog
        pannelloRimborsi = new PannelloRimborsi(gestoreSpese, percorsoFileCorrente, this);
        pannelloStatistiche = new PannelloStatistiche(gestoreSpese);

        tabbedPane.addTab("📋  Rimborsi",    pannelloRimborsi);
        tabbedPane.addTab("📊  Statistiche", pannelloStatistiche);

        // Quando si passa alla scheda Statistiche, i totali vengono ricalcolati
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == pannelloStatistiche) {
                pannelloStatistiche.aggiornaTotali();
            }
        });

        return tabbedPane;
    }

    /** Crea la barra di stato in fondo alla finestra. */
    private JPanel creaStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setBackground(new Color(240, 240, 240));

        labelStatusBar = new JLabel("File: " + percorsoFileCorrente
            + "   |   Spese caricate: " + gestoreSpese.getTutteLeSpese().size());
        labelStatusBar.setFont(new Font("Arial", Font.PLAIN, 11));
        statusBar.add(labelStatusBar);

        return statusBar;
    }

    // ── Azioni del menu ───────────────────────────────────────────────────────

    /**
     * Apre un file CSV usando JFileChooser.
     *
     * ─── Come funziona JFileChooser ───────────────────────────────────────────
     *
     * JFileChooser è il selettore di file nativo di Swing.
     *
     * 1. Creiamo un JFileChooser
     * 2. (Opzionale) Aggiungiamo un filtro per mostrare solo i .csv
     * 3. Mostriamo la finestra con showOpenDialog(parent)
     * 4. Se l'utente ha scelto un file (APPROVE_OPTION), leggiamo il percorso
     *
     * ─────────────────────────────────────────────────────────────────────────
     */
    private void apriFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Apri file CSV");

        // Filtro: mostra solo file .csv (e le cartelle per navigare)
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "File CSV (*.csv)", "csv"));

        // Mostra la finestra di selezione; il metodo blocca finché l'utente non sceglie
        int risultato = fileChooser.showOpenDialog(this);

        // Controlla se l'utente ha effettivamente selezionato un file
        if (risultato == JFileChooser.APPROVE_OPTION) {
            String percorso = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                ArrayList<SpesaDipendente> nuoveSpese = CsvReader.leggiCsv(percorso);
                gestoreSpese = new GestoreSpese(nuoveSpese);
                percorsoFileCorrente = percorso;

                // Ricrea i pannelli con i nuovi dati
                ricreaPannelli();

                aggiornaStatusBar();
                setTitle("Gestione Rimborsi Spese — " + percorso);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Impossibile leggere il file:\n" + ex.getMessage(),
                    "Errore apertura", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Salva con nome: apre JFileChooser in modalità SALVATAGGIO.
     */
    private void salvaConNome() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salva con nome");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "File CSV (*.csv)", "csv"));

        // showSaveDialog invece di showOpenDialog
        int risultato = fileChooser.showSaveDialog(this);

        if (risultato == JFileChooser.APPROVE_OPTION) {
            String percorso = fileChooser.getSelectedFile().getAbsolutePath();

            // Aggiunge estensione .csv se l'utente non l'ha inserita
            if (!percorso.toLowerCase().endsWith(".csv")) percorso += ".csv";

            try {
                CsvWriter.scriviCsv(percorso, gestoreSpese.getTutteLeSpese());
                percorsoFileCorrente = percorso;
                pannelloRimborsi.setPercorsoFile(percorso);
                setTitle("Gestione Rimborsi Spese — " + percorso);
                aggiornaStatusBar();
                JOptionPane.showMessageDialog(this,
                    "File salvato:\n" + percorso,
                    "Salvataggio riuscito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Errore durante il salvataggio:\n" + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Apre il dialog di ricerca avanzata. */
    private void apriDialogRicerca() {
        new DialogRicerca(this, gestoreSpese).setVisible(true);
    }

    /** Chiede conferma prima di uscire dall'applicazione. */
    private void chiudiApplicazione() {
        int risposta = JOptionPane.showConfirmDialog(this,
            "Vuoi uscire dall'applicazione?\nLe modifiche non salvate andranno perse.",
            "Esci", JOptionPane.YES_NO_OPTION);
        if (risposta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // ── Utilità ───────────────────────────────────────────────────────────────

    /** Aggiorna il testo della barra di stato. */
    private void aggiornaStatusBar() {
        labelStatusBar.setText("File: " + percorsoFileCorrente
            + "   |   Spese caricate: " + gestoreSpese.getTutteLeSpese().size());
    }

    /**
     * Ricrea i pannelli dopo aver aperto un nuovo file.
     * Svuota il contenitore centrale e lo ri-popola.
     */
    private void ricreaPannelli() {
        // Recupera il JTabbedPane (primo componente nell'area CENTER)
        Container contentPane = getContentPane();
        // Rimuove il vecchio TabbedPane
        for (Component c : contentPane.getComponents()) {
            if (c instanceof JTabbedPane) {
                contentPane.remove(c);
                break;
            }
        }
        contentPane.add(creaAreaCentrale(), BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
    }
}

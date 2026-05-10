import gui.DialogLogin;
import gui.FinestraPrincipale;
import model.SpesaDipendente;
import service.GestoreSpese;
import util.CsvReader;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ════════════════════════════════════════════════════════════════════════════
 *  CLASSE PRINCIPALE — RimborsiUDA
 *  Punto di ingresso dell'applicazione "Gestione Rimborsi Spese".
 * ════════════════════════════════════════════════════════════════════════════
 *
 * ────────────────────────────────────────────────────────────────────────────
 */
public class RimborsiUDA {

    // Percorso del file CSV caricato all'avvio (può essere cambiato via menu File → Apri)
    private static final String FILE_CSV_DEFAULT = "spese.csv";

    /**
     * Metodo main: punto di ingresso dell'applicazione.
     *
     * ─── Perché SwingUtilities.invokeLater? ──────────────────────────────────
     *
     * Swing non è thread-safe: tutti i componenti grafici devono essere creati
     * e aggiornati sul thread dedicato alla GUI chiamato "Event Dispatch Thread"
     * (EDT). invokeLater schedula il codice sull'EDT garantendo correttezza.
     *
     * ─────────────────────────────────────────────────────────────────────────
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> avviaApplicazione());
    }

    /**
     * Sequenza di avvio:
     *   1. Imposta il Look & Feel del sistema operativo
     *   2. Mostra il dialog di login
     *   3. Se login riuscito, carica il CSV
     *   4. Crea il GestoreSpese con i dati letti
     *   5. Apre la FinestraPrincipale
     */
    private static void avviaApplicazione() {

        // ── 1. Look & Feel nativo del sistema operativo ───────────────────────
        // Rende i componenti Swing simili alle finestre native di Windows/macOS/Linux
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Se fallisce, Swing usa il suo aspetto di default (Metal): non è un errore grave
            System.err.println("Look&Feel non disponibile, uso il default Swing.");
        }

        // ── 2. Dialog di login ────────────────────────────────────────────────
        // Passiamo null come owner perché la finestra principale non esiste ancora
        DialogLogin dialogLogin = new DialogLogin(null);
        dialogLogin.setVisible(true); // Blocca qui (modale) finché non si chiude

        // Se l'utente ha annullato o inserito credenziali errate: usciamo
        if (!dialogLogin.isAccessoConsentito()) {
            System.out.println("Accesso negato o annullato. Chiusura.");
            System.exit(0);
        }

        // ── 3. Caricamento del file CSV ───────────────────────────────────────
        ArrayList<SpesaDipendente> listaSpese = new ArrayList<>();
        String percorsoFile = FILE_CSV_DEFAULT;

        try {
            listaSpese = CsvReader.leggiCsv(percorsoFile);
        } catch (IOException e) {
            // Il file non esiste o non è leggibile: avvisiamo e partiamo con lista vuota
            JOptionPane.showMessageDialog(null,
                "File '" + percorsoFile + "' non trovato.\n"
                + "L'applicazione partirà con una lista vuota.\n"
                + "Puoi aprire un file dal menu File → Apri.",
                "File non trovato", JOptionPane.WARNING_MESSAGE);
        }

        // ── 4. Creazione del service ──────────────────────────────────────────
        // GestoreSpese è il "cervello" dell'applicazione: contiene i dati e la logica
        GestoreSpese gestoreSpese = new GestoreSpese(listaSpese);

        // ── 5. Apertura della finestra principale ─────────────────────────────
        FinestraPrincipale finestra = new FinestraPrincipale(gestoreSpese, percorsoFile);
        finestra.setVisible(true);
    }
}

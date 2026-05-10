package gui;

import model.SpesaDipendente;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Modello dati per la JTable di Swing.
 *
 * ─── Cos'è un TableModel? ────────────────────────────────────────────────────
 *
 * In Swing, JTable non contiene direttamente i dati: li legge da un "modello".
 * Estendendo AbstractTableModel dobbiamo implementare solo 3 metodi obbligatori:
 *   - getRowCount()    → quante righe ha la tabella?
 *   - getColumnCount() → quante colonne?
 *   - getValueAt()     → che valore c'è nella cella (riga, colonna)?
 *
 * In più sovrascriviamo getColumnName() per dare nomi leggibili alle colonne.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class SpesaTableModel extends AbstractTableModel {

    // Nomi delle colonne visualizzate nell'intestazione della tabella
    private static final String[] INTESTAZIONI = {
        "ID", "Dipendente", "Reparto", "Data", "Categoria", "Descrizione", "Importo (€)", "Stato"
    };

    // Lista delle spese da mostrare (può essere filtrata)
    private ArrayList<SpesaDipendente> spese;

    // ── Costruttore ──────────────────────────────────────────────────────────

    public SpesaTableModel(ArrayList<SpesaDipendente> spese) {
        this.spese = spese;
    }

    // ── Metodi obbligatori di AbstractTableModel ─────────────────────────────

    /** Numero di righe = numero di spese nella lista */
    @Override
    public int getRowCount() {
        return spese.size();
    }

    /** Numero di colonne fisso = 8 */
    @Override
    public int getColumnCount() {
        return INTESTAZIONI.length;
    }

    /**
     * Restituisce il valore da mostrare nella cella (riga, colonna).
     * Ogni colonna corrisponde a un campo dell'oggetto SpesaDipendente.
     */
    @Override
    public Object getValueAt(int riga, int colonna) {
        SpesaDipendente spesa = spese.get(riga);

        switch (colonna) {
            case 0: return spesa.getIdSpesa();
            case 1: return spesa.getNomeDipendente();
            case 2: return spesa.getReparto();
            case 3: return spesa.getData();
            case 4: return spesa.getCategoria();
            case 5: return spesa.getDescrizione();
            case 6: return String.format("%.2f", spesa.getImporto()); // Es. "25.50"
            case 7: return spesa.getStato();
            default: return "";
        }
    }

    // ── Metodi opzionali ma utili ─────────────────────────────────────────────

    /** Restituisce il nome della colonna per l'intestazione */
    @Override
    public String getColumnName(int colonna) {
        return INTESTAZIONI[colonna];
    }

    /**
     * Aggiorna la lista mostrata nella tabella e notifica la GUI.
     * Chiamato ogni volta che si applica un filtro.
     *
     * fireTableDataChanged() dice alla JTable: "i dati sono cambiati,
     * ridisegna tutto".
     */
    public void aggiornaSpese(ArrayList<SpesaDipendente> nuoveSpese) {
        this.spese = nuoveSpese;
        fireTableDataChanged(); // Notifica la JTable
    }

    /**
     * Restituisce la spesa alla riga indicata.
     * Usato quando l'utente seleziona una riga e vuole approvare/rifiutare.
     */
    public SpesaDipendente getSpesaAllaRiga(int riga) {
        if (riga >= 0 && riga < spese.size()) {
            return spese.get(riga);
        }
        return null;
    }
}

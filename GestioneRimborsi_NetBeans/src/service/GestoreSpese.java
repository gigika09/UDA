package service;

import model.SpesaDipendente;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe di servizio (business logic) per la gestione delle spese.
 *
 * Contiene tutti i metodi che lavorano sui dati:
 *   - filtraggi per dipendente, categoria, stato
 *   - calcolo dei totali
 *   - approvazione e rifiuto dei rimborsi
 *
 * Non sa nulla di GUI né di CSV: è "al centro" dell'applicazione.
 */
public class GestoreSpese {

    // Lista principale: contiene TUTTE le spese caricate dal CSV
    private ArrayList<SpesaDipendente> tutteLeSpese;

    // ── Costruttore ──────────────────────────────────────────────────────────

    /**
     * Inizializza il gestore con la lista di spese già letta dal CSV.
     *
     * @param spese Lista proveniente da CsvReader.leggiCsv(...)
     */
    public GestoreSpese(ArrayList<SpesaDipendente> spese) {
        this.tutteLeSpese = spese;
    }

    // ── Accesso alla lista completa ──────────────────────────────────────────

    /** Restituisce tutte le spese (usata dalla GUI per popolare la tabella). */
    public ArrayList<SpesaDipendente> getTutteLeSpese() {
        return tutteLeSpese;
    }

    // ── Ricerca e filtro ─────────────────────────────────────────────────────

    /**
     * Restituisce le spese di un singolo dipendente.
     *
     * @param nomeDipendente  Nome esatto come appare nel CSV
     * @return Lista (eventualmente vuota) delle sue spese
     */
    public ArrayList<SpesaDipendente> getSpesePerDipendente(String nomeDipendente) {
        ArrayList<SpesaDipendente> risultato = new ArrayList<>();
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getNomeDipendente().equalsIgnoreCase(nomeDipendente)) {
                risultato.add(spesa);
            }
        }
        return risultato;
    }

    /**
     * Restituisce le spese filtrate per categoria (es. "vitto").
     */
    public ArrayList<SpesaDipendente> getSpesePerCategoria(String categoria) {
        ArrayList<SpesaDipendente> risultato = new ArrayList<>();
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getCategoria().equalsIgnoreCase(categoria)) {
                risultato.add(spesa);
            }
        }
        return risultato;
    }

    /**
     * Restituisce le spese filtrate per stato (es. "in attesa").
     */
    public ArrayList<SpesaDipendente> getSpesePerStato(String stato) {
        ArrayList<SpesaDipendente> risultato = new ArrayList<>();
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getStato().equalsIgnoreCase(stato)) {
                risultato.add(spesa);
            }
        }
        return risultato;
    }

    // ── Calcolo totali ───────────────────────────────────────────────────────

    /**
     * Calcola il totale importi per ogni dipendente (solo spese approvate).
     *
     * Usa una LinkedHashMap per mantenere l'ordine di inserimento,
     * con il nome del dipendente come chiave e il totale come valore.
     *
     * @return Mappa  nomeDipendente → totale approvato
     */
    public Map<String, Double> getTotalePerDipendente() {
        // LinkedHashMap mantiene l'ordine di inserimento (più leggibile nella GUI)
        Map<String, Double> totali = new LinkedHashMap<>();

        for (SpesaDipendente spesa : tutteLeSpese) {
            // Contiamo solo le spese approvate
            if (spesa.getStato().equalsIgnoreCase(SpesaDipendente.STATO_APPROVATA)) {
                String nome = spesa.getNomeDipendente();

                // Se il dipendente è già nella mappa, sommiamo; altrimenti iniziamo da 0
                double totaleAttuale = totali.getOrDefault(nome, 0.0);
                totali.put(nome, totaleAttuale + spesa.getImporto());
            }
        }
        return totali;
    }

    /**
     * Calcola il totale importi per ogni categoria (solo spese approvate).
     *
     * @return Mappa  categoria → totale approvato
     */
    public Map<String, Double> getTotalePerCategoria() {
        Map<String, Double> totali = new LinkedHashMap<>();

        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getStato().equalsIgnoreCase(SpesaDipendente.STATO_APPROVATA)) {
                String cat = spesa.getCategoria();
                double totaleAttuale = totali.getOrDefault(cat, 0.0);
                totali.put(cat, totaleAttuale + spesa.getImporto());
            }
        }
        return totali;
    }

    /**
     * Calcola il totale complessivo di tutte le spese approvate.
     */
    public double getTotaleGenerale() {
        double totale = 0.0;
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getStato().equalsIgnoreCase(SpesaDipendente.STATO_APPROVATA)) {
                totale += spesa.getImporto();
            }
        }
        return totale;
    }

    // ── Approvazione e rifiuto ───────────────────────────────────────────────

    /**
     * Approva la spesa con l'ID indicato.
     * Cerca l'oggetto nella lista e ne aggiorna lo stato.
     *
     * @param idSpesa  ID della spesa da approvare
     * @return true se trovata e aggiornata, false se non trovata
     */
    public boolean approvaSpesa(int idSpesa) {
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getIdSpesa() == idSpesa) {
                spesa.setStato(SpesaDipendente.STATO_APPROVATA);
                return true; // Trovata → usciamo subito
            }
        }
        return false; // Non trovata
    }

    /**
     * Rifiuta la spesa con l'ID indicato.
     *
     * @param idSpesa  ID della spesa da rifiutare
     * @return true se trovata e aggiornata, false se non trovata
     */
    public boolean rifiutaSpesa(int idSpesa) {
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (spesa.getIdSpesa() == idSpesa) {
                spesa.setStato(SpesaDipendente.STATO_RIFIUTATA);
                return true;
            }
        }
        return false;
    }

    // ── Utilità ──────────────────────────────────────────────────────────────

    /**
     * Restituisce la lista dei nomi dipendenti distinti (senza duplicati).
     * Utile per popolare i menu a tendina nella GUI.
     */
    public ArrayList<String> getNomiDipendenti() {
        ArrayList<String> nomi = new ArrayList<>();
        for (SpesaDipendente spesa : tutteLeSpese) {
            if (!nomi.contains(spesa.getNomeDipendente())) {
                nomi.add(spesa.getNomeDipendente());
            }
        }
        return nomi;
    }
}

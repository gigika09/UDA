package util;

import model.SpesaDipendente;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Classe di utilità per leggere il file CSV e restituire
 * una lista di oggetti SpesaDipendente.
 *
 * ─── Come funziona la lettura CSV in Java ────────────────────────────────────
 *
 * 1. FileReader apre il file dal percorso indicato.
 * 2. BufferedReader lo "avvolge" per leggere riga per riga in modo efficiente.
 * 3. Per ogni riga usiamo split(",") per separare i campi.
 * 4. Convertiamo i tipi (es. importo da String a double) e creiamo l'oggetto.
 * 5. Aggiungiamo l'oggetto all'ArrayList che verrà restituito.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class CsvReader {

    /**
     * Legge il file CSV al percorso indicato e restituisce
     * un ArrayList di SpesaDipendente.
     *
     * @param percorsoFile  Il percorso del file CSV (es. "spese.csv")
     * @return              Lista di tutte le spese lette dal file
     * @throws IOException  Se il file non esiste o non è leggibile
     */
    public static ArrayList<SpesaDipendente> leggiCsv(String percorsoFile) throws IOException {

        // Lista che conterrà tutte le spese lette
        ArrayList<SpesaDipendente> listaSpeseue = new ArrayList<>();

        /*
         * try-with-resources: Java chiude automaticamente FileReader e
         * BufferedReader al termine del blocco, anche in caso di eccezione.
         */
        try (BufferedReader br = new BufferedReader(new FileReader(percorsoFile))) {

            String riga;
            boolean primaRiga = true; // Serve per saltare l'intestazione (header)

            // readLine() restituisce null quando il file è finito
            while ((riga = br.readLine()) != null) {

                // Salta la prima riga (l'intestazione del CSV)
                if (primaRiga) {
                    primaRiga = false;
                    continue;
                }

                // Salta righe vuote
                if (riga.trim().isEmpty()) {
                    continue;
                }

                // Separa i campi usando la virgola come delimitatore
                // Nota: split con -1 include anche i campi vuoti in fondo
                String[] campi = riga.split(",", -1);

                // Controlla che ci siano esattamente 8 colonne
                if (campi.length != 8) {
                    System.err.println("⚠ Riga ignorata (numero campi errato): " + riga);
                    continue;
                }

                try {
                    // ── Parsing dei singoli campi ────────────────────────────

                    // Campo 0: idSpesa  (int)
                    int idSpesa = Integer.parseInt(campi[0].trim());

                    // Campo 1: nomeDipendente  (String)
                    String nomeDipendente = campi[1].trim();

                    // Campo 2: reparto  (String)
                    String reparto = campi[2].trim();

                    // Campo 3: data  (String nel formato YYYY-MM-DD)
                    String data = campi[3].trim();

                    // Campo 4: categoria  (String: vitto/trasporto/alloggio/altro)
                    String categoria = campi[4].trim().toLowerCase();

                    // Campo 5: descrizione  (String)
                    String descrizione = campi[5].trim();

                    // Campo 6: importo  (double)
                    // replace sostituisce la virgola decimale con il punto
                    double importo = Double.parseDouble(campi[6].trim().replace(",", "."));

                    // Campo 7: stato  (String: in attesa/approvata/rifiutata)
                    String stato = campi[7].trim().toLowerCase();

                    // Crea l'oggetto SpesaDipendente e aggiungilo alla lista
                    SpesaDipendente spesa = new SpesaDipendente(
                            idSpesa, nomeDipendente, reparto,
                            data, categoria, descrizione,
                            importo, stato
                    );
                    listaSpeseue.add(spesa);

                } catch (NumberFormatException e) {
                    // Se parseInt o parseDouble fallisce, avvisiamo e saltiamo la riga
                    System.err.println("Riga ignorata (errore di formato): " + riga);
                }
            }
        }

        System.out.println("Lette " + listaSpeseue.size() + " spese dal file: " + percorsoFile);
        return listaSpeseue;
    }
}

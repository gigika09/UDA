package util;

import model.SpesaDipendente;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Classe di utilità per salvare la lista di spese su file CSV.
 * Viene usata dopo aver approvato o rifiutato un rimborso,
 * in modo da rendere le modifiche persistenti.
 */
public class CsvWriter {

    // Intestazione fissa del file CSV
    private static final String INTESTAZIONE =
            "idSpesa,nomeDipendente,reparto,data,categoria,descrizione,importo,stato";

    /**
     * Scrive l'intera lista di spese nel file indicato.
     * Il file viene sovrascritto completamente.
     *
     * @param percorsoFile  Percorso del file di destinazione
     * @param listaSpese    Lista aggiornata di spese da salvare
     * @throws IOException  Se il file non è scrivibile
     */
    public static void scriviCsv(String percorsoFile, ArrayList<SpesaDipendente> listaSpese)
            throws IOException {

        /*
         * FileWriter con secondo argomento "false" significa SOVRASCRITTURA.
         * Se mettessimo "true" aggiungerebbe in coda al file esistente.
         * BufferedWriter migliora le prestazioni raggruppando le scritture.
         */
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(percorsoFile, false))) {

            // Scrivi l'intestazione
            bw.write(INTESTAZIONE);
            bw.newLine();

            // Scrivi ogni spesa come riga CSV
            for (SpesaDipendente spesa : listaSpese) {

                // Costruiamo la riga manualmente campo per campo
                String riga = spesa.getIdSpesa()          + ","
                            + spesa.getNomeDipendente()   + ","
                            + spesa.getReparto()          + ","
                            + spesa.getData()             + ","
                            + spesa.getCategoria()        + ","
                            + spesa.getDescrizione()      + ","
                            + spesa.getImporto()          + ","
                            + spesa.getStato();

                bw.write(riga);
                bw.newLine();
            }
        }

        System.out.println("✔ File CSV salvato: " + percorsoFile);
    }
}

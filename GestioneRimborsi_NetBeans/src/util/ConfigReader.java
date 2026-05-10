package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Legge un file di configurazione in formato .ini / .properties.
 *
 * Formato atteso:
 *   # Commento (riga ignorata)
 *   chiave=valore
 *
 * Esempio: config.ini
 *   username=admin
 *   password=rimborsi2024
 */
public class ConfigReader {

    /**
     * Legge il file indicato e restituisce una mappa chiave → valore.
     *
     * @param percorso  Percorso del file config.ini
     * @return          Mappa con le proprietà lette
     * @throws IOException  Se il file non esiste o non è leggibile
     */
    public static Map<String, String> leggi(String percorso) throws IOException {
        Map<String, String> config = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(percorso))) {
            String riga;
            while ((riga = br.readLine()) != null) {
                riga = riga.trim();

                // Salta commenti e righe vuote
                if (riga.isEmpty() || riga.startsWith("#")) continue;

                // Separa su "=" (massimo 2 parti: chiave e valore)
                String[] parti = riga.split("=", 2);
                if (parti.length == 2) {
                    config.put(parti[0].trim(), parti[1].trim());
                }
            }
        }
        return config;
    }
}

package model;

/**
 * Classe che rappresenta una singola spesa di un dipendente.
 * Ogni oggetto di questa classe corrisponde a una riga del file CSV.
 *
 * Struttura CSV:
 *   idSpesa, nomeDipendente, reparto, data, categoria, descrizione, importo, stato
 */
public class SpesaDipendente {

    // ── Costanti per i valori ammessi dei campi ──────────────────────────────

    // Categorie possibili
    public static final String CAT_VITTO      = "vitto";
    public static final String CAT_TRASPORTO  = "trasporto";
    public static final String CAT_ALLOGGIO   = "alloggio";
    public static final String CAT_ALTRO      = "altro";

    // Stati possibili della richiesta di rimborso
    public static final String STATO_IN_ATTESA = "in attesa";
    public static final String STATO_APPROVATA = "approvata";
    public static final String STATO_RIFIUTATA = "rifiutata";

    // ── Attributi ────────────────────────────────────────────────────────────

    private int    idSpesa;
    private String nomeDipendente;
    private String reparto;
    private String data;          // Formato stringa "YYYY-MM-DD"
    private String categoria;     // vitto / trasporto / alloggio / altro
    private String descrizione;
    private double importo;
    private String stato;         // in attesa / approvata / rifiutata

    // ── Costruttore ──────────────────────────────────────────────────────────

    /**
     * Costruisce un oggetto SpesaDipendente con tutti i campi.
     * Viene chiamato dal CsvReader quando legge ogni riga del file.
     */
    public SpesaDipendente(int idSpesa, String nomeDipendente, String reparto,
                           String data, String categoria, String descrizione,
                           double importo, String stato) {
        this.idSpesa        = idSpesa;
        this.nomeDipendente = nomeDipendente;
        this.reparto        = reparto;
        this.data           = data;
        this.categoria      = categoria;
        this.descrizione    = descrizione;
        this.importo        = importo;
        this.stato          = stato;
    }

    // ── Getter e Setter ──────────────────────────────────────────────────────

    public int    getIdSpesa()          { return idSpesa; }
    public String getNomeDipendente()   { return nomeDipendente; }
    public String getReparto()          { return reparto; }
    public String getData()             { return data; }
    public String getCategoria()        { return categoria; }
    public String getDescrizione()      { return descrizione; }
    public double getImporto()          { return importo; }
    public String getStato()            { return stato; }

    /** Aggiorna lo stato (es. da "in attesa" a "approvata" o "rifiutata") */
    public void setStato(String stato)  { this.stato = stato; }

    // ── Metodo toString ──────────────────────────────────────────────────────

    /**
     * Rappresentazione testuale utile per debug e per ricostruire una riga CSV.
     */
    @Override
    public String toString() {
        return idSpesa + "," + nomeDipendente + "," + reparto + "," + data + ","
             + categoria + "," + descrizione + "," + importo + "," + stato;
    }
}

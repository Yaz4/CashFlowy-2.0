package CashFlowy.service.analytics;

import CashFlowy.persistence.model.Transaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAnalyticsService implements AnalyticsService {

    @Override
    public Map<String, Double> getSpesePerCategoria(List<Transaction> transactions) {
        // Creiamo una mappa vuota per i risultati
        Map<String, Double> risultati = new HashMap<>();

        // Scorriamo tutte le transazioni una per una
        for (Transaction t : transactions) {

            // Ci interessano solo le uscite
            if ("Uscita".equals(t.getTipo())) {
                String categoria = t.getCategoria();
                // Usiamo Math.abs per assicurarci che l'importo sia positivo
                double importo = Math.abs(t.getImporto());

                // Se la categoria è già nella mappa, sommiamo il nuovo importo a quello vecchio
                if (risultati.containsKey(categoria)) {
                    double vecchioTotale = risultati.get(categoria);
                    risultati.put(categoria, vecchioTotale + importo);
                } else {
                    // Altrimenti inseriamo la categoria per la prima volta
                    risultati.put(categoria, importo);
                }
            }
        }
        return risultati;
    }

    @Override
    public Map<Integer, Double> getEntrateMensili(List<Transaction> transactions) {
        // Usiamo un metodo di supporto per non riscrivere codice simile due volte
        return calcolaTotaliPerMese(transactions, "Entrata");
    }

    @Override
    public Map<Integer, Double> getUsciteMensili(List<Transaction> transactions) {
        // Usiamo lo stesso metodo, ma cercando le "Uscite"
        return calcolaTotaliPerMese(transactions, "Uscita");
    }

    // Metodo privato "helper" scritto in modo semplice
    private Map<Integer, Double> calcolaTotaliPerMese(List<Transaction> transactions, String tipoDaCercare) {
        Map<Integer, Double> mappaMesi = new HashMap<>();

        // 1. Inizializziamo la mappa con tutti i 12 mesi a 0.0
        // (Così il grafico mostrerà anche i mesi vuoti invece di saltarli)
        for (int i = 1; i <= 12; i++) {
            mappaMesi.put(i, 0.0);
        }

        // 2. Scorriamo le transazioni
        for (Transaction t : transactions) {
            // Controlliamo se il tipo corrisponde (Entrata o Uscita)
            if (tipoDaCercare.equals(t.getTipo())) {

                // Otteniamo il numero del mese (es. 1 per Gennaio) dalla data
                int mese = t.getData().getMonthValue();
                double importo = Math.abs(t.getImporto());

                // Aggiorniamo il totale per quel mese
                double totaleAttuale = mappaMesi.get(mese);
                mappaMesi.put(mese, totaleAttuale + importo);
            }
        }
        return mappaMesi;
    }
}
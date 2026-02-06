package CashFlowy.service.analytics;

import CashFlowy.persistence.model.Transaction;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {

    // Calcola il totale delle uscite per ogni categoria
    Map<String, Double> getSpesePerCategoria(List<Transaction> transactions);

    // Calcola il totale delle entrate per ogni mese (1=Gennaio, ..., 12=Dicembre)
    Map<Integer, Double> getEntrateMensili(List<Transaction> transactions);

    // Calcola il totale delle uscite per ogni mese (1=Gennaio, ..., 12=Dicembre)
    Map<Integer, Double> getUsciteMensili(List<Transaction> transactions);
}
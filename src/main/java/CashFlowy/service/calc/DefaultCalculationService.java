package CashFlowy.service.calc;

import CashFlowy.persistence.model.Transaction;
import javafx.collections.ObservableList;

public class DefaultCalculationService implements CalculationService {
    @Override
    public double calcolaTotalePerTipo(String tipo, ObservableList<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getTipo().equals(tipo))
                .mapToDouble(Transaction::getImporto)
                .sum();
    }

    @Override
    public double calcolaPatrimonio(ObservableList<Transaction> transactions) {
        double totale = transactions.stream().mapToDouble(Transaction::getImporto).sum();
        double investimentiUscite = transactions.stream()
                .filter(t -> t.getTipo().equals("Uscita") && t.getCategoria().equals("INVESTIMENTI"))
                .mapToDouble(Transaction::getImporto)
                .sum();
        return totale - investimentiUscite;
    }
}

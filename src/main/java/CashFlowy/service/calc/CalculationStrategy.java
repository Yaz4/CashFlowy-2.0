package CashFlowy.service.calc;

import CashFlowy.persistence.model.Transaction;
import javafx.collections.ObservableList;

public interface CalculationStrategy {
    double calcolaTotalePerTipo(String tipo, ObservableList<Transaction> transactions);
    double calcolaPatrimonio(ObservableList<Transaction> transactions);
}

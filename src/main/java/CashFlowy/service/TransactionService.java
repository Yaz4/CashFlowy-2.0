package CashFlowy.service;

import CashFlowy.persistence.model.Transaction;
import CashFlowy.service.calc.CalculationStrategy;
import CashFlowy.service.calc.DefaultCalculationStrategy;
import javafx.collections.ObservableList;

public class TransactionService {

    private CalculationStrategy calculationStrategy;

    public TransactionService() {
        this.calculationStrategy = new DefaultCalculationStrategy();
    }

    public TransactionService(CalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
    }

    public void setCalculationStrategy(CalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
    }

    public double aggiornaTotale(String tipo, ObservableList<Transaction> transactions) {
        return calculationStrategy.calcolaTotalePerTipo(tipo, transactions);
    }

    public double aggiornaPatrimonio(ObservableList<Transaction> transactions) {
        return calculationStrategy.calcolaPatrimonio(transactions);
    }
}

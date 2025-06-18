package CashFlowy.service;

import CashFlowy.persistence.model.Transaction;
import javafx.collections.ObservableList;


public class TransactionService {

    public double aggiornaTotale(String tipo, ObservableList<Transaction> transactions) {
        return transactions.stream().filter(Transaction -> Transaction.getTipo().equals(tipo)).mapToDouble(Transaction::getImporto).sum();
    }

    public double aggiornaPatrimonio(ObservableList<Transaction> transactions) {
        return transactions.stream().mapToDouble(Transaction::getImporto).sum() - transactions.stream().filter(Transaction -> Transaction.getTipo().equals("Uscita") && Transaction.getCategoria().equals("INVESTIMENTI")).mapToDouble(Transaction::getImporto).sum();
    }


}

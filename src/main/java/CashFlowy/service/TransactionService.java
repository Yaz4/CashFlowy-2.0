package CashFlowy.service;

import CashFlowy.persistence.model.Transaction;
import CashFlowy.persistence.repository.TransactionRepository;
import CashFlowy.service.calc.CalculationStrategy;
import CashFlowy.service.calc.DefaultCalculationStrategy;
import javafx.collections.ObservableList;

import java.util.Optional;

public class TransactionService {

    private CalculationStrategy calculationStrategy;
    private TransactionRepository transactionRepository;

    // Costruttore vuoto per compatibilità (ma sconsigliato se non si setta il repository dopo)
    public TransactionService() {
        this.calculationStrategy = new DefaultCalculationStrategy();
    }

    // Costruttore con Repository (Dependency Injection manuale)
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.calculationStrategy = new DefaultCalculationStrategy();
    }

    public TransactionService(TransactionRepository transactionRepository, CalculationStrategy calculationStrategy) {
        this.transactionRepository = transactionRepository;
        this.calculationStrategy = calculationStrategy;
    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setCalculationStrategy(CalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
    }

    // Metodi di calcolo (esistenti)
    public double aggiornaTotale(String tipo, ObservableList<Transaction> transactions) {
        return calculationStrategy.calcolaTotalePerTipo(tipo, transactions);
    }

    public double aggiornaPatrimonio(ObservableList<Transaction> transactions) {
        return calculationStrategy.calcolaPatrimonio(transactions);
    }

    // --- Metodi CRUD che delegano al Repository ---

    public Iterable<Transaction> findAll() {
        if (transactionRepository == null) {
            throw new IllegalStateException("TransactionRepository non inizializzato nel Service");
        }
        return transactionRepository.findAll();
    }

    public Transaction save(Transaction transaction) {
        if (transactionRepository == null) {
            throw new IllegalStateException("TransactionRepository non inizializzato nel Service");
        }
        // Qui potremmo aggiungere logica di validazione extra prima di salvare
        return transactionRepository.save(transaction);
    }

    public void deleteById(Long id) {
        if (transactionRepository == null) {
            throw new IllegalStateException("TransactionRepository non inizializzato nel Service");
        }
        transactionRepository.deleteById(id);
    }
    
    public Optional<Transaction> findById(Long id) {
         if (transactionRepository == null) {
            throw new IllegalStateException("TransactionRepository non inizializzato nel Service");
        }
        return transactionRepository.findById(id);
    }
}

package CashFlowy.service;

import CashFlowy.persistence.model.Transaction;
import CashFlowy.persistence.repository.TransactionRepository;
import CashFlowy.service.calc.CalculationService;
import CashFlowy.service.calc.DefaultCalculationService;
import javafx.collections.ObservableList;

import java.util.Optional;

public class TransactionService {

    private CalculationService calculationService;
    private TransactionRepository transactionRepository;

    // Costruttore vuoto per compatibilità (ma sconsigliato se non si setta il repository dopo)
    public TransactionService() {
        this.calculationService = new DefaultCalculationService();
    }

    // Costruttore con Repository (Dependency Injection manuale)
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.calculationService = new DefaultCalculationService();
    }

    public TransactionService(TransactionRepository transactionRepository, CalculationService calculationService) {
        this.transactionRepository = transactionRepository;
        this.calculationService = calculationService;
    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setCalculationStrategy(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    // Metodi di calcolo (esistenti)
    public double aggiornaTotale(String tipo, ObservableList<Transaction> transactions) {
        return calculationService.calcolaTotalePerTipo(tipo, transactions);
    }

    public double aggiornaPatrimonio(ObservableList<Transaction> transactions) {
        return calculationService.calcolaPatrimonio(transactions);
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

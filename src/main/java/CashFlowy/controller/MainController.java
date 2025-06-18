package CashFlowy.controller;


import CashFlowy.persistence.model.Transaction;
import CashFlowy.persistence.repository.TransactionRepository;
import CashFlowy.service.ChartService;
import CashFlowy.service.TransactionService;
import com.zaxxer.hikari.HikariDataSource;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MainController {


    @FXML private TextField descrizioneField;
    @FXML private TextField importoField;
    @FXML private DatePicker dataField;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> tipoChoiceBox;
    @FXML private ChoiceBox<String> yearSelection;
    @FXML private ChoiceBox<String> categoriaChoiceBox;
    @FXML private TableView<Transaction> tabella;
    @FXML private TableColumn<Transaction, String> categoriaCol;
    @FXML private TableColumn<Transaction, String> descrizioneCol;
    @FXML private TableColumn<Transaction, String> tipoCol;
    @FXML private TableColumn<Transaction, String> dataCol;
    @FXML private TableColumn<Transaction, Double> importoCol;
    HikariDataSource hikariDataSource;
    private FilteredList<Transaction> filteredData;
    private TransactionRepository transactionRepository;
    @FXML private Label patrimonioLabel;
    @FXML private Label saldoLabel;
    @FXML private Label entrateTotaliLabel;
    @FXML private Label usciteTotaliLabel;
    @FXML private PieChart pieChartCategorie;
    @FXML private BarChart BarChartMensile;
    @FXML private Label welcomeBack;


    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private ChartService chartService;
    private TransactionService transactionService;



    public void initDataSource(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
        this.transactionRepository = new TransactionRepository(hikariDataSource);
        this.transactionService = new TransactionService();
        this.chartService=new ChartService();
        Iterable<Transaction> savedTransactions = transactionRepository.findAll();
        for(Transaction savedTransaction: savedTransactions) {
            transactions.add(savedTransaction);
        }
        patrimonioLabel.setText(String.format("€ %.2f", transactionService.aggiornaPatrimonio(transactions)));
        aggiornaPagina();
    }

    @FXML
    public void initialize(){

        welcomeBack.setText("Bentornato\n" + LoginController.getUsername());





        /*COLONNA TIPO*/
        tipoChoiceBox.setItems(FXCollections.observableArrayList("Entrata", "Uscita"));
        tipoChoiceBox.setValue("Entrata");
        tipoChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean enableCategoria = newVal.equals("Uscita");
            categoriaChoiceBox.setDisable(!enableCategoria);
            //se il tipo non è più uscita pulisco il campo categoria
            if (!enableCategoria) {
                categoriaChoiceBox.getSelectionModel().clearSelection();
            }
        });

        /*DATA INIZIALE*/
        dataField.setValue(LocalDate.now());


        categoriaChoiceBox.setDisable(!tipoChoiceBox.getValue().equals("Uscita")); //la disabilito inizialmente
        categoriaChoiceBox.setItems(FXCollections.observableArrayList("Bollette/Imposte", "Abbonamenti", "Carburante", "Salute", "Svago", "Regalo", "Spese Alimentari/casa", "INVESTIMENTI"));
        categoriaChoiceBox.setValue("Categoria");




        /*COLONNA CATEGORIA*/
        categoriaCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria()));
        categoriaCol.setOnEditCommit(event -> { //possibilità di modificare i valori direttamente nella tabella
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setCategoria(event.getNewValue());
            transactionRepository.save(selectedTransaction);
        });

        /*COLONNA DESCRIZIONE*/
        descrizioneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescrizione()));
        descrizioneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descrizioneCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setDescrizione(event.getNewValue());
            transactionRepository.save(selectedTransaction);
        });

        /*COLONNA TIPO*/
        tipoCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipo()));
        tipoCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setTipo(event.getNewValue());
            transactionRepository.save(selectedTransaction);
        });

        /*COLONNA DATA*/
        dataCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData().toString()));
        dataCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dataCol.setOnEditCommit(event -> {
            Transaction transazione = event.getRowValue();
            try { //nell'aggiornarla controllo che il formato sia giusto
                LocalDate nuovaData = LocalDate.parse(event.getNewValue());
                transazione.setData(nuovaData);
                transactionRepository.save(transazione);
            } catch (Exception e) { //per prevenire una parsing exception
                mostraErrore("Formato data non valido. Usa AAAA-MM-GG.");
            }
        });

        /*COLONNA IMPORTO*/
        importoCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getImporto()).asObject());
        importoCol.setOnEditCommit(event -> {

            try {
                Transaction selectedTransaction = event.getRowValue();
                double newValue = Double.parseDouble(event.getNewValue().toString());
                selectedTransaction.setImporto(newValue);
                transactionRepository.save(selectedTransaction);
            } catch (NumberFormatException e) {
                mostraErrore("Importo non valido. Inserire un valore numerico");
            }
        });


        pieChartCategorie.setTitle("Analisi Spese");
        BarChartMensile.setTitle("Entrate e Spese Mensili");


        filteredData = new FilteredList<>(transactions, t -> true); //predicato presente in aggiornaFiltroGlobale()
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            aggiornaFiltroGlobale(); // deve rispettare il filtro globale
            //transactionService.setTransactions(filteredData);

        });
        tabella.setItems(filteredData);
        tabella.setEditable(true);




        yearSelection.setValue("Storico");
        yearSelection.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(!Objects.equals(oldVal,newVal)) {
                aggiornaFiltroGlobale(); //per poter utilizzare anche il filtro testuale
                aggiornaPagina(); //aggiorna tabella e grafici secondo il nuovo filtro
            }
        });


    }

    private void aggiornaFiltroGlobale() {
        String searchText = searchField.getText().toLowerCase();
        String annoSelezionato = yearSelection.getValue();

        filteredData.setPredicate(t -> {
            boolean matchAnno = annoSelezionato == null || annoSelezionato.equals("Storico")
                    || String.valueOf(t.getData().getYear()).equals(annoSelezionato);

            boolean matchRicerca = searchText == null || searchText.isEmpty()
                    || (t.getDescrizione() != null && t.getDescrizione().toLowerCase().contains(searchText))
                    || (t.getCategoria() != null && t.getCategoria().toLowerCase().contains(searchText))
                    || (t.getTipo() != null && t.getTipo().toLowerCase().contains(searchText))
                    || (t.getData() != null && t.getData().toString().contains(searchText));

            return matchAnno && matchRicerca; //devo assicurarmi che sia l'anno, sia il testo cercato vengano soddisfatti
        });
    }

    /*-----------------------------------
     * METODI PER AGIRE SULLE transactions*/

    @FXML
    public void aggiungiTransazione() {
        String categoria = categoriaChoiceBox.getValue();
        String descrizione = descrizioneField.getText();
        String tipo = tipoChoiceBox.getValue();
        LocalDate data = dataField.getValue();
        double importo;

        try {
            importo = Double.parseDouble(importoField.getText());
        } catch (NumberFormatException e) {
            mostraErrore("Importo non valido. Inserire un valore numerico");
            return;
        }

        if (tipo.equals("Uscita")) {
            if(categoria == null || categoria.equals("Categoria")){
                mostraErrore("Scegli una categoria valida per l'uscita.");
                return;
            }
            importo = -Math.abs(importo);
        } else if(tipo.equals("Entrata")) {categoria = null;}//categoria non può rimanere impostato a "Categoria"
        Transaction transazione = new Transaction(categoria,descrizione, importo, data, tipo);

        try {

            transactionRepository.save(transazione);
            System.out.println("Transazione salvata con ID: " + transazione.getId()); //stampa di debug


            transactions.add(transazione);

            descrizioneField.clear();
            importoField.clear();
            patrimonioLabel.setText(String.format("€ %.2f", transactionService.aggiornaPatrimonio(transactions)));
            aggiornaPagina();
        } catch (Exception e) {
            mostraErrore("Errore durante il salvataggio: " + e.getMessage());
        }


    }

    public void rimuoviTransazione() {
        Transaction selezionata = tabella.getSelectionModel().getSelectedItem();

        if (selezionata == null) {
            mostraErrore("Seleziona una transazione da rimuovere.");
            return;
        }

        try {
            transactionRepository.deleteById(selezionata.getId());
            transactions.remove(selezionata);
            patrimonioLabel.setText(String.format("€ %.2f", transactionService.aggiornaPatrimonio(transactions)));
            aggiornaPagina();
        } catch (Exception e) {
            mostraErrore("Errore nella rimozione della transazione: " + e.getMessage());
        }
    }

    private void aggiornaOpzioniAnni() {
        String valoreCorrente = yearSelection.getValue();

        Set<String> anniDisponibili = transactions.stream()
                .map(t -> String.valueOf(t.getData().getYear()))
                .collect(Collectors.toCollection(TreeSet::new));

        List<String> nuoveOpzioni = new ArrayList<>();
        nuoveOpzioni.add("Storico");
        nuoveOpzioni.addAll(anniDisponibili);

        // Aggiorna la lista delle opzioni solo se è cambiata
        if (!yearSelection.getItems().equals(nuoveOpzioni)) {
            yearSelection.setItems(FXCollections.observableArrayList(nuoveOpzioni));
        }

        // Imposta il valore solo se è necessario
        if (!nuoveOpzioni.contains(valoreCorrente)) {
            yearSelection.setValue("Storico");
        } else if (!valoreCorrente.equals(yearSelection.getValue())) {
            yearSelection.setValue(valoreCorrente);
        }
    }


    /* Metodi per segnalare errori o informazioni*/
    private void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    @FXML
    private void displayInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("CashFlowy 1.0");
        alert.setContentText("Benvenuto su CashFlowy\n Prendi il controllo del tuo portafoglio! ");
        alert.showAndWait();
    }



    /*metodo per esportare file excel con le transazioni -> dependency Apache POI*/

    public void esportaExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva come Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Transazioni");
                Row header = sheet.createRow(0);

                // intestazioni
                for (int i = 0; i < tabella.getColumns().size(); i++) {
                    header.createCell(i).setCellValue(tabella.getColumns().get(i).getText());
                }

                // righe
                for (int i = 0; i < tabella.getItems().size(); i++) {
                    Transaction t = tabella.getItems().get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(t.getCategoria());
                    row.createCell(1).setCellValue(t.getDescrizione());
                    row.createCell(2).setCellValue(t.getTipo());
                    row.createCell(3).setCellValue(t.getData().toString());
                    row.createCell(4).setCellValue(t.getImporto());
                }

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void aggiornaPagina(){
        System.out.println("Aggiorna pagina"); //stampa di debug


        entrateTotaliLabel.setText(String.format("Entrate Totali: € %.2f", transactionService.aggiornaTotale("Entrata",filteredData)));
        usciteTotaliLabel.setText(String.format("Uscite Totali: € %.2f", transactionService.aggiornaTotale("Uscita", filteredData)));
        saldoLabel.setText(String.format("Saldo: € %.2f",transactionService.aggiornaTotale("Entrata", filteredData) + transactionService.aggiornaTotale("Uscita", filteredData )));
        chartService.aggiornaGraficoCategorie(pieChartCategorie, filteredData);
        chartService.aggiornaGraficoMensile(BarChartMensile, filteredData);
        aggiornaOpzioniAnni();
    }


}
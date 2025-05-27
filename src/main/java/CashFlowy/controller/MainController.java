package CashFlowy.controller;


import CashFlowy.persistence.model.Transaction;
import CashFlowy.persistence.repository.TransactionRepository;
import com.zaxxer.hikari.HikariDataSource;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MainController {

    @FXML private TextField descrizioneField;
    @FXML private TextField importoField;
    @FXML private DatePicker dataField;
    @FXML private ChoiceBox<String> tipoChoiceBox;
    @FXML private ChoiceBox<String> categoriaChoiceBox;
    @FXML private TableView<Transaction> tabella;
    @FXML private TableColumn<Transaction, String> categoriaCol;
    @FXML private TableColumn<Transaction, String> descrizioneCol;
    @FXML private TableColumn<Transaction, String> tipoCol;
    @FXML private TableColumn<Transaction, String> dataCol;
    @FXML private TableColumn<Transaction, Double> importoCol;
    HikariDataSource hikariDataSource;
    private TransactionRepository transactionRepository;
    @FXML private Label saldoLabel;
    @FXML private Label entrateTotaliLabel;
    @FXML private Label usciteTotaliLabel;
    @FXML private PieChart pieChartCategorie;


    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();


    public void initDataSource(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
        this.transactionRepository = new TransactionRepository(hikariDataSource);
        Iterable<Transaction> savedTransactions = transactionRepository.findAll();
        transactions.addAll(StreamSupport.stream(savedTransactions.spliterator(), false).collect(Collectors.toList()));
        aggiornaSaldo();
        aggiornaEntrateTotali();
        aggiornaUsciteTotali();
        aggiornaGraficoCategorie();
    }

    @FXML
    public void initialize() throws SQLException {


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
        categoriaChoiceBox.setDisable(!tipoChoiceBox.getValue().equals("Uscita")); //la disabilito inizialmente
        categoriaChoiceBox.setItems(FXCollections.observableArrayList("Bollette", "Abbonamenti", "Carburante", "Salute", "Svago"));
        categoriaChoiceBox.setValue("Categoria");
        dataField.setValue(LocalDate.now());






        categoriaCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategoria()));
        categoriaCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setTipo(event.getNewValue());;
            transactionRepository.save(selectedTransaction);
        });

        descrizioneCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescrizione()));
        descrizioneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descrizioneCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setDescrizione(event.getNewValue());;
            transactionRepository.save(selectedTransaction);
        });

        tipoCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTipo()));
        tipoCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setTipo(event.getNewValue());;
            transactionRepository.save(selectedTransaction);
        });


        dataCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData().toString()));
        dataCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dataCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setDescrizione(event.getNewValue());;
            transactionRepository.save(selectedTransaction);
        });

        importoCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getImporto()).asObject());
        importoCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setDescrizione(event.getNewValue().toString());;
            transactionRepository.save(selectedTransaction);
        });

        pieChartCategorie.setTitle("Analisi Spese");
        tabella.setItems(transactions);
        tabella.setEditable(true);
        aggiornaSaldo();
        aggiornaUsciteTotali();
        aggiornaEntrateTotali();
        aggiornaGraficoCategorie();
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

        if (tipo.equals("Uscita")) importo = -Math.abs(importo);
        Transaction transazione = new Transaction(categoria,descrizione, importo, data, tipo);

        try {

            transactionRepository.save(transazione);
            System.out.println("Transazione salvata con ID: " + transazione.getId());


            transactions.add(transazione);

            descrizioneField.clear();
            importoField.clear();
            aggiornaSaldo();
            aggiornaEntrateTotali();
            aggiornaUsciteTotali();
            aggiornaGraficoCategorie();
        } catch (Exception e) {
            mostraErrore("Errore durante il salvataggio: " + e.getMessage());
        }


    }

    public void rimuoviTransazione(ActionEvent event) {
        Transaction selezionata = tabella.getSelectionModel().getSelectedItem();

        if (selezionata == null) {
            mostraErrore("Seleziona una transazione da rimuovere.");
            return;
        }

        try {
            transactionRepository.deleteById(selezionata.getId());
            transactions.remove(selezionata);
            aggiornaSaldo();
            aggiornaEntrateTotali();
            aggiornaUsciteTotali();
            aggiornaGraficoCategorie();
        } catch (Exception e) {
            mostraErrore("Errore nella rimozione della transazione: " + e.getMessage());
        }
    }



    /*-------------------------------------------------*/
    /*METODI PER AGGIORNARE I VALORI*/

    private void aggiornaUsciteTotali() {
        double usciteTotali = transactions.stream().filter(Transaction -> Transaction.getTipo().equals("Uscita")).mapToDouble(Transaction::getImporto).sum();
        usciteTotaliLabel.setText(String.format("Uscite Totali: € %.2f", usciteTotali));
    }

    private void aggiornaEntrateTotali() {
        double entrateTotali = transactions.stream().filter(Transaction -> Transaction.getTipo().equals("Entrata")).mapToDouble(Transaction::getImporto).sum();
        entrateTotaliLabel.setText(String.format("Entrate Totali: € %.2f", entrateTotali));
    }


    private void aggiornaSaldo() {
        double saldo = transactions.stream().mapToDouble(Transaction::getImporto).sum();
        saldoLabel.setText(String.format("Saldo: € %.2f", saldo));
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

    /*metodo per esportare file excel con le transazioni -> dipendenza Apache POI*/

    public void esportaExcel(ActionEvent event) {
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

    //Metodo per aggiornare il grafico a torta delle spese suddivise per categoria
    private void aggiornaGraficoCategorie() {
        // Raggruppa le spese per categoria (considerando solo "Uscita")
        Map<String, Double> spesePerCategoria = transactions.stream()
                .filter(t -> "Uscita".equals(t.getTipo()))
                .collect(Collectors.groupingBy(Transaction::getCategoria, Collectors.summingDouble(Transaction::getImporto)));

        // Il valore è negativo per le uscite, quindi lo rendiamo positivo per il grafico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : spesePerCategoria.entrySet()) {
            double valorePositivo = -entry.getValue(); // inverti il segno
            pieChartData.add(new PieChart.Data(entry.getKey(), valorePositivo));
        }

        pieChartCategorie.setData(pieChartData);
    }


}


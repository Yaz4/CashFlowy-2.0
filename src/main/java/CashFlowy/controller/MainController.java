package CashFlowy.controller;


import CashFlowy.persistence.model.Transaction;
import CashFlowy.persistence.repository.TransactionRepository;
import com.zaxxer.hikari.HikariDataSource;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
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
import java.sql.SQLException;
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
    @FXML private BarChart StackedBarChartMensile;
    @FXML private Label welcomeBack;


    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();


    public void initDataSource(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
        this.transactionRepository = new TransactionRepository(hikariDataSource);
        Iterable<Transaction> savedTransactions = transactionRepository.findAll();
        transactions.addAll(StreamSupport.stream(savedTransactions.spliterator(), false).toList());
        aggiornaPatrimonio();
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

        /*COLONNA CATEGORIA*/
        categoriaChoiceBox.setDisable(!tipoChoiceBox.getValue().equals("Uscita")); //la disabilito inizialmente
        categoriaChoiceBox.setItems(FXCollections.observableArrayList("Bollette/Imposte", "Abbonamenti", "Carburante", "Salute", "Svago", "Regalo", "Spese Alimentari/casa", "INVESTIMENTI"));
        categoriaChoiceBox.setValue("Categoria");


        categoriaCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategoria()));
        categoriaCol.setOnEditCommit(event -> {
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
        StackedBarChartMensile.setTitle("Entrate e Spese Mensili");


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(t -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();

                return (t.getDescrizione() != null && t.getDescrizione().toLowerCase().contains(lowerCaseFilter)) ||
                        (t.getCategoria() != null && t.getCategoria().toLowerCase().contains(lowerCaseFilter)) ||
                        (t.getTipo() != null && t.getTipo().toLowerCase().contains(lowerCaseFilter)) ||
                        (t.getData() != null && t.getData().toString().contains(lowerCaseFilter));
            });
        });

        filteredData = new FilteredList<>(transactions, t -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            aggiornaFiltroGlobale(); // deve rispettare anche il filtro globale
        });
        tabella.setItems(filteredData);
        tabella.setEditable(true);




        yearSelection.setValue("Storico");
        yearSelection.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(!Objects.equals(oldVal,newVal)) {
                aggiornaPagina(); //aggiorna tabella e grafici secondo il nuovo filtro
                aggiornaFiltroGlobale(); //per poter utilizzare anche il filtro testuale
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

        if (tipo.equals("Uscita")) {importo = -Math.abs(importo);}
        if (tipo.equals("Uscita") && (categoria == null || categoria.equals("Categoria"))) {
            mostraErrore("Scegli una categoria valida per l'uscita.");
            return;
        } else if(tipo.equals("Entrata")) {categoria = null;}//categoria non può rimanere impostato a "Categoria"
        Transaction transazione = new Transaction(categoria,descrizione, importo, data, tipo);

        try {

            transactionRepository.save(transazione);
            System.out.println("Transazione salvata con ID: " + transazione.getId()); //stampa di debug


            transactions.add(transazione);

            descrizioneField.clear();
            importoField.clear();
            aggiornaPatrimonio();
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
            aggiornaPatrimonio();
            aggiornaPagina();
        } catch (Exception e) {
            mostraErrore("Errore nella rimozione della transazione: " + e.getMessage());
        }
    }

    private List<Transaction> filtraPerAnno() {
        String selezione = yearSelection.getValue();
        if (selezione == null || selezione.equals("Storico")) {
            return new ArrayList<>(transactions);
        }

        int anno = Integer.parseInt(selezione);
        return transactions.stream().filter(t -> t.getData().getYear() == anno).toList();
    }

    /*-------------------------------------------------*/
    /*METODI PER AGGIORNARE I VALORI*/

    private void aggiornaUsciteTotali() {
        double usciteTotali = filtraPerAnno().stream().filter(Transaction -> Transaction.getTipo().equals("Uscita")).mapToDouble(Transaction::getImporto).sum();
        usciteTotaliLabel.setText(String.format("Uscite Totali: € %.2f", usciteTotali));
    }

    private void aggiornaEntrateTotali() {
        double entrateTotali = filtraPerAnno().stream().filter(Transaction -> Transaction.getTipo().equals("Entrata")).mapToDouble(Transaction::getImporto).sum();
        entrateTotaliLabel.setText(String.format("Entrate Totali: € %.2f", entrateTotali));
    }
    private void aggiornaPatrimonio() {
        double patrimonio = transactions.stream().mapToDouble(Transaction::getImporto).sum() - transactions.stream().filter(Transaction -> Transaction.getTipo().equals("Uscita") && Transaction.getCategoria().equals("INVESTIMENTI")).mapToDouble(Transaction::getImporto).sum();;
        patrimonioLabel.setText(String.format("€ %.2f", patrimonio));
    }



    private void aggiornaSaldo() {
        double saldo = filtraPerAnno().stream().mapToDouble(Transaction::getImporto).sum();
        saldoLabel.setText(String.format("Saldo: € %.2f", saldo));
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



    /*metodo per esportare file excel con le transazioni -> dipendency Apache POI*/

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




    /*METODI PER AGGIORNARE I GRAFICI*/



    //Metodo per aggiornare il grafico a torta delle spese suddivise per categoria
    private void aggiornaGraficoCategorie() {
        // Raggruppa le spese per categoria (considerando solo "Uscita")
        Map<String, Double> spesePerCategoria = filtraPerAnno().stream()
                .filter(t -> "Uscita".equals(t.getTipo()))
                .collect(Collectors.groupingBy(Transaction::getCategoria, Collectors.summingDouble(Transaction::getImporto)));

        // Il valore è negativo per le uscite, quindi lo rendiamo positivo per il grafico
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : spesePerCategoria.entrySet()) {
            double valorePositivo = -entry.getValue(); // inverti il segno
            pieChartData.add(new PieChart.Data(entry.getKey(), valorePositivo));
        }

        pieChartCategorie.setData(pieChartData);

        double total = pieChartCategorie.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();

        // Set labels with percentages
        for (PieChart.Data data : pieChartCategorie.getData()) {
            double percentage = (data.getPieValue() / total) * 100;
            data.setName(String.format("%s (%.1f%%)", data.getName().split(" ")[0], percentage));
        }
    }

    //Grafico a barre sovrapposte per entrate/uscite Mensili
    private void aggiornaGraficoMensile() {
        StackedBarChartMensile.getData().clear(); // Pulisco eventuali dati precedenti


        String[] mesi = {"Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"};

        Map<Integer, Double> entratePerMese = new HashMap<>();
        Map<Integer, Double> uscitePerMese = new HashMap<>();

        // Inizializzo a zero
        for (int i = 1; i <= 12; i++) {
            entratePerMese.put(i, 0.0);
            uscitePerMese.put(i, 0.0);
        }

        for (Transaction t : filtraPerAnno()) {
            int mese = t.getData().getMonthValue();
            double importo = t.getImporto();
            if (t.getTipo().equals("Entrata")) {
                entratePerMese.put(mese, entratePerMese.get(mese) + importo);
            } else if (t.getTipo().equals("Uscita")) {
                uscitePerMese.put(mese, uscitePerMese.get(mese) + (-importo));
            }
        }

        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");

        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        for (int i = 1; i <= 12; i++) {
            String mese = mesi[i - 1];
            serieEntrate.getData().add(new XYChart.Data<>(mese, entratePerMese.get(i)));
            serieUscite.getData().add(new XYChart.Data<>(mese, uscitePerMese.get(i)));
        }

        StackedBarChartMensile.getData().addAll(serieEntrate, serieUscite);

    }


    public void aggiornaPagina(){
        System.out.println("Aggiorna pagina"); //stampa di debug

        aggiornaSaldo();
        aggiornaEntrateTotali();
        aggiornaUsciteTotali();
        aggiornaGraficoCategorie();
        aggiornaGraficoMensile();
        aggiornaOpzioniAnni();
    }


}


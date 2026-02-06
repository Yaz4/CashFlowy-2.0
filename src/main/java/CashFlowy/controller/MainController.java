package CashFlowy.controller;


import CashFlowy.persistence.model.Transaction;
import CashFlowy.persistence.repository.TransactionRepository;
import CashFlowy.service.analytics.AnalyticsService;
import CashFlowy.service.analytics.DefaultAnalyticsService;
import CashFlowy.service.TransactionService;
import CashFlowy.service.charts.MonthlyChartService;
import CashFlowy.service.charts.PieChartService;
import CashFlowy.service.event.TransactionListener;
import CashFlowy.service.export.ExportCSV;
import CashFlowy.service.validation.DefaultValidationService;
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
import CashFlowy.service.export.ExportExcel;
import CashFlowy.service.validation.ValidationService;

import java.text.NumberFormat;
import java.util.Locale;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.control.TableCell;

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
    // RIMOSSO: private TransactionRepository transactionRepository;
    @FXML private Label patrimonioLabel;
    @FXML private Label saldoLabel;
    @FXML private Label entrateTotaliLabel;
    @FXML private Label usciteTotaliLabel;
    @FXML private PieChart pieChartCategorie;
    @FXML private BarChart BarChartMensile;
    @FXML private Label welcomeBack;
    @FXML private Button aggiungiButton;
    @FXML private Button clearSearchButton;
    @FXML private Label resultsCountLabel;


    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    //private ChartService monthlyChartService;
    //private ChartService pieChartService;
    private AnalyticsService analyticsService;

    private TransactionService transactionService;
    private ExportExcel exportExcel;
    private ExportCSV exportCSV;
    private ValidationService validationService;



    public void initDataSource(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
        // Creiamo il repository localmente solo per passarlo al service
        TransactionRepository repo = new TransactionRepository(hikariDataSource);
        
        // Inietto il repository nel service
        this.transactionService = new TransactionService(repo);
        this.analyticsService = new DefaultAnalyticsService();
        //this.monthlyChartService = new MonthlyChartService();
        //this.pieChartService = new PieChartService();

        this.analyticsService = new DefaultAnalyticsService();
        this.exportExcel = new ExportExcel();
        this.exportCSV = new ExportCSV();
        this.validationService = new DefaultValidationService();

        this.transactionService.addListener(new TransactionListener() {
            @Override
            public void onDataChanged() {
                ricaricaDati();
                aggiornaPagina();
            }
        });

        // Uso il service per recuperare i dati
        Iterable<Transaction> savedTransactions = transactionService.findAll();
        for(Transaction savedTransaction: savedTransactions) {
            transactions.add(savedTransaction);
        }
        patrimonioLabel.setText(String.format("€ %.2f", transactionService.aggiornaPatrimonio(transactions)));
        aggiornaConteggioRisultati();
        aggiornaPagina();
    }

    @FXML
    public void initialize(){

        welcomeBack.setText("Bentornato\n" + LoginController.getUsername());

        // Placeholder per la tabella quando non ci sono dati
        tabella.setPlaceholder(new Label("Nessuna transazione trovata"));





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
            // USATO SERVICE INVECE DI REPO
            transactionService.save(selectedTransaction);
        });

        /*COLONNA DESCRIZIONE*/
        descrizioneCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescrizione()));
        descrizioneCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descrizioneCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setDescrizione(event.getNewValue());
            // USATO SERVICE INVECE DI REPO
            transactionService.save(selectedTransaction);
        });

        /*COLONNA TIPO*/
        tipoCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipo()));
        tipoCol.setOnEditCommit(event -> {
            Transaction selectedTransaction = event.getRowValue();
            selectedTransaction.setTipo(event.getNewValue());
            // USATO SERVICE INVECE DI REPO
            transactionService.save(selectedTransaction);
        });

        /*COLONNA DATA*/
        dataCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData().toString()));
        dataCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dataCol.setOnEditCommit(event -> {
            Transaction transazione = event.getRowValue();
            try {
                LocalDate nuovaData = validationService.parseDate(event.getNewValue());
                transazione.setData(nuovaData);
                // USATO SERVICE INVECE DI REPO
                transactionService.save(transazione);
            } catch (IllegalArgumentException e) {
                mostraErrore(e.getMessage());
            }
        });

        /*COLONNA IMPORTO*/
        importoCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getImporto()).asObject());
        importoCol.setOnEditCommit(event -> {
            try {
                validationService.validateAmount(event.getNewValue().toString());
                Transaction selectedTransaction = event.getRowValue();
                double newValue = Double.parseDouble(event.getNewValue().toString());
                selectedTransaction.setImporto(newValue);
                // USATO SERVICE INVECE DI REPO
                transactionService.save(selectedTransaction);
            } catch (IllegalArgumentException e) {
                mostraErrore(e.getMessage());
            }
        });


        pieChartCategorie.setTitle("Analisi Spese");
        BarChartMensile.setTitle("Entrate e Spese Mensili");


        filteredData = new FilteredList<>(transactions, t -> true); //predicato presente in aggiornaFiltroGlobale()
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            aggiornaFiltroGlobale(); // deve rispettare il filtro globale
        });
        tabella.setItems(filteredData);
        tabella.setEditable(true);

        // Button clear search handler visibility
        if (clearSearchButton != null) {
            clearSearchButton.disableProperty().bind(searchField.textProperty().isEmpty());
        }

        // Disabilita il pulsante Aggiungi finché i campi non sono validi
        if (aggiungiButton != null) {
            aggiungiButton.disableProperty().bind(importoField.textProperty().isEmpty()
                    .or(dataField.valueProperty().isNull())
                    .or(tipoChoiceBox.valueProperty().isNull())
                    .or(tipoChoiceBox.valueProperty().isEqualTo("Uscita").and(
                            categoriaChoiceBox.valueProperty().isNull()
                                    .or(categoriaChoiceBox.valueProperty().isEqualTo("Categoria"))
                    )));
        }

        // Formattazione valuta per la colonna importo (solo visualizzazione)
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.ITALY);
        importoCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : currency.format(value));
            }
        });




        yearSelection.setValue("Storico");
        yearSelection.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(!Objects.equals(oldVal,newVal)) {
                aggiornaFiltroGlobale(); //per poter utilizzare anche il filtro testuale
                aggiornaPagina(); //aggiorna tabella e grafici secondo il nuovo filtro
            }
        });


    }

    private void aggiornaFiltroGlobale() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String annoSelezionato = yearSelection.getValue();

        filteredData.setPredicate(t -> {
            boolean matchAnno = annoSelezionato == null || annoSelezionato.equals("Storico")
                    || String.valueOf(t.getData().getYear()).equals(annoSelezionato);

            boolean matchRicerca = searchText.isEmpty()
                    || (t.getDescrizione() != null && t.getDescrizione().toLowerCase().contains(searchText))
                    || (t.getCategoria() != null && t.getCategoria().toLowerCase().contains(searchText))
                    || (t.getTipo() != null && t.getTipo().toLowerCase().contains(searchText))
                    || (t.getData() != null && t.getData().toString().contains(searchText));

            return matchAnno && matchRicerca; //devo assicurarmi che sia l'anno, sia il testo cercato vengano soddisfatti
        });
        aggiornaConteggioRisultati();
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
            validationService.validateAmount(importoField.getText());
            importo = Double.parseDouble(importoField.getText());
        } catch (IllegalArgumentException e) {
            mostraErrore(e.getMessage());
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
            // USATO SERVICE INVECE DI REPO
            transactionService.save(transazione);
            System.out.println("Transazione salvata con ID: " + transazione.getId()); //stampa di debug


            //transactions.add(transazione);

            ricaricaDati();
            descrizioneField.clear();
            importoField.clear();
            //patrimonioLabel.setText(String.format("€ %.2f", transactionService.aggiornaPatrimonio(transactions)));
            //aggiornaPagina();
        } catch (Exception e) {
            mostraErrore("Errore durante il salvataggio: " + e.getMessage());
        }


    }
    private void ricaricaDati() {
        transactions.clear();
        Iterable<Transaction> nuoveTransazioni = transactionService.findAll();
        for (Transaction t : nuoveTransazioni) {
            transactions.add(t);
        }

    }

    public void rimuoviTransazione() {
        Transaction selezionata = tabella.getSelectionModel().getSelectedItem();

        if (selezionata == null) {
            mostraErrore("Seleziona una transazione da rimuovere.");
            return;
        }

        try {
            // USATO SERVICE INVECE DI REPO
            transactionService.deleteById(selezionata.getId());
            transactions.remove(selezionata);
            patrimonioLabel.setText(String.format("€ %.2f", transactionService.aggiornaPatrimonio(transactions)));
            aggiornaPagina();
        } catch (Exception e) {
            mostraErrore("Errore nella rimozione della transazione: " + e.getMessage());
        }
    }

    @FXML
    private void clearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
    }

    private void aggiornaConteggioRisultati() {
        if (resultsCountLabel != null && filteredData != null) {
            int n = filteredData.size();
            int m = transactions.size();
            resultsCountLabel.setText("Risultati: " + n + "/" + m);
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
            try {
                exportExcel.export(file, new java.util.ArrayList<>(tabella.getItems()));
            } catch (IOException e) {
                mostraErrore("Errore durante l'esportazione: " + e.getMessage());
            }
        }
    }

    public void esportaCSV(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva come CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                exportCSV.export(file, new java.util.ArrayList<>(tabella.getItems()));
            } catch (IOException e) {
                mostraErrore("Errore durante l'esportazione: " + e.getMessage());
            }
        }



    }
    /* --- NUOVI METODI PER GESTIRE I GRAFICI NEL CONTROLLER --- */

    private void aggiornaGraficoTorta() {
        // 1. Chiediamo i numeri al Service (Logica pura)
        Map<String, Double> dati = analyticsService.getSpesePerCategoria(filteredData);

        // 2. Prepariamo la lista per JavaFX (Grafica)
        ObservableList<PieChart.Data> datiGrafico = FXCollections.observableArrayList();
        double totaleUscite = 0;

        // Convertiamo la mappa in dati per il grafico
        for (Map.Entry<String, Double> entry : dati.entrySet()) {
            String categoria = entry.getKey();
            Double importo = entry.getValue();
            datiGrafico.add(new PieChart.Data(categoria, importo));
            totaleUscite += importo;
        }

        // 3. Aggiungiamo le percentuali alle etichette (Opzionale, ma carino)
        for (PieChart.Data d : datiGrafico) {
            double percentuale = (d.getPieValue() / totaleUscite) * 100;
            d.setName(String.format("%s (%.1f%%)", d.getName(), percentuale));
        }

        // 4. Aggiorniamo il componente a schermo
        pieChartCategorie.setData(datiGrafico);
    }

    private void aggiornaGraficoBarre() {
        // 1. Chiediamo i numeri al Service
        Map<Integer, Double> entrate = analyticsService.getEntrateMensili(filteredData);
        Map<Integer, Double> uscite = analyticsService.getUsciteMensili(filteredData);

        String[] nomiMesi = {"Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"};

        // 2. Creiamo le due serie (Entrate e Uscite)
        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");

        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        // 3. Riempiamo le serie ciclando i 12 mesi
        for (int i = 1; i <= 12; i++) {
            String nomeMese = nomiMesi[i-1];
            // Nota: entrate.get(i) restituisce il valore o 0.0 (gestito dal service)
            serieEntrate.getData().add(new XYChart.Data<>(nomeMese, entrate.get(i)));
            serieUscite.getData().add(new XYChart.Data<>(nomeMese, uscite.get(i)));
        }

        // 4. Puliamo e aggiorniamo il grafico a barre
        BarChartMensile.getData().clear();
        BarChartMensile.getData().addAll(serieEntrate, serieUscite);
    }

    public void aggiornaPagina(){
        System.out.println("Aggiorna pagina"); //stampa di debug
        double patrimonio = transactionService.aggiornaPatrimonio(transactions);
        patrimonioLabel.setText(String.format("€ %.2f", patrimonio));

        double entrate = transactionService.aggiornaTotale("Entrata", filteredData);
        double uscite = transactionService.aggiornaTotale("Uscita", filteredData);

        entrateTotaliLabel.setText(String.format("Entrate Totali: € %.2f", entrate ));
        usciteTotaliLabel.setText(String.format("Uscite Totali: € %.2f", uscite));
        saldoLabel.setText(String.format("Saldo: € %.2f",transactionService.aggiornaTotale("Entrata", filteredData) + transactionService.aggiornaTotale("Uscita", filteredData )));

        // Aggiorna grafici solo se ci sono dati filtrati
        if (filteredData != null && !filteredData.isEmpty()) {
            aggiornaGraficoTorta();
            aggiornaGraficoBarre();
        } else {
            pieChartCategorie.getData().clear();
            BarChartMensile.getData().clear();
        }
        aggiornaOpzioniAnni();
        aggiornaConteggioRisultati();
    }


}
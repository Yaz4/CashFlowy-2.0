package CashFlowy.service;

import CashFlowy.persistence.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartService {

    public void aggiornaGraficoCategorie(PieChart pieChartCategorie, List<Transaction> transactions) {
        Map<String, Double> spesePerCategoria = transactions.stream()
                .filter(t -> "Uscita".equals(t.getTipo()))
                .collect(Collectors.groupingBy(Transaction::getCategoria, Collectors.summingDouble(Transaction::getImporto)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : spesePerCategoria.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), -entry.getValue()));
        }
        pieChartCategorie.setData(pieChartData);

        double total = pieChartData.stream()
                .mapToDouble(PieChart.Data::getPieValue).sum();

        for (PieChart.Data data : pieChartCategorie.getData()) {
            double percentage = (data.getPieValue() / total) * 100;
            data.setName(String.format("%s (%.1f%%)", data.getName().split(" ")[0], percentage));
        }
    }

    public void aggiornaGraficoMensile(BarChart<String, Number> chartMensile, List<Transaction> transactions) {
        String[] mesi = {"Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"};

        Map<Integer, Double> entrate = new HashMap<>();
        Map<Integer, Double> uscite = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            entrate.put(i, 0.0);
            uscite.put(i, 0.0);
        }

        for (Transaction t : transactions) {

            int mese = t.getData().getMonthValue();
            if ("Entrata".equals(t.getTipo())) {
                entrate.put(mese, entrate.get(mese) + t.getImporto());
            } else if ("Uscita".equals(t.getTipo())) {
                uscite.put(mese, uscite.get(mese) + (-t.getImporto()));
            }
        }

        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");
        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        for (int i = 1; i <= 12; i++) {
            serieEntrate.getData().add(new XYChart.Data<>(mesi[i - 1], entrate.get(i)));
            serieUscite.getData().add(new XYChart.Data<>(mesi[i - 1], uscite.get(i)));
        }

        chartMensile.getData().clear();
        chartMensile.getData().addAll(serieEntrate, serieUscite);
    }
}

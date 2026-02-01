package CashFlowy.service.charts;

import CashFlowy.persistence.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PieChartService implements ChartService<PieChart>{

    public void aggiornaGrafico(PieChart pieChartCategorie, List<Transaction> transactions) {
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
}

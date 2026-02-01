package CashFlowy.service.charts;

import CashFlowy.persistence.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ChartService<T extends Chart> {

    public void aggiornaGrafico(T chart, List<Transaction> transactions);
}

package CashFlowy.service.charts;

import CashFlowy.persistence.model.Transaction;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyChartService implements ChartService<BarChart<String, Number>>{
    public void aggiornaGrafico(BarChart<String, Number> chartMensile, List<Transaction> transactions) {
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

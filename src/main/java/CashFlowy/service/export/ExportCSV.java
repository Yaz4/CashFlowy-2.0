package CashFlowy.service.export;

import CashFlowy.persistence.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExportCSV implements ExportService {

    @Override
    public void export(File file, List<Transaction> transactions) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            // Scrittura dell'intestazione del CSV
            writer.println("Id,Categoria,Descrizione,Importo,Data,Tipo");

            // Scrittura delle transazioni
            for (Transaction transaction : transactions) {
                writer.println(
                        transaction.getId() + "," +
                                transaction.getCategoria() + "," +
                                transaction.getDescrizione() + "," +
                                transaction.getImporto() + "," +
                                transaction.getData() + "," +
                                transaction.getTipo()
                );
            }
        }
    }
}

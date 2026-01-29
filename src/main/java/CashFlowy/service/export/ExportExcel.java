package CashFlowy.service.export;

import CashFlowy.persistence.model.Transaction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportExcel implements ExportService{
    public void export(File file, List<Transaction> transactions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transazioni");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Categoria");
            header.createCell(1).setCellValue("Descrizione");
            header.createCell(2).setCellValue("Tipo");
            header.createCell(3).setCellValue("Data");
            header.createCell(4).setCellValue("Importo");

            // Rows
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(t.getCategoria());
                row.createCell(1).setCellValue(t.getDescrizione());
                row.createCell(2).setCellValue(t.getTipo());
                row.createCell(3).setCellValue(t.getData() != null ? t.getData().toString() : "");
                row.createCell(4).setCellValue(t.getImporto());
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
        }
    }
}

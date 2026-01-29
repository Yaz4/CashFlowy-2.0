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

/**
 * ExportService: Interfaccia per esportare le transazioni*/
public interface ExportService {

    public void export(File file, List<Transaction> transactions) throws IOException;
}

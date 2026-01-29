package CashFlowy.service.validation;

import java.time.LocalDate;

/**
 * ValidationService: responsabilità singola di validare e fare parsing sicuro degli input.
 */
public class ValidationService {

    public void validateAmount(String amountStr) {
        try {
            Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Importo non valido. Inserire un valore numerico");
        }
    }

    public LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato data non valido. Usa AAAA-MM-GG.");
        }
    }
}

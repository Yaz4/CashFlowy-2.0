package CashFlowy.service.validation;

import java.time.LocalDate;

/**
 * ValidationService: responsabilità singola di validare e fare parsing sicuro degli input.
 */
public interface ValidationService {

    public void validateAmount(String amountStr);

    public LocalDate parseDate(String dateStr);
}

package com.example.Documentation.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentDto {
    String companyName;
    BigDecimal costOfContract;
    Boolean isMoneyTransferred;
    Boolean isMoneySent;
    Boolean isMoneyAccepted;
    BigDecimal costInUSD;
    BigDecimal tax;
    BigDecimal summaAfterBankService;
    BigDecimal additionalExpenseInPercentage;
    BigDecimal moneyAfterExpenses;
    String latNumber;
    BigDecimal transportExpenses;
    BigDecimal profit;
    BigDecimal profitInPercentage;
    String productName;
    String phoneNumber;
    String hashIdOfContract;
    String hashIdOfInvoice;
    String date;
}

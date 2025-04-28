package com.example.Documentation.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Document extends AuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    String companyName;
    BigDecimal costOfContract;
    Boolean isMoneyTransferred;
    Boolean isMoneyAccepted;
    Boolean isMoneySent;
    BigDecimal costInUSD;
    BigDecimal exchangingRate;
    BigDecimal costInUZS;
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
    LocalDate date;

    @OneToOne
    Attachment contractAttachment;

    @OneToOne
    Attachment invoiceAttachment;

    @CreationTimestamp
    Date createdAt;

    @UpdateTimestamp
    Date updateAt;
}

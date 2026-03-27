package com.payment.service;

import com.payment.model.Pain001Message;
import com.payment.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class Pain001MessageBuilder {

    public Pain001Message buildPain001Message(Payment payment) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime creationDateTime = LocalDateTime.now();

        Pain001Message.InitiatingParty initiatingParty = Pain001Message.InitiatingParty.builder()
                .name(payment.getDebtorName())
                .id(payment.getDebtorAccount())
                .build();

        Pain001Message.PaymentTypeInfo paymentTypeInfo = Pain001Message.PaymentTypeInfo.builder()
                .instructionPriority(payment.getPriority() != null ? payment.getPriority() : "NORM")
                .serviceLevel("SEPA")
                .build();

        Pain001Message.Party debtor = Pain001Message.Party.builder()
                .name(payment.getDebtorName())
                .identification(payment.getDebtorAccount())
                .build();

        Pain001Message.Account debtorAccount = Pain001Message.Account.builder()
                .iban(payment.getDebtorAccount())
                .currency(payment.getCurrency())
                .build();

        Pain001Message.Party creditor = Pain001Message.Party.builder()
                .name(payment.getCreditorName())
                .identification(payment.getCreditorAccount())
                .build();

        Pain001Message.Account creditorAccount = Pain001Message.Account.builder()
                .iban(payment.getCreditorAccount())
                .currency(payment.getCurrency())
                .build();

        Pain001Message.Amount amount = Pain001Message.Amount.builder()
                .currency(payment.getCurrency())
                .value(payment.getAmount())
                .build();

        Pain001Message.RemittanceInfo remittanceInfo = Pain001Message.RemittanceInfo.builder()
                .unstructured(payment.getRemittanceInformation() != null ?
                        payment.getRemittanceInformation() : payment.getPaymentPurpose())
                .build();

        Pain001Message.CreditTransferTransaction creditTransfer =
                Pain001Message.CreditTransferTransaction.builder()
                .paymentId(payment.getPaymentId())
                .instructedAmount(amount)
                .creditor(creditor)
                .creditorAccount(creditorAccount)
                .remittanceInformation(remittanceInfo)
                .requestedExecutionDate(payment.getRequestedExecutionDate() != null ?
                        payment.getRequestedExecutionDate() :
                        LocalDateTime.now().toLocalDate().toString())
                .build();

        Pain001Message.PaymentInformation paymentInfo = Pain001Message.PaymentInformation.builder()
                .paymentInformationId(UUID.randomUUID().toString())
                .paymentMethod("TRF")
                .batchBooking(false)
                .numberOfTransactions(1)
                .controlSum(payment.getAmount())
                .paymentTypeInformation(paymentTypeInfo)
                .debtor(debtor)
                .debtorAccount(debtorAccount)
                .creditTransferTransaction(creditTransfer)
                .build();

        return Pain001Message.builder()
                .messageId(messageId)
                .creationDateTime(creationDateTime)
                .numberOfTransactions(1)
                .controlSum(payment.getAmount())
                .initiatingParty(initiatingParty)
                .paymentInformation(paymentInfo)
                .version("pain.001.003.09")
                .build();
    }
}

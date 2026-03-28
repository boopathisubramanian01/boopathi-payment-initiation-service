package com.payment.service;

import com.payment.model.Pain001Message;
import com.payment.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class Pain001MessageBuilder {

        private static final Logger log = LoggerFactory.getLogger(Pain001MessageBuilder.class);

    public Pain001Message buildPain001Message(Payment payment) {
        String messageId = UUID.randomUUID().toString();
        LocalDateTime creationDateTime = LocalDateTime.now();

        Pain001Message.InitiatingParty initiatingParty = new Pain001Message.InitiatingParty();
        initiatingParty.setName(payment.getDebtorName());
        initiatingParty.setId(payment.getDebtorAccount());

        Pain001Message.PaymentTypeInfo paymentTypeInfo = new Pain001Message.PaymentTypeInfo();
        paymentTypeInfo.setInstructionPriority(payment.getPriority() != null ? payment.getPriority() : "NORM");
        paymentTypeInfo.setServiceLevel("SEPA");

        Pain001Message.Party debtor = new Pain001Message.Party();
        debtor.setName(payment.getDebtorName());
        debtor.setIdentification(payment.getDebtorAccount());

        Pain001Message.Account debtorAccount = new Pain001Message.Account();
        debtorAccount.setIban(payment.getDebtorAccount());
        debtorAccount.setCurrency(payment.getCurrency());

        Pain001Message.Party creditor = new Pain001Message.Party();
        creditor.setName(payment.getCreditorName());
        creditor.setIdentification(payment.getCreditorAccount());

        Pain001Message.Account creditorAccount = new Pain001Message.Account();
        creditorAccount.setIban(payment.getCreditorAccount());
        creditorAccount.setCurrency(payment.getCurrency());

        Pain001Message.Amount amount = new Pain001Message.Amount();
        amount.setCurrency(payment.getCurrency());
        amount.setValue(payment.getAmount());

        Pain001Message.RemittanceInfo remittanceInfo = new Pain001Message.RemittanceInfo();
        remittanceInfo.setUnstructured(payment.getRemittanceInformation() != null
                ? payment.getRemittanceInformation() : payment.getPaymentPurpose());

        Pain001Message.CreditTransferTransaction creditTransfer = new Pain001Message.CreditTransferTransaction();
        creditTransfer.setPaymentId(payment.getPaymentId());
        creditTransfer.setInstructedAmount(amount);
        creditTransfer.setCreditor(creditor);
        creditTransfer.setCreditorAccount(creditorAccount);
        creditTransfer.setRemittanceInformation(remittanceInfo);
        creditTransfer.setRequestedExecutionDate(payment.getRequestedExecutionDate() != null
                ? payment.getRequestedExecutionDate() : LocalDateTime.now().toLocalDate().toString());

        Pain001Message.PaymentInformation paymentInfo = new Pain001Message.PaymentInformation();
        paymentInfo.setPaymentInformationId(UUID.randomUUID().toString());
        paymentInfo.setPaymentMethod("TRF");
        paymentInfo.setBatchBooking(false);
        paymentInfo.setNumberOfTransactions(1);
        paymentInfo.setControlSum(payment.getAmount());
        paymentInfo.setPaymentTypeInformation(paymentTypeInfo);
        paymentInfo.setDebtor(debtor);
        paymentInfo.setDebtorAccount(debtorAccount);
        paymentInfo.setDebtorRoutingNumber(payment.getDebtorRoutingNumber());
        paymentInfo.setCreditorRoutingNumber(payment.getCreditorRoutingNumber());
        paymentInfo.setCreditTransferTransaction(creditTransfer);

        Pain001Message message = new Pain001Message();
        message.setMessageId(messageId);
        message.setCreationDateTime(creationDateTime);
        message.setNumberOfTransactions(1);
        message.setControlSum(payment.getAmount());
        message.setInitiatingParty(initiatingParty);
        message.setPaymentInformation(paymentInfo);
        message.setVersion("pain.001.003.09");
        return message;
    }
}

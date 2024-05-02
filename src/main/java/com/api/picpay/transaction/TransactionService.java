package com.api.picpay.transaction;

import com.api.picpay.authorization.AuthorizerService;
import com.api.picpay.notification.NotificationService;
import com.api.picpay.wallet.Wallet;
import com.api.picpay.wallet.WalletRepository;
import com.api.picpay.wallet.WalletType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizerService authorizerService;
    private final NotificationService notificationService;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository,
            AuthorizerService authorizerService,
            NotificationService notificationService
            ) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizerService = authorizerService;
        this.notificationService = notificationService;
    }

    @Transactional
    public  Transaction create(Transaction transaction){
        // 1 - Validar
        validate(transaction);

        // 2 - criar transaç©ão
        var newTransaction = transactionRepository.save(transaction);

        // 3 -  debitar da carteira
        var walletPayer = walletRepository.findById(transaction.payer()).get();
        var walletPayee = walletRepository.findById(transaction.payee()).get();
        walletRepository.save(walletPayer.debit(transaction.value()));
        walletRepository.save(walletPayee.credit(transaction.value()));

        // 4 - chamar serviçcos externos
        //authorize transaction
        authorizerService.authorize(transaction);

        // notification
        notificationService.notify(transaction);

        return newTransaction;
    }

    private void validate(Transaction transaction){
        walletRepository.findById(transaction.payee())
                .map(payee -> walletRepository.findById(transaction.payer())
                    .map(payer -> isTransactionValid(transaction, payer) ? transaction : null)
                        .orElseThrow(() -> new InvalidTransactionException("Invalid transaction - %s".formatted(transaction))))
                .orElseThrow(() -> new InvalidTransactionException("Invalid transaction - %s".formatted(transaction)));
    }

    private static boolean isTransactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());
    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}

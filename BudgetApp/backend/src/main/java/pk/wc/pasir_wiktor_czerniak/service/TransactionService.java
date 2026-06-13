package pk.wc.pasir_wiktor_czerniak.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pk.wc.pasir_wiktor_czerniak.dto.BalanceDto;
import pk.wc.pasir_wiktor_czerniak.dto.TransactionDTO;
import pk.wc.pasir_wiktor_czerniak.model.Transaction;
import pk.wc.pasir_wiktor_czerniak.model.TransactionType;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.TransactionRepository;
import pk.wc.pasir_wiktor_czerniak.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = auth.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow();
    }

    public List<Transaction> getAllTransactions() {

        User user = getCurrentUser();

        return transactionRepository.findByUser(user);
    }

    public Transaction getTransactionById(Long id) {

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Nie znaleziono transakcji"
                                ));

        if (!transaction.getUser().getEmail()
                .equals(getCurrentUser().getEmail())) {

            throw new SecurityException(
                    "Brak dostepu do transakcji"
            );
        }

        return transaction;
    }

    public Transaction createTransaction(
            TransactionDTO dto) {

        Transaction transaction =
                new Transaction();

        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTags(dto.getTags());
        transaction.setNotes(dto.getNotes());
        transaction.setTimestamp(
                LocalDateTime.now()
        );

        transaction.setUser(
                getCurrentUser()
        );

        return transactionRepository
                .save(transaction);
    }

    public Transaction updateTransaction(
            Long id,
            TransactionDTO dto) {

        Transaction transaction =
                transactionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Nie znaleziono transakcji"
                                ));

        if (!transaction.getUser().getEmail()
                .equals(getCurrentUser().getEmail())) {

            throw new SecurityException(
                    "Brak dostepu do transakcji"
            );
        }

        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTags(dto.getTags());
        transaction.setNotes(dto.getNotes());

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {

        Transaction transaction =
                transactionRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Nie znaleziono transakcji"
                                ));

        if (!transaction.getUser().getEmail()
                .equals(getCurrentUser().getEmail())) {

            throw new SecurityException(
                    "Brak dostepu do transakcji"
            );
        }

        transactionRepository.delete(transaction);
    }

    public BalanceDto getUserBalance(
            Double days
    ) {

        User user = getCurrentUser();

        List<Transaction> transactions;

        if (days == null) {

            transactions =
                    transactionRepository.findByUser(user);

        } else {

            LocalDateTime fromDate =
                    LocalDateTime.now()
                            .minusSeconds(
                                    (long) (days * 24 * 60 * 60)
                            );

            transactions =
                    transactionRepository
                            .findByUserAndTimestampAfter(
                                    user,
                                    fromDate
                            );
        }

        double totalIncome =
                transactions.stream()
                        .filter(t ->
                                t.getType()
                                        == TransactionType.INCOME)
                        .mapToDouble(
                                Transaction::getAmount
                        )
                        .sum();

        double totalExpense =
                transactions.stream()
                        .filter(t ->
                                t.getType()
                                        == TransactionType.EXPENSE)
                        .mapToDouble(
                                Transaction::getAmount
                        )
                        .sum();

        return new BalanceDto(
                totalIncome,
                totalExpense,
                totalIncome - totalExpense
        );
    }
}
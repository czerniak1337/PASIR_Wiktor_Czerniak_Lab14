package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pk.wc.pasir_wiktor_czerniak.dto.BalanceDto;
import pk.wc.pasir_wiktor_czerniak.dto.TransactionDTO;
import pk.wc.pasir_wiktor_czerniak.model.Transaction;
import pk.wc.pasir_wiktor_czerniak.model.TransactionType;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.TransactionRepository;
import pk.wc.pasir_wiktor_czerniak.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final String TRANSACTION_NOT_FOUND =
            "Nie znaleziono transakcji";
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private static final Clock UTC_CLOCK = Clock.systemUTC();

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Użytkownik nie jest zalogowany");
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie znaleziony"));
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findByUser(getCurrentUser());
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(TRANSACTION_NOT_FOUND));

        validateOwnership(transaction);
        return transaction;
    }

    @Transactional
    public Transaction createTransaction(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        updateTransactionFields(transaction, dto);
        transaction.setTimestamp(LocalDateTime.now(UTC_CLOCK));
        transaction.setUser(getCurrentUser());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, TransactionDTO dto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(TRANSACTION_NOT_FOUND));

        validateOwnership(transaction);
        updateTransactionFields(transaction, dto);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(TRANSACTION_NOT_FOUND));

        validateOwnership(transaction);
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public BalanceDto getUserBalance(Double days) {
        User user = getCurrentUser();
        List<Transaction> transactions = (days == null)
                ? transactionRepository.findByUser(user)
                : transactionRepository.findByUserAndTimestampAfter(
                user, LocalDateTime.now(UTC_CLOCK).minusSeconds((long) (days * 24 * 60 * 60)));

        double totalIncome = calculateTotal(transactions, TransactionType.INCOME);
        double totalExpense = calculateTotal(transactions, TransactionType.EXPENSE);

        return new BalanceDto(totalIncome, totalExpense, totalIncome - totalExpense);
    }

    private void validateOwnership(Transaction transaction) {
        if (!transaction.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new SecurityException("Brak dostępu do transakcji");
        }
    }

    private void updateTransactionFields(Transaction transaction, TransactionDTO dto) {
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTags(dto.getTags());
        transaction.setNotes(dto.getNotes());
    }

    private double calculateTotal(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}
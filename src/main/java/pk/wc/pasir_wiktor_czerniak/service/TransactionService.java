package pk.wc.PASIR_Wiktor_Czerniak.service;

import org.springframework.stereotype.Service;
import pk.wc.PASIR_Wiktor_Czerniak.dto.TransactionDTO;
import pk.wc.PASIR_Wiktor_Czerniak.model.Transaction;
import pk.wc.PASIR_Wiktor_Czerniak.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(
            TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {

        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Nie znaleziono transakcji"));
    }

    public Transaction createTransaction(TransactionDTO dto) {

        Transaction transaction = new Transaction();

        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTags(dto.getTags());
        transaction.setNotes(dto.getNotes());
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id,
                                         TransactionDTO dto) {

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Nie znaleziono transakcji"));

        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTags(dto.getTags());
        transaction.setNotes(dto.getNotes());

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Nie znaleziono transakcji"));

        transactionRepository.delete(transaction);
    }
}
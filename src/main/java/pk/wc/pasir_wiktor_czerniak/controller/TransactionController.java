package pk.wc.PASIR_Wiktor_Czerniak.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pk.wc.PASIR_Wiktor_Czerniak.dto.TransactionDTO;
import pk.wc.PASIR_Wiktor_Czerniak.model.Transaction;
import pk.wc.PASIR_Wiktor_Czerniak.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>>
    getAllTransactions() {

        return ResponseEntity.ok(
                transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction>
    getTransactionById(@PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(id));
    }

    @PostMapping
    public ResponseEntity<Transaction>
    createTransaction(
            @Valid @RequestBody TransactionDTO dto) {

        return ResponseEntity.ok(
                transactionService.createTransaction(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction>
    updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto) {

        return ResponseEntity.ok(
                transactionService.updateTransaction(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteTransaction(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }
}
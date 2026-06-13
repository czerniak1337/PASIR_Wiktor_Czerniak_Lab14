package pk.wc.pasir_wiktor_czerniak.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pk.wc.pasir_wiktor_czerniak.dto.BalanceDto;
import pk.wc.pasir_wiktor_czerniak.dto.TransactionDTO;
import pk.wc.pasir_wiktor_czerniak.model.Transaction;
import pk.wc.pasir_wiktor_czerniak.service.TransactionService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TransactionGraphQLController {

    private final TransactionService transactionService;

    @QueryMapping
    public List<Transaction> transactions() {
        return transactionService.getAllTransactions();
    }

    @QueryMapping
    public Transaction transaction(
            @Argument Long id
    ) {
        return transactionService.getTransactionById(id);
    }

    @QueryMapping
    public BalanceDto userBalance(
            @Argument Double days
    ) {
        return transactionService.getUserBalance(days);
    }

    @MutationMapping
    public Transaction addTransaction(
            @Argument @Valid TransactionDTO transactionDTO
    ) {
        return transactionService.createTransaction(transactionDTO);
    }

    @MutationMapping
    public Transaction updateTransaction(
            @Argument Long id,
            @Argument @Valid TransactionDTO transactionDTO
    ) {
        return transactionService.updateTransaction(id, transactionDTO);
    }

    @MutationMapping
    public Boolean deleteTransaction(
            @Argument Long id
    ) {
        transactionService.deleteTransaction(id);
        return true;
    }
}
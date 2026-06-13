package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pk.wc.pasir_wiktor_czerniak.dto.DebtDTO;
import pk.wc.pasir_wiktor_czerniak.dto.GroupTransactionDTO;
import pk.wc.pasir_wiktor_czerniak.model.Debt;
import pk.wc.pasir_wiktor_czerniak.model.Group;
import pk.wc.pasir_wiktor_czerniak.model.Membership;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.*;
import pk.wc.pasir_wiktor_czerniak.dto.GroupNotificationDto;
import pk.wc.pasir_wiktor_czerniak.model.Transaction;
import pk.wc.pasir_wiktor_czerniak.model.TransactionType;

import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final CurrentUserService currentUserService;
    private final MembershipRepository membershipRepository;
    private final GroupNotificationService groupNotificationService;
    private final TransactionRepository transactionRepository;

    public List<Debt> getGroupDebts(
            Long groupId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        Membership membership =
                membershipRepository
                        .findByGroup_Id(groupId)
                        .stream()
                        .filter(m ->
                                m.getUser()
                                        .getId()
                                        .equals(currentUser.getId())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Brak dostępu do grupy"
                                )
                        );

        return debtRepository
                .findByGroupId(groupId)
                .stream()
                .filter(debt ->
                        !debt.getCreatedAt()
                                .isBefore(
                                        membership.getJoinedAt()
                                )
                )
                .toList();
    }

    public Debt createDebt(
            DebtDTO dto
    ) {

        User debtor =
                userRepository.findById(
                        dto.getDebtorId()
                ).orElseThrow();

        User creditor =
                userRepository.findById(
                        dto.getCreditorId()
                ).orElseThrow();

        Group group =
                groupRepository.findById(
                        dto.getGroupId()
                ).orElseThrow();

        if (group.getOwner() == null) {
            throw new RuntimeException(
                    "Grupa nie ma właściciela"
            );
        }

        User currentUser =
                currentUserService.getCurrentUser();

        boolean isOwner =
                group.getOwner()
                        .getId()
                        .equals(
                                currentUser.getId()
                        );

        boolean isParticipant =
                debtor.getId().equals(
                        currentUser.getId()
                )
                        ||
                        creditor.getId().equals(
                                currentUser.getId()
                        );

        if (
                !isOwner
                        &&
                        !isParticipant
        ) {
            throw new RuntimeException(
                    "Mozesz tworzyc tylko wlasne dlugi"
            );
        }

        if (
                debtor.getId().equals(
                        creditor.getId()
                )
        ) {
            throw new RuntimeException(
                    "Nie mozna utworzyc dlugu do samego siebie"
            );
        }

        if (
                !membershipRepository
                        .existsByGroup_IdAndUser_Id(
                                group.getId(),
                                debtor.getId()
                        )
        ) {
            throw new RuntimeException(
                    "Dluznik nie nalezy do grupy"
            );
        }

        if (
                !membershipRepository
                        .existsByGroup_IdAndUser_Id(
                                group.getId(),
                                creditor.getId()
                        )
        ) {
            throw new RuntimeException(
                    "Wierzyciel nie nalezy do grupy"
            );
        }

        Debt debt = new Debt();

        debt.setAmount(
                dto.getAmount()
        );

        debt.setTitle(
                dto.getTitle()
        );

        debt.setDebtor(
                debtor
        );

        debt.setCreditor(
                creditor
        );

        debt.setGroup(
                group
        );

        return debtRepository.save(
                debt
        );
    }

    public Debt addGroupTransaction(
            GroupTransactionDTO dto
    ) {

        Group group =
                groupRepository.findById(
                        dto.getGroupId()
                ).orElseThrow();

        User currentUser =
                currentUserService.getCurrentUser();

        Transaction transaction = new Transaction();

        transaction.setUser(currentUser);
        transaction.setAmount(dto.getAmount());

        transaction.setType(
                "EXPENSE".equals(dto.getType())
                        ? TransactionType.EXPENSE
                        : TransactionType.INCOME
        );

        transaction.setTimestamp(LocalDateTime.now());
        transaction.setNotes("Transakcja grupowa: " + dto.getTitle());

        transactionRepository.save(transaction);

        List<Membership> members =
                membershipRepository.findByGroup_Id(
                        group.getId()
                );

        List<Membership> selectedMembers =
                selectParticipants(
                        dto,
                        members,
                        currentUser
                );

        if (selectedMembers.isEmpty()) {
            throw new RuntimeException(
                    "Grupa nie ma czlonkow"
            );
        }

        double amountPerUser =
                dto.getAmount()
                        / selectedMembers.size();

        boolean expense =
                "EXPENSE".equals(
                        dto.getType()
                );

        Debt firstDebt = null;

        for (Membership membership : selectedMembers) {

            User member =
                    membership.getUser();

            if (
                    member.getId().equals(
                            currentUser.getId()
                    )
            ) {
                continue;
            }

            Debt debt = new Debt();

            debt.setTitle(
                    dto.getTitle()
            );

            debt.setAmount(
                    amountPerUser
            );

            debt.setGroup(
                    group
            );

            debt.setDebtor(
                    expense
                            ? member
                            : currentUser
            );

            debt.setCreditor(
                    expense
                            ? currentUser
                            : member
            );

            Debt savedDebt =
                    debtRepository.save(
                            debt
                    );

            GroupNotificationDto notification =
                    new GroupNotificationDto(
                            "GROUP_EXPENSE_ADDED",
                            group.getId(),
                            group.getName(),
                            dto.getTitle(),
                            dto.getAmount(),
                            amountPerUser,
                            currentUser.getEmail(),
                            currentUser.getEmail()
                                    + " dodał wydatek "
                                    + dto.getTitle()
                    );

            groupNotificationService.sendToUser(
                    member.getEmail(),
                    notification
            );
            if (firstDebt == null) {
                firstDebt = savedDebt;
            }
        }

        return firstDebt;
    }

    public void deleteDebt(
            Long debtId
    ) {

        Debt debt =
                debtRepository.findById(
                        debtId
                ).orElseThrow();

        User currentUser =
                currentUserService.getCurrentUser();

        boolean isOwner =
                debt.getGroup()
                        .getOwner()
                        .getId()
                        .equals(
                                currentUser.getId()
                        );

        boolean isDebtor =
                debt.getDebtor()
                        .getId()
                        .equals(
                                currentUser.getId()
                        );

        boolean isCreditor =
                debt.getCreditor()
                        .getId()
                        .equals(
                                currentUser.getId()
                        );

        if (
                !isOwner
                        && !isDebtor
                        && !isCreditor
        ) {
            throw new RuntimeException(
                    "Brak uprawnien do usuniecia dlugu"
            );
        }

        debtRepository.delete(
                debt
        );
    }

    private List<Membership> selectParticipants(
            GroupTransactionDTO dto,
            List<Membership> members,
            User currentUser
    ) {

        List<Long> selectedUserIds =
                dto.getSelectedUserIds();

        if (
                selectedUserIds == null
                        || selectedUserIds.isEmpty()
        ) {
            return members;
        }

        Set<Long> ids =
                new HashSet<>(selectedUserIds);

        List<Membership> selected =
                members.stream()
                        .filter(m ->
                                ids.contains(
                                        m.getUser().getId()
                                )
                        )
                        .toList();

        if (selected.size() != ids.size()) {
            throw new RuntimeException(
                    "Wszyscy wybrani uzytkownicy musza nalezec do grupy"
            );
        }

        boolean currentUserSelected =
                selected.stream()
                        .anyMatch(m ->
                                m.getUser()
                                        .getId()
                                        .equals(
                                                currentUser.getId()
                                        )
                        );

        if (!currentUserSelected) {
            throw new RuntimeException(
                    "Aktualny uzytkownik musi byc uczestnikiem"
            );
        }

        if (selected.size() < 2) {
            throw new RuntimeException(
                    "Transakcja wymaga minimum 2 uczestnikow"
            );
        }

        return selected;
    }

    private Debt getDebtForCurrentGroupMember(
            Long debtId
    ) {

        Debt debt =
                debtRepository.findById(
                        debtId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Nie znaleziono dlugu"
                        )
                );

        User currentUser =
                currentUserService.getCurrentUser();

        boolean member =
                membershipRepository
                        .existsByGroup_IdAndUser_Id(
                                debt.getGroup().getId(),
                                currentUser.getId()
                        );

        if (!member) {
            throw new RuntimeException(
                    "Brak dostepu do grupy"
            );
        }

        return debt;
    }

    public Debt markDebtAsPaid(
            Long debtId
    ) {

        Debt debt =
                getDebtForCurrentGroupMember(
                        debtId
                );

        User currentUser =
                currentUserService.getCurrentUser();

        if (
                !debt.getDebtor()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )
        ) {
            throw new RuntimeException(
                    "Tylko dluznik moze oznaczyc dlug jako oplacony"
            );
        }

        debt.setPaidByDebtor(true);

        debt.setConfirmedByCreditor(false);

        return debtRepository.save(
                debt
        );
    }

    public Debt confirmDebtPayment(
            Long debtId
    ) {

        Debt debt =
                getDebtForCurrentGroupMember(
                        debtId
                );

        User currentUser =
                currentUserService.getCurrentUser();

        if (
                !debt.getCreditor()
                        .getId()
                        .equals(
                                currentUser.getId()
                        )
        ) {
            throw new RuntimeException(
                    "Tylko wierzyciel moze potwierdzic splate"
            );
        }

        if (!debt.isPaidByDebtor()) {
            throw new RuntimeException(
                    "Dlug musi zostac oznaczony jako oplacony"
            );
        }

        debt.setConfirmedByCreditor(true);

        Transaction transaction = new Transaction();

        transaction.setUser(currentUser);
        transaction.setAmount(debt.getAmount());
        transaction.setType(TransactionType.INCOME);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setNotes(
                "Spłata długu: " + debt.getTitle()
        );

        transactionRepository.save(transaction);

        return debtRepository.save(
                debt
        );
    }
}
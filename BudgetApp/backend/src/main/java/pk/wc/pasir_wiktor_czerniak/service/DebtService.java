package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pk.wc.pasir_wiktor_czerniak.dto.*;
import pk.wc.pasir_wiktor_czerniak.model.*;
import pk.wc.pasir_wiktor_czerniak.repository.*;
import pk.wc.pasir_wiktor_czerniak.model.TransactionType;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final Clock UTC_CLOCK = Clock.systemUTC();

    @Transactional(readOnly = true)
    public List<Debt> getGroupDebts(Long groupId) {
        final User currentUser = currentUserService.getCurrentUser();

        final Membership membership = membershipRepository
                .findByGroup_Id(groupId)
                .stream()
                .filter(m -> m.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new SecurityException("Brak dostępu do grupy"));

        return debtRepository
                .findByGroupId(groupId)
                .stream()
                .filter(debt -> !debt.getCreatedAt().isBefore(membership.getJoinedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Debt createDebt(DebtDTO dto) {
        final User debtor = userRepository.findById(dto.getDebtorId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono dłużnika"));
        final User creditor = userRepository.findById(dto.getCreditorId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono wierzyciela"));
        final Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono grupy"));

        validateGroupOwner(group);

        final User currentUser = currentUserService.getCurrentUser();
        checkAuthorization(group, debtor, creditor, currentUser);
        validateSelfDebt(debtor, creditor);
        validateMemberships(group, debtor, creditor);

        Debt debt = new Debt();
        debt.setAmount(dto.getAmount());
        debt.setTitle(dto.getTitle());
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);
        debt.setGroup(group);

        return debtRepository.save(debt);
    }

    @Transactional
    public Debt addGroupTransaction(GroupTransactionDTO dto) {
        final Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono grupy"));
        final User currentUser = currentUserService.getCurrentUser();

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        transaction.setAmount(dto.getAmount());
        transaction.setType("EXPENSE".equals(dto.getType()) ? TransactionType.EXPENSE : TransactionType.INCOME);
        transaction.setTimestamp(LocalDateTime.now(UTC_CLOCK));
        transaction.setNotes("Transakcja grupowa: " + dto.getTitle());

        transactionRepository.save(transaction);

        final List<Membership> members = membershipRepository.findByGroup_Id(group.getId());
        final List<Membership> selectedMembers = selectParticipants(dto, members, currentUser);

        if (selectedMembers.isEmpty()) {
            throw new IllegalStateException("Grupa nie ma członków");
        }

        final double amountPerUser = dto.getAmount() / selectedMembers.size();
        final boolean expense = "EXPENSE".equals(dto.getType());

        Debt firstDebt = null;

        for (Membership membership : selectedMembers) {
            User member = membership.getUser();
            if (member.getId().equals(currentUser.getId())) continue;

            Debt debt = new Debt();
            debt.setTitle(dto.getTitle());
            debt.setAmount(amountPerUser);
            debt.setGroup(group);
            debt.setDebtor(expense ? member : currentUser);
            debt.setCreditor(expense ? currentUser : member);

            Debt savedDebt = debtRepository.save(debt);

            GroupNotificationDto notification = new GroupNotificationDto(
                    "GROUP_EXPENSE_ADDED", group.getId(), group.getName(), dto.getTitle(),
                    dto.getAmount(), amountPerUser, currentUser.getEmail(),
                    currentUser.getEmail() + " dodał wydatek " + dto.getTitle()
            );

            groupNotificationService.sendToUser(member.getEmail(), notification);
            if (firstDebt == null) firstDebt = savedDebt;
        }

        return firstDebt;
    }

    @Transactional
    public void deleteDebt(Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono długu"));
        final User currentUser = currentUserService.getCurrentUser();

        boolean isOwner = debt.getGroup().getOwner().getId().equals(currentUser.getId());
        boolean isDebtor = debt.getDebtor().getId().equals(currentUser.getId());
        boolean isCreditor = debt.getCreditor().getId().equals(currentUser.getId());

        if (!isOwner && !isDebtor && !isCreditor) {
            throw new SecurityException("Brak uprawnień do usunięcia długu");
        }
        debtRepository.delete(debt);
    }

    @Transactional
    public Debt markDebtAsPaid(Long debtId) {
        Debt debt = getDebtForCurrentGroupMember(debtId);
        User currentUser = currentUserService.getCurrentUser();

        if (!debt.getDebtor().getId().equals(currentUser.getId())) {
            throw new SecurityException("Tylko dłużnik może oznaczyć dług jako opłacony");
        }

        debt.setPaidByDebtor(true);
        debt.setConfirmedByCreditor(false);
        return debtRepository.save(debt);
    }

    @Transactional
    public Debt confirmDebtPayment(Long debtId) {
        Debt debt = getDebtForCurrentGroupMember(debtId);
        User currentUser = currentUserService.getCurrentUser();

        if (!debt.getCreditor().getId().equals(currentUser.getId())) {
            throw new SecurityException("Tylko wierzyciel może potwierdzić spłatę");
        }

        if (!debt.isPaidByDebtor()) {
            throw new IllegalStateException("Dług musi zostać oznaczony jako opłacony przez dłużnika");
        }

        debt.setConfirmedByCreditor(true);

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        transaction.setAmount(debt.getAmount());
        transaction.setType(TransactionType.INCOME);
        transaction.setTimestamp(LocalDateTime.now(UTC_CLOCK));
        transaction.setNotes("Spłata długu: " + debt.getTitle());

        transactionRepository.save(transaction);
        return debtRepository.save(debt);
    }


    private void validateGroupOwner(Group group) {
        if (group.getOwner() == null) throw new IllegalStateException("Grupa nie ma właściciela");
    }

    private void validateSelfDebt(User debtor, User creditor) {
        if (debtor.getId().equals(creditor.getId()))
            throw new IllegalArgumentException("Nie można utworzyć długu do samego siebie");
    }

    private void validateMemberships(Group group, User debtor, User creditor) {
        if (!membershipRepository.existsByGroup_IdAndUser_Id(group.getId(), debtor.getId()))
            throw new IllegalArgumentException("Dłużnik nie należy do grupy");
        if (!membershipRepository.existsByGroup_IdAndUser_Id(group.getId(), creditor.getId()))
            throw new IllegalArgumentException("Wierzyciel nie należy do grupy");
    }

    private void checkAuthorization(Group group, User debtor, User creditor, User currentUser) {
        boolean isOwner = group.getOwner().getId().equals(currentUser.getId());
        boolean isParticipant = debtor.getId().equals(currentUser.getId()) || creditor.getId().equals(currentUser.getId());

        if (!isOwner && !isParticipant)
            throw new SecurityException("Możesz tworzyć tylko własne długi");
    }

    private List<Membership> selectParticipants(GroupTransactionDTO dto, List<Membership> members, User currentUser) {
        List<Long> selectedUserIds = dto.getSelectedUserIds();
        if (selectedUserIds == null || selectedUserIds.isEmpty()) return members;

        Set<Long> ids = new HashSet<>(selectedUserIds);
        List<Membership> selected = members.stream()
                .filter(m -> ids.contains(m.getUser().getId()))
                .collect(Collectors.toList());

        if (selected.size() != ids.size()) throw new IllegalArgumentException("Wszyscy wybrani użytkownicy muszą należeć do grupy");
        if (selected.stream().noneMatch(m -> m.getUser().getId().equals(currentUser.getId())))
            throw new IllegalStateException("Aktualny użytkownik musi być uczestnikiem");
        if (selected.size() < 2) throw new IllegalArgumentException("Transakcja wymaga minimum 2 uczestników");

        return selected;
    }

    private Debt getDebtForCurrentGroupMember(Long debtId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono długu"));
        User currentUser = currentUserService.getCurrentUser();

        if (!membershipRepository.existsByGroup_IdAndUser_Id(debt.getGroup().getId(), currentUser.getId())) {
            throw new SecurityException("Brak dostępu do grupy");
        }
        return debt;
    }
}
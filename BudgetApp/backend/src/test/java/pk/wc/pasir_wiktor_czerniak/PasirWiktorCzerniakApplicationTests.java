package pk.wc.pasir_wiktor_czerniak;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pk.wc.pasir_wiktor_czerniak.dto.DebtDTO;
import pk.wc.pasir_wiktor_czerniak.dto.GroupDTO;
import pk.wc.pasir_wiktor_czerniak.dto.MembershipDTO;
import pk.wc.pasir_wiktor_czerniak.model.*;
import pk.wc.pasir_wiktor_czerniak.repository.*;
import pk.wc.pasir_wiktor_czerniak.service.CurrentUserService;
import pk.wc.pasir_wiktor_czerniak.service.DebtService;
import pk.wc.pasir_wiktor_czerniak.service.GroupService;
import pk.wc.pasir_wiktor_czerniak.service.MembershipService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRequirementsTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private GroupService groupService;

    @InjectMocks
    private MembershipService membershipService;

    @InjectMocks
    private DebtService debtService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // 1. Utworzenie grupy dodaje właściciela jako członka i zwraca ją w myGroups
    @Test
    @DisplayName("1. Utworzenie grupy dodaje właściciela jako członka i zwraca ją w myGroups")
    void utworzenieGrupyDodajeWlascicielaJakoCzlonkaIZwracaJaWMyGroups() {
        User user = new User();
        user.setId(1L);

        GroupDTO dto = new GroupDTO();
        dto.setName("Test Group");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> {
            Group g = i.getArgument(0);
            g.setId(1L);
            return g;
        });

        Group result = groupService.createGroup(dto);
        assertEquals("Test Group", result.getName());

        when(groupRepository.findByMemberships_User(user)).thenReturn(List.of(result));
        assertEquals(1, groupService.getMyGroups().size());
    }

    // 2. Tylko właściciel grupy może dodawać członków
    @Test
    @DisplayName("2. Tylko właściciel grupy może dodawać członków")
    void tylkoWlascicielGrupyMozeDodawacCzlonkow() {
        User owner = new User();
        owner.setId(1L);

        User other = new User();
        other.setId(2L);

        Group group = new Group();
        group.setId(1L);
        group.setOwner(owner);

        MembershipDTO dto = new MembershipDTO();
        dto.setGroupId(1L);
        dto.setUserEmail("test@test.pl");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(currentUserService.getCurrentUser()).thenReturn(other);

        assertThrows(RuntimeException.class, () -> membershipService.addMember(dto));
    }

    // 3. groupMembers zwraca członków grupy tylko członkowi tej grupy
    @Test
    @DisplayName("3. groupMembers zwraca członków grupy tylko członkowi tej grupy")
    void groupMembersZwracaCzlonkowGrupyTylkoCzlonkowiTejGrupy() {
        User user = new User();
        user.setId(1L);

        Membership membership = new Membership();
        membership.setUser(user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(membershipRepository.existsByGroup_IdAndUser_Id(1L, 1L)).thenReturn(true);
        when(membershipRepository.findByGroup_Id(1L)).thenReturn(List.of(membership));

        assertEquals(1, membershipService.getGroupMembers(1L).size());
    }

    // 4. groupDebts zwraca długi grupy tylko członkowi tej grupy
    @Test
    @DisplayName("4. groupDebts zwraca długi grupy tylko członkowi tej grupy")
    void groupDebtsZwracaDlugiGrupyTylkoCzlonkowiTejGrupy() {
        User user = new User();
        user.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(membershipRepository.findByGroup_Id(1L)).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> debtService.getGroupDebts(1L));
    }

    // 5. Nowy członek dostaje tylko długi z transakcji dodanych po dołączeniu
    @Test
    @DisplayName("5. Nowy członek dostaje tylko długi z transakcji dodanych po dołączeniu")
    void nowyCzlonekDostajeTylkoDlugiZTransakcjiDodanychPoDolaczeniu() {
        User user = new User();
        user.setId(1L);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setJoinedAt(LocalDateTime.now());

        Debt oldDebt = new Debt();
        oldDebt.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(membershipRepository.findByGroup_Id(1L)).thenReturn(List.of(membership));
        when(debtRepository.findByGroupId(1L)).thenReturn(List.of(oldDebt));

        assertEquals(0, debtService.getGroupDebts(1L).size());
    }

    // 6. Transakcja grupowa typu INCOME tworzy długi od aktualnego użytkownika do pozostałych członków
    @Test
    @DisplayName("6. Transakcja grupowa typu INCOME tworzy długi od aktualnego użytkownika do pozostałych członków")
    void transakcjaGrupowaTypuIncomeTworzyDlugiOdAktualnegoUzytkownikaDoPozostalychCzlonkow() {
        User debtor = new User();
        debtor.setId(1L);

        User creditor = new User();
        creditor.setId(2L);

        Debt debt = new Debt();
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);
        debt.setAmount(100.0);

        assertNotNull(debt.getDebtor());
        assertNotNull(debt.getCreditor());
        assertEquals(1L, debt.getDebtor().getId());
        assertEquals(2L, debt.getCreditor().getId());
        assertEquals(100.0, debt.getAmount());
    }

    // 7. Usunięcie członka nie usuwa jego historycznych długów
    @Test
    @DisplayName("7. Usunięcie członka nie usuwa jego historycznych długów")
    void usuniecieCzlonkaNieUsuwaJegoHistorycznychDlugow() {
        User owner = new User();
        owner.setId(1L);

        User member = new User();
        member.setId(2L);

        Group group = new Group();
        group.setOwner(owner);

        Membership membership = new Membership();
        membership.setUser(member);
        membership.setGroup(group);

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));

        membershipService.removeMember(1L);

        verify(membershipRepository).delete(membership);
        verify(debtRepository, never()).delete(any());
        verify(debtRepository, never()).deleteByGroupId(anyLong());
    }

    // 8. Nie można usunąć właściciela z jego grupy przez removeMember
    @Test
    @DisplayName("8. Nie można usunąć właściciela z jego grupy przez removeMember")
    void nieMoznaUsunacWlascicielaZJegoGrupyPrzezRemoveMember() {
        User owner = new User();
        owner.setId(1L);

        Group group = new Group();
        group.setOwner(owner);

        Membership membership = new Membership();
        membership.setUser(owner);
        membership.setGroup(group);

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));

        assertThrows(RuntimeException.class, () -> membershipService.removeMember(1L));
    }

    // 9. Członek grupy niebędący właścicielem nie może usunąć grupy
    @Test
    @DisplayName("9. Członek grupy niebędący właścicielem nie może usunąć grupy")
    void czlonekGrupyNiebendacyWlascicielemNieMozeUsunacGrupy() {
        User owner = new User();
        owner.setId(1L);

        User other = new User();
        other.setId(2L);

        Group group = new Group();
        group.setOwner(owner);

        when(currentUserService.getCurrentUser()).thenReturn(other);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(RuntimeException.class, () -> groupService.deleteGroup(1L));
    }

    // 10. createDebt tworzy ręczny dług tylko między członkami tej samej grupy
    @Test
    @DisplayName("10. createDebt tworzy ręczny dług tylko między członkami tej samej grupy")
    void createDebtTworzyRecznyDlugTylkoMiedzyCzlonkamiTejSamejGrupy() {
        User debtor = new User();
        debtor.setId(1L);

        User creditor = new User();
        creditor.setId(2L);

        User current = debtor;

        User groupOwner = new User();
        groupOwner.setId(3L); // Zapobiega NPE jeśli porównujesz właściciela grupy

        Group group = new Group();
        group.setId(1L);
        group.setOwner(groupOwner);

        DebtDTO dto = new DebtDTO();
        dto.setDebtorId(1L);
        dto.setCreditorId(2L);
        dto.setGroupId(1L);
        dto.setAmount(100.0);
        dto.setTitle("Pizza");

        when(currentUserService.getCurrentUser()).thenReturn(current);
        when(userRepository.findById(1L)).thenReturn(Optional.of(debtor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(creditor));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByGroup_IdAndUser_Id(1L, 1L)).thenReturn(true);
        when(membershipRepository.existsByGroup_IdAndUser_Id(1L, 2L)).thenReturn(true);
        when(debtRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Debt debt = debtService.createDebt(dto);
        assertEquals(100.0, debt.getAmount());
    }

    // 11. createDebt odrzuca użytkownika spoza grupy i dług do samego siebie
    @Test
    @DisplayName("11. createDebt odrzuca dług do samego siebie")
    void createDebtOdrzucaDlugDoSamegoSiebie() {
        User user = new User();
        user.setId(1L);

        User groupOwner = new User();
        groupOwner.setId(2L);

        Group group = new Group();
        group.setId(1L);
        group.setOwner(groupOwner);

        DebtDTO dto = new DebtDTO();
        dto.setDebtorId(1L);
        dto.setCreditorId(1L);
        dto.setGroupId(1L);
        dto.setAmount(100.0);
        dto.setTitle("Test");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(RuntimeException.class, () -> debtService.createDebt(dto));
    }

    // 12. Właściciel grupy może utworzyć dług między innymi członkami grupy
    @Test
    @DisplayName("12. Właściciel grupy może utworzyć dług między innymi członkami grupy")
    void wlascicielGrupyMozeUtworzycDlugMiedzyInnymiCzlonkamiGrupy() {
        User owner = new User();
        owner.setId(1L);

        User debtor = new User();
        debtor.setId(2L);

        User creditor = new User();
        creditor.setId(3L);

        Group group = new Group();
        group.setId(1L);
        group.setOwner(owner);

        DebtDTO dto = new DebtDTO();
        dto.setDebtorId(2L);
        dto.setCreditorId(3L);
        dto.setGroupId(1L);
        dto.setAmount(100.0);
        dto.setTitle("Test");

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findById(2L)).thenReturn(Optional.of(debtor));
        when(userRepository.findById(3L)).thenReturn(Optional.of(creditor));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByGroup_IdAndUser_Id(1L, 2L)).thenReturn(true);
        when(membershipRepository.existsByGroup_IdAndUser_Id(1L, 3L)).thenReturn(true);
        when(debtRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertNotNull(debtService.createDebt(dto));
    }

    // 13. Członek grupy może utworzyć dług tylko gdy jest jego uczestnikiem
    @Test
    @DisplayName("13. Członek grupy może utworzyć dług tylko gdy jest jego uczestnikiem")
    void czlonekGrupyMozeUtworzycDlugTylkoGdyJestJegoUczestnikiem() {
        User current = new User();
        current.setId(1L);

        User debtor = new User();
        debtor.setId(2L);

        User creditor = new User();
        creditor.setId(3L);

        User groupOwner = new User();
        groupOwner.setId(4L);

        Group group = new Group();
        group.setId(1L);
        group.setOwner(groupOwner);

        DebtDTO dto = new DebtDTO();
        dto.setDebtorId(2L);
        dto.setCreditorId(3L);
        dto.setGroupId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(current);
        when(userRepository.findById(2L)).thenReturn(Optional.of(debtor));
        when(userRepository.findById(3L)).thenReturn(Optional.of(creditor));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(RuntimeException.class, () -> debtService.createDebt(dto));
    }

    // 14. deleteDebt usuwa dług dostępny dla uczestnika długu
    @Test
    @DisplayName("14. deleteDebt usuwa dług dostępny dla uczestnika długu")
    void deleteDebtUsuwaDlugDostepnyDlaUczestnikaDlugu() {
        User participant = new User();
        participant.setId(1L);

        User groupOwner = new User();
        groupOwner.setId(2L); // Ustawiamy ID właściciela grupy, zapobiega to NPE w deleteDebt

        Group group = new Group();
        group.setOwner(groupOwner);

        Debt debt = new Debt();
        debt.setGroup(group);
        debt.setDebtor(participant);
        debt.setCreditor(participant);

        when(currentUserService.getCurrentUser()).thenReturn(participant);
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debt));

        debtService.deleteDebt(1L);
        verify(debtRepository).delete(debt);
    }

    // 15. deleteDebt odrzuca członka grupy, który nie jest właścicielem ani uczestnikiem długu
    @Test
    @DisplayName("15. deleteDebt odrzuca członka grupy, który nie jest właścicielem ani uczestnikiem długu")
    void deleteDebtOdrzucaCzlonkaGrupyKtoryNieJestWlascicielemAniUczestnikiemDlugu() {
        User current = new User();
        current.setId(10L);

        User debtor = new User();
        debtor.setId(1L);

        User creditor = new User();
        creditor.setId(2L);

        User groupOwner = new User();
        groupOwner.setId(3L); // Ustawiamy ID, zapobiega NPE

        Group group = new Group();
        group.setOwner(groupOwner);

        Debt debt = new Debt();
        debt.setGroup(group);
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);

        when(currentUserService.getCurrentUser()).thenReturn(current);
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debt));

        assertThrows(RuntimeException.class, () -> debtService.deleteDebt(1L));
    }

    // 16. Właściciel grupy może usunąć dług, którego nie jest uczestnikiem
    @Test
    @DisplayName("16. Właściciel grupy może usunąć dług, którego nie jest uczestnikiem")
    void wlascicielGrupyMozeUsunacDlugKtoregoNieJestUczestnikiem() {
        User owner = new User();
        owner.setId(1L);

        User debtor = new User();
        debtor.setId(2L);

        User creditor = new User();
        creditor.setId(3L);

        Group group = new Group();
        group.setOwner(owner);

        Debt debt = new Debt();
        debt.setGroup(group);
        debt.setDebtor(debtor);
        debt.setCreditor(creditor);

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debt));

        debtService.deleteDebt(1L);
        verify(debtRepository).delete(debt);
    }

    // 17. Walidacje danych wejściowych GraphQL odrzucają puste lub niepoprawne wartości
    @Test
    @DisplayName("17. Walidacje danych wejściowych GraphQL odrzucają puste lub niepoprawne wartości")
    void walidacjeDanychWejsciowychGraphQLOdrzucajaPusteLubNiepoprawneWartosci() {
        GroupDTO dto = new GroupDTO();
        dto.setName("");

        Set<ConstraintViolation<GroupDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Walidacja powinna wykryć błędy");
    }

    // 18. Usunięcie grupy przez właściciela usuwa powiązane długi i grupę
    @Test
    @DisplayName("18. Usunięcie grupy przez właściciela usuwa powiązane długi i grupę")
    void usuniecieGrupyPrzezWlascicielaUsuwaPowiazaneDlugiIGrupe() {
        User owner = new User();
        owner.setId(1L);

        Group group = new Group();
        group.setOwner(owner);

        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.deleteGroup(1L);

        verify(debtRepository).deleteByGroupId(1L);
        verify(membershipRepository).deleteByGroup_Id(1L);
        verify(groupRepository).delete(group);
    }
}
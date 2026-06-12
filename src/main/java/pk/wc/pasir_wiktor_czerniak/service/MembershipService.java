package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import pk.wc.pasir_wiktor_czerniak.dto.MembershipDTO;
import pk.wc.pasir_wiktor_czerniak.model.Group;
import pk.wc.pasir_wiktor_czerniak.model.Membership;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.GroupRepository;
import pk.wc.pasir_wiktor_czerniak.repository.MembershipRepository;
import pk.wc.pasir_wiktor_czerniak.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") ||
                        a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equalsIgnoreCase("admin"));
    }

    public List<Membership> getGroupMembers(
            Long groupId
    ) {
        User currentUser = currentUserService.getCurrentUser();

        if (!isAdmin() && !membershipRepository.existsByGroup_IdAndUser_Id(groupId, currentUser.getId())) {
            throw new RuntimeException("Brak dostepu do grupy");
        }

        return membershipRepository.findByGroup_Id(groupId);
    }

    public Membership addMember(
            MembershipDTO dto
    ) {
        // 1. Bezpieczne szukanie grupy
        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono grupy o podanym ID: " + dto.getGroupId()));

        User currentUser = currentUserService.getCurrentUser();

        // Sprawdzenie uprawnień (właściciel lub admin)
        if (!group.getOwner().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new RuntimeException("Tylko wlasciciel lub admin moze dodawac czlonkow");
        }

        // 2. Bezpieczne szukanie zapraszanego użytkownika po emailu
        User user = userRepository.findByEmail(dto.getUserEmail())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o emailu: " + dto.getUserEmail()));

        // Sprawdzenie czy już należy do grupy
        if (membershipRepository.existsByGroup_IdAndUser_Id(group.getId(), user.getId())) {
            throw new RuntimeException("Uzytkownik juz nalezy do grupy");
        }

        try {
            Membership membership = new Membership();
            membership.setGroup(group);
            membership.setUser(user);
            membership.setJoinedAt(LocalDateTime.now());

            return membershipRepository.save(membership);
        } catch (Exception e) {
            // Wypisze dokładny błąd w konsoli Twojego Spring Boota (np. błąd SQL, brak jakiegoś pola)
            e.printStackTrace();
            throw new RuntimeException("Błąd bazy danych przy zapisie członka: " + e.getMessage());
        }
    }

    public void removeMember(
            Long membershipId
    ) {
        Membership membership = membershipRepository.findById(membershipId).orElseThrow();
        User currentUser = currentUserService.getCurrentUser();
        Group group = membership.getGroup();

        if (!group.getOwner().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new RuntimeException("Tylko wlasciciel lub admin moze usuwac czlonkow");
        }

        if (membership.getUser().getId().equals(group.getOwner().getId())) {
            throw new RuntimeException("Nie mozna usunac wlasciciela grupy");
        }

        membershipRepository.delete(membership);
    }
}
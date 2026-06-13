package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a ->
                        "ADMIN".equals(a.getAuthority()) ||
                                "ROLE_ADMIN".equals(a.getAuthority()) ||
                                "admin".equalsIgnoreCase(a.getAuthority())
                );
    }

    public List<Membership> getGroupMembers(
            Long groupId
    ) {
        User currentUser = currentUserService.getCurrentUser();

        if (!isAdmin()
                && !membershipRepository.existsByGroup_IdAndUser_Id(
                groupId,
                currentUser.getId()
        )) {
            throw new SecurityException(
                    "Brak dostepu do grupy"
            );
        }

        return membershipRepository.findByGroup_Id(groupId);
    }

    public Membership addMember(
            MembershipDTO dto
    ) {

        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Nie znaleziono grupy o podanym ID: "
                                        + dto.getGroupId()
                        )
                );

        User currentUser = currentUserService.getCurrentUser();

        if (!group.getOwner().getId().equals(currentUser.getId())
                && !isAdmin()) {

            throw new SecurityException(
                    "Tylko wlasciciel lub admin moze dodawac czlonkow"
            );
        }

        User user = userRepository.findByEmail(dto.getUserEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Nie znaleziono użytkownika o emailu: "
                                        + dto.getUserEmail()
                        )
                );

        if (membershipRepository.existsByGroup_IdAndUser_Id(
                group.getId(),
                user.getId()
        )) {

            throw new IllegalStateException(
                    "Uzytkownik juz nalezy do grupy"
            );
        }

        try {

            Membership membership = new Membership();
            membership.setGroup(group);
            membership.setUser(user);
            membership.setJoinedAt(LocalDateTime.now());

            return membershipRepository.save(membership);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Błąd bazy danych przy zapisie członka: "
                            + e.getMessage(),
                    e
            );

        }
    }

    public void removeMember(
            Long membershipId
    ) {

        Membership membership =
                membershipRepository.findById(membershipId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nie znaleziono członkostwa o ID: "
                                                + membershipId
                                )
                        );

        User currentUser = currentUserService.getCurrentUser();
        Group group = membership.getGroup();

        if (!group.getOwner().getId().equals(currentUser.getId())
                && !isAdmin()) {

            throw new SecurityException(
                    "Tylko wlasciciel lub admin moze usuwac czlonkow"
            );
        }

        if (membership.getUser().getId()
                .equals(group.getOwner().getId())) {

            throw new IllegalStateException(
                    "Nie mozna usunac wlasciciela grupy"
            );
        }

        membershipRepository.delete(membership);
    }
}
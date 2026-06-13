package pk.wc.pasir_wiktor_czerniak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import pk.wc.pasir_wiktor_czerniak.dto.GroupDTO;
import pk.wc.pasir_wiktor_czerniak.model.Group;
import pk.wc.pasir_wiktor_czerniak.model.Membership;
import pk.wc.pasir_wiktor_czerniak.model.User;
import pk.wc.pasir_wiktor_czerniak.repository.DebtRepository;
import pk.wc.pasir_wiktor_czerniak.repository.GroupRepository;
import pk.wc.pasir_wiktor_czerniak.repository.MembershipRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final CurrentUserService currentUserService;

    // Pobieramy rolę zalogowanego użytkownika wprost ze Spring Security (omijamy model User)
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

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public List<Group> getMyGroups() {
        User currentUser = currentUserService.getCurrentUser();
        return groupRepository.findByMemberships_User(currentUser);
    }

    public Group createGroup(GroupDTO groupDTO) {
        User currentUser = currentUserService.getCurrentUser();
        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setOwner(currentUser);
        group.setCreatedAt(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);

        Membership membership = new Membership();
        membership.setGroup(savedGroup);
        membership.setUser(currentUser);
        membershipRepository.save(membership);

        return savedGroup;
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        User currentUser = currentUserService.getCurrentUser();
        Group group = groupRepository.findById(groupId).orElseThrow();

        // Jeśli to NIE jest właściciel i jednocześnie NIE jest admin, wyrzuć błąd
        if (!group.getOwner().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new RuntimeException("Tylko właściciel lub admin może usunąć grupę");
        }

        debtRepository.deleteByGroupId(groupId);
        membershipRepository.deleteByGroup_Id(groupId);
        groupRepository.delete(group);
    }
}
package pk.wc.pasir_wiktor_czerniak.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pk.wc.pasir_wiktor_czerniak.dto.MembershipDTO;
import pk.wc.pasir_wiktor_czerniak.model.Membership;
import pk.wc.pasir_wiktor_czerniak.service.MembershipService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MembershipGraphQLController {

    private final MembershipService membershipService;

    @QueryMapping
    public List<Membership> groupMembers(
            @Argument("groupId") Long groupId
    ) {
        return membershipService.getGroupMembers(groupId);
    }

    @MutationMapping
    public Membership addMember(
            // KLUCZOWA POPRAWKA: Nazwa w nawiasie musi odpowiadać dokładnie "membershipDTO" ze schemy GraphQL
            @Argument("membershipDTO") @Valid MembershipDTO membershipDTO
    ) {
        return membershipService.addMember(membershipDTO);
    }

    @MutationMapping
    public Boolean removeMember(
            @Argument("membershipId") Long membershipId
    ) {
        membershipService.removeMember(membershipId);
        return true;
    }
}
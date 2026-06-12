package pk.wc.pasir_wiktor_czerniak.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import pk.wc.pasir_wiktor_czerniak.dto.GroupDTO;
import pk.wc.pasir_wiktor_czerniak.model.Group;
import pk.wc.pasir_wiktor_czerniak.service.GroupService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GroupGraphQLController {

    private final GroupService groupService;

    @QueryMapping
    public List<Group> myGroups() {
        return groupService.getMyGroups();
    }

    @MutationMapping
    public Group createGroup(
            @Argument @Valid GroupDTO groupDTO
    ) {
        return groupService.createGroup(groupDTO);
    }

    @MutationMapping
    public Boolean deleteGroup(
            @Argument Long id
    ) {
        groupService.deleteGroup(id);
        return true;
    }
}
package pk.wc.pasir_wiktor_czerniak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupNotificationDto {

    private String type;

    private Long groupId;

    private String groupName;

    private String title;

    private Double amount;

    private Double userShare;

    private String createdByEmail;

    private String message;
}
package com.example.roko.dto.response;

import com.example.roko.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String titre;
    private String message;
    private Boolean lu;
    private LocalDateTime dateCreation;
    private NotificationType type;
}

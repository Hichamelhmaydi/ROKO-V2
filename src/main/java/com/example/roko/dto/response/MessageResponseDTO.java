
package com.example.roko.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private Boolean success;
    private String message;
}

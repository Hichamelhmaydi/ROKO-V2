package com.example.roko.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicStatsDTO {
    private long totalVoyageurs;
    private long totalVoyages;
    private int satisfiedRate;
    private String assistanceHours;
}

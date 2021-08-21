package com.vinyla.server.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RateDto {
    int vinylIdx;
    float rate;
    int rateCount;
}

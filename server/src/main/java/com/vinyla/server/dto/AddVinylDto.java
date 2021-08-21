package com.vinyla.server.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AddVinylDto {
    SearchDetailVinylDto vinylDetail;
    int my_rate;
    String my_memo;
}

package com.vinyla.server.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SearchVinylDto {
    int id;
    String coverImage;
    String title;
    String artist;
}

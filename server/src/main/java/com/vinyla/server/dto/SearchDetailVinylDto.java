package com.vinyla.server.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class SearchDetailVinylDto {
    int id;
    String title;
    String artist;
    String cover_img;
    String released;
    List<String> genres;
    List<Track> tracklist;
}

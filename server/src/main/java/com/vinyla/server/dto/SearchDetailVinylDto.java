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
//    String thumb;
    String released;
    List<String> genres;
    List<TrackList> tracklist;
}

package com.vinyla.server.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class DiscogsSearchDetail {
    int id;
    String title;
    String artists_sort;
    String released;
    String thumb;
    List<String> genres;
    List<TrackList> tracklist;
}
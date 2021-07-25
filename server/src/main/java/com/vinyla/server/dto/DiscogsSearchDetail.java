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
    List<Image> images;
    List<String> genres;
    List<Track> tracklist;
}
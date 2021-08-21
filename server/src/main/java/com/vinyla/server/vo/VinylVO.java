package com.vinyla.server.vo;

import lombok.Data;

@Data
public class VinylVO {
    int vinylIdx;
    String title;
    String imageUrl;
    String artist;
    float rate;
    int rateCount;
    int id;
    int year;
}

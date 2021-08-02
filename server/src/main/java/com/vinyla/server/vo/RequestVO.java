package com.vinyla.server.vo;

import lombok.Data;

@Data
public class RequestVO {
    int requestIdx;
    String title;
    String artist;
    String image;
    String memo;
    int userIdx;
}

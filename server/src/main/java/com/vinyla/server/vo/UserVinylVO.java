package com.vinyla.server.vo;

import lombok.Data;

@Data
public class UserVinylVO {
    int userIdx;
    int vinylIdx;
    boolean myVinyl;
    float rate;
    String comment;
}

package com.vinyla.server.vo;

import lombok.Data;

@Data
public class UserVO {
    int userIdx;
    String nickname;
    String profileUrl;
    String instaId;
    int rankIdx;
    int vinylNum;
}

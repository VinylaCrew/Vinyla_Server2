package com.vinyla.server.dto;

import com.vinyla.server.vo.RankVO;
import com.vinyla.server.vo.VinylVO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class HomeInfoDto {
    //my vinyl 정보
    VinylVO myVinyl;

    //recent vinyl 정보 배열
    List<VinylVO> recentVinyls;

    //내 정보
    String nickname;
    String profileUrl;
    RankVO rank;
    int vinylNum;
    List<String> myGenre;
}

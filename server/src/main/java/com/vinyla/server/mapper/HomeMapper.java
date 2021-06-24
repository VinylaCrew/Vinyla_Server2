package com.vinyla.server.mapper;

import com.vinyla.server.vo.RankVO;
import com.vinyla.server.vo.UserVO;
import com.vinyla.server.vo.VinylVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HomeMapper {
    UserVO homeUserInfo(int userIdx);
    RankVO matchRank(int rankIdx);
    List<String> myGenre(int userIdx);
    VinylVO myVinyl(int userIdx);
    List<VinylVO> recentVinyls(int userIdx);
}

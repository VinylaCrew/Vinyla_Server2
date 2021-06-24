package com.vinyla.server.service;

import com.vinyla.server.dto.HomeInfoDto;
import com.vinyla.server.mapper.HomeMapper;
import com.vinyla.server.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HomeService {

    @Autowired
    HomeMapper homeMapper;

    public HomeInfoDto homeUserInfo(int userIdx){
        HomeInfoDto homeInfoDto = new HomeInfoDto();

        UserVO userVO = homeMapper.homeUserInfo(userIdx);
        homeInfoDto.setNickname(userVO.getNickname()); //nickname
        homeInfoDto.setVinylNum(userVO.getVinylNum()); //vinylNum

        int rankIdx = userVO.getRankIdx();
        homeInfoDto.setRank(homeMapper.matchRank(rankIdx)); //rankName

        homeInfoDto.setMyGenre(homeMapper.myGenre(userIdx)); //myGenre (Array)

        homeInfoDto.setMyVinyl(homeMapper.myVinyl(userIdx)); //myVinyl

        homeInfoDto.setRecentVinyls(homeMapper.recentVinyls(userIdx)); //recentVinyls

        return homeInfoDto;
    }

}

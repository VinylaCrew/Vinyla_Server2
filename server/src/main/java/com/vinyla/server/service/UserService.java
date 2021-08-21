package com.vinyla.server.service;

import com.vinyla.server.dto.CheckDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vinyla.server.mapper.UserMapper;
import com.vinyla.server.vo.UserVO;

@Service
@Slf4j
public class UserService {

    @Autowired
    UserMapper userMapper;

    public boolean duplicateCheck(CheckDto nickname){
        if (userMapper.duplicateCheck(nickname) != 0){ // 중복 닉네임이 있을 때
            return false;
        }
        else
            return true;

    }

    public int signUp(UserVO user){
        UserVO userVO = new UserVO();
        userMapper.signUp(user);
        int userIdx = userVO.getUserIdx();
        return userIdx;
    }

    public int signIn(CheckDto user){
        return userMapper.signIn(user);
    }
}

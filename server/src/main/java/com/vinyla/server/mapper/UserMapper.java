package com.vinyla.server.mapper;

import java.util.List;

import com.vinyla.server.dto.CheckDto;
import org.apache.ibatis.annotations.Mapper;
import com.vinyla.server.vo.UserVO;

@Mapper
public interface UserMapper {
    void signUp(UserVO user);
    int duplicateCheck(CheckDto nickname);
    int signIn(CheckDto nickname);
}

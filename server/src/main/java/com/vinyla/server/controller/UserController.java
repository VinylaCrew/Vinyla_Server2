package com.vinyla.server.controller;

import com.vinyla.server.dto.CheckDto;
import com.vinyla.server.service.SecurityService;
import com.vinyla.server.service.UserService;
import com.vinyla.server.util.DefaultRes;
import com.vinyla.server.util.ResponseMessage;
import com.vinyla.server.util.StatusCode;
import com.vinyla.server.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    SecurityService securityService;

    @PostMapping("/check")
    public ResponseEntity duplicateCheck(@RequestBody CheckDto nickname){
        if(!userService.duplicateCheck(nickname)){
            return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, ResponseMessage.DUPLICATE_NICKNAME, false), HttpStatus.BAD_REQUEST);
        }
        else{
            return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.NO_DUPLICATE, true), HttpStatus.OK);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody UserVO user){
        if(user == null){
            return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, ResponseMessage.NO_INFORMATION, false), HttpStatus.BAD_REQUEST);
        }
        String token = securityService.createToken(user.getUserIdx(), (2*1000*60));
        log.info("token is here!!! => ", token);

        return new ResponseEntity(DefaultRes.res(StatusCode.OK, ResponseMessage.CREATED_USER,
                true), HttpStatus.OK); //userService.signUp(user, token);
    }

}

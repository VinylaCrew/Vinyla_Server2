package com.vinyla.server.controller;

import com.vinyla.server.dto.HomeInfoDto;
import com.vinyla.server.service.HomeService;
import com.vinyla.server.util.DefaultRes;
import com.vinyla.server.util.ResponseMessage;
import com.vinyla.server.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    HomeService homeService;

    @GetMapping("/{userIdx}") // 나중에 token으로 변경
    public ResponseEntity homeInfo(@PathVariable int userIdx){
        return new ResponseEntity(DefaultRes.res(StatusCode.OK, true,
                ResponseMessage.HOME_INFO_SUCCESS, homeService.homeUserInfo(userIdx)), HttpStatus.OK);
    }
}

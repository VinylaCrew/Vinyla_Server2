package com.vinyla.server.controller;

import com.vinyla.server.service.VinylService;
import com.vinyla.server.util.DefaultRes;
import com.vinyla.server.util.ResponseMessage;
import com.vinyla.server.util.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vinyls")
@Slf4j
public class VinylController {
    @Autowired
    VinylService vinylService;

    @GetMapping("/search")
    public ResponseEntity search(@RequestParam(value = "q") String q){
        if(q.isEmpty()){
            return new ResponseEntity(DefaultRes.res(StatusCode.BAD_REQUEST, false,
                    ResponseMessage.NO_SEARCH_WORD), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(DefaultRes.res(StatusCode.OK, true,
                ResponseMessage.VINYL_SEARCH_SUCCESS, vinylService.search(q)), HttpStatus.OK);
    }
}

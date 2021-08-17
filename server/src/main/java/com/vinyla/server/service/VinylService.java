package com.vinyla.server.service;

import com.vinyla.server.dto.*;
import com.vinyla.server.mapper.VinylMapper;
import com.vinyla.server.vo.UserGenreVO;
import com.vinyla.server.vo.UserVinylVO;
import com.vinyla.server.vo.VinylGenreVO;
import com.vinyla.server.vo.VinylVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Configuration
@PropertySource("classpath:application.properties")
@Service
@Slf4j
public class VinylService {

    @Autowired
    VinylMapper vinylMapper;

    @Value("${discogs.key}")
    public String key;

    @Value("${discogs.secret}")
    public String secret;

    private final WebClient webClient;

    public VinylService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.discogs.com").build();
    }

    public List<SearchVinylDto> search(String q) {
        List<SearchVinylDto> searchVinylDtoList = new ArrayList<>();

        Mono<DiscogsSearch> resp = this.webClient.get().uri("/database/search?q="+q+"&type=release&key="+key+"&secret="+secret).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(DiscogsSearch.class).log();

        Mono<List<Results>> response = resp.map(DiscogsSearch::getResults);
        List<Results> results = response.block();
        for(int i = 0; i < results.size(); i++){
            SearchVinylDto searchVinylDto = new SearchVinylDto();
            searchVinylDto.setId(results.get(i).id);
            searchVinylDto.setThumb(results.get(i).thumb);
            String titleArtist = results.get(i).title;
            String[] ta = titleArtist.split("-");

            searchVinylDto.setTitle(ta[0].trim());
            searchVinylDto.setArtist(ta[1].trim());
            searchVinylDtoList.add(searchVinylDto);
        }

        return searchVinylDtoList;
    }

    public SearchDetailVinylDto searchDetail(int id){
        SearchDetailVinylDto searchDetailVinylDto = new SearchDetailVinylDto();

        Mono<DiscogsSearchDetail> resp = this.webClient.get().uri("/releases/"+id).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(DiscogsSearchDetail.class).log();
        DiscogsSearchDetail response = resp.block();

        searchDetailVinylDto.setId(response.getId());
        searchDetailVinylDto.setTitle(response.getTitle());
        searchDetailVinylDto.setArtist(response.getArtists_sort());
        searchDetailVinylDto.setYear(response.getYear());
        searchDetailVinylDto.setGenres(response.getGenres());

        //cover image
        Mono<List<Image>> respImg = resp.map(DiscogsSearchDetail::getImages);
        List<Image> images = respImg.block();

        for(int i = 0; i < images.size(); i++){
//            Image img = new Image();
            if(images.get(i).getType().equals("primary")){
                searchDetailVinylDto.setCover_img(images.get(i).getUri());
                break;
            }
        }

        //tracklist
        Mono<List<Track>> respTL = resp.map(DiscogsSearchDetail::getTracklist);
        List<Track> tracklists = respTL.block();
        List<String> tl = new ArrayList<>();
        for(int i = 0; i < tracklists.size(); i++){
            tl.add(tracklists.get(i).getTitle());
        }
        searchDetailVinylDto.setTracklist(tl);

        //rate
        // 우리 DB에 있는 바이닐이면 거기서 가져오기
        // 없는 거면 그냥 0.0, 0 으로 두


        return searchDetailVinylDto;
    }

    public boolean addVinyl(AddVinylDto addVinyl){
        int vinylIdx;
        SearchDetailVinylDto searchDetailVinylDto = addVinyl.getVinylDetail();

        // {우리 디비(vinyl)에 있는 바이닐인지 체크 후,
        // 없으면 vinyl에 새로 추가. rate 관련은 디폴트로. 넣은 담에 리턴으로 vinylIdx도 받아놓기.}
        if(vinylMapper.hasVinyl(addVinyl.getVinylDetail().getId()) == null){
            VinylVO vinylVO = new VinylVO();
            vinylVO.setTitle(searchDetailVinylDto.getTitle());
            vinylVO.setArtist(searchDetailVinylDto.getArtist());
            vinylVO.setImageUrl(searchDetailVinylDto.getCover_img());
            vinylVO.setId(searchDetailVinylDto.getId());
            vinylVO.setYear(searchDetailVinylDto.getYear());
            vinylVO.setRate(addVinyl.getMy_rate());
            vinylMapper.addNewVinyl(vinylVO);
            vinylIdx = vinylVO.getVinylIdx();

            log.info("vinylIdx *** "+vinylIdx);

            // track 테이블 집어넣기
            List<String> tracklist = searchDetailVinylDto.getTracklist();
            for(String title : tracklist){
                AddTrackDto addTrackDto = new AddTrackDto();
                addTrackDto.setVinylIdx(vinylIdx);
                addTrackDto.setTitle(title);
                vinylMapper.addTrack(addTrackDto);
            }

        }

        // {우리 디비에 있는 바이닐이면,
        // vinyl 테이블에서 id로 찾아와서(자체 집어넣은거면 null일 거라 신경 X) rate 정보 업데이트(다시 계산)
        else{
            vinylIdx = vinylMapper.hasVinyl(addVinyl.getVinylDetail().getId());
            float prevRateSum = searchDetailVinylDto.getRate() * searchDetailVinylDto.getRateCount();
            int newRateCount = searchDetailVinylDto.getRateCount() + 1;
            float newRate = (prevRateSum + addVinyl.getMy_rate()) / newRateCount;

            RateDto rateDto = new RateDto();
            rateDto.setVinylIdx(vinylIdx);
            rateDto.setRate(newRate);
            rateDto.setRateCount(newRateCount);

            vinylMapper.setRate(rateDto);


            // 이 바이닐의 장르가 이미 genre 테이블에 있는 건지 체크 후,
            // 있는 거면 IDx 찾아서 vinyl_genre 테이블에 추가
            // user_genre에도 업데이트


        }
        for(String genre : searchDetailVinylDto.getGenres()){ // 바이닐의 장르 하나하나
            int genreIdx;
            if(vinylMapper.hasGenre(genre) == null){ // 장르 테이블에 없던 장르면
                GenreDto genreDto = new GenreDto();
                genreDto.setGenreName(genre);
                vinylMapper.addGenre(genreDto); // 장르 추가하고 idx 뽑아오기
                genreIdx = genreDto.getGenreIdx();

                UserGenreVO userGenreVO = new UserGenreVO();
                userGenreVO.setUserIdx(3);
                userGenreVO.setGenreIdx(genreIdx);
                userGenreVO.setGenreNum(1);
                vinylMapper.addUserGenre(userGenreVO);
            }
            else{ // 장르 테이블에 있던 장르면
                genreIdx = vinylMapper.hasGenre(genre); // idx 찾아오기

                UserGenreVO userGenreVO = new UserGenreVO();
                userGenreVO.setUserIdx(3);
                userGenreVO.setGenreIdx(genreIdx);
                userGenreVO.setGenreNum(1);
                vinylMapper.addGenreNum(userGenreVO);
            }

            // vinyl_genre 테이블에 추가
            VinylGenreVO vinylGenreVO = new VinylGenreVO();
            vinylGenreVO.setVinylIdx(vinylIdx);
            vinylGenreVO.setGenreIdx(genreIdx);
            vinylMapper.addVinylGenre(vinylGenreVO);

        }

        // user_vinyl 테이블 업데이트
        UserVinylVO userVinylVO = new UserVinylVO();
        userVinylVO.setUserIdx(3); // 토큰 하기 전까지 일단은 3번 유...
        userVinylVO.setVinylIdx(vinylIdx);
        userVinylVO.setRate(addVinyl.getMy_rate());
        userVinylVO.setComment(addVinyl.getMy_memo());
        vinylMapper.addUserVinyl(userVinylVO);

        // user 테이블의 vinylNum 정보 업데이트
        vinylMapper.addVinylNum(3); // 토큰 하기 전까지 일단은 3번 유저...

//        log.info("shibal*** - "+ addVinyl.getVinylDetail().getArtist());

        return true;
    }
}

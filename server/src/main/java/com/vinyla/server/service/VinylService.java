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
        // ?????? DB??? ?????? ??????????????? ????????? ????????????
        // ?????? ?????? ?????? 0.0, 0 ?????? ???


        return searchDetailVinylDto;
    }

    public boolean addVinyl(AddVinylDto addVinyl){
        int vinylIdx;
        SearchDetailVinylDto searchDetailVinylDto = addVinyl.getVinylDetail();

        // {?????? ??????(vinyl)??? ?????? ??????????????? ?????? ???,
        // ????????? vinyl??? ?????? ??????. rate ????????? ????????????. ?????? ?????? ???????????? vinylIdx??? ????????????.}
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

            // track ????????? ????????????
            List<String> tracklist = searchDetailVinylDto.getTracklist();
            for(String title : tracklist){
                AddTrackDto addTrackDto = new AddTrackDto();
                addTrackDto.setVinylIdx(vinylIdx);
                addTrackDto.setTitle(title);
                vinylMapper.addTrack(addTrackDto);
            }

        }

        // {?????? ????????? ?????? ???????????????,
        // vinyl ??????????????? id??? ????????????(?????? ?????????????????? null??? ?????? ?????? X) rate ?????? ????????????(?????? ??????)
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


            // ??? ???????????? ????????? ?????? genre ???????????? ?????? ?????? ?????? ???,
            // ?????? ?????? IDx ????????? vinyl_genre ???????????? ??????
            // user_genre?????? ????????????


        }
        for(String genre : searchDetailVinylDto.getGenres()){ // ???????????? ?????? ????????????
            int genreIdx;
            if(vinylMapper.hasGenre(genre) == null){ // ?????? ???????????? ?????? ?????????
                GenreDto genreDto = new GenreDto();
                genreDto.setGenreName(genre);
                vinylMapper.addGenre(genreDto); // ?????? ???????????? idx ????????????
                genreIdx = genreDto.getGenreIdx();

                UserGenreVO userGenreVO = new UserGenreVO();
                userGenreVO.setUserIdx(3);
                userGenreVO.setGenreIdx(genreIdx);
                userGenreVO.setGenreNum(1);
                vinylMapper.addUserGenre(userGenreVO);
            }
            else{ // ?????? ???????????? ?????? ?????????
                genreIdx = vinylMapper.hasGenre(genre); // idx ????????????

                UserGenreVO userGenreVO = new UserGenreVO();
                userGenreVO.setUserIdx(3);
                userGenreVO.setGenreIdx(genreIdx);
                userGenreVO.setGenreNum(1);
                vinylMapper.addGenreNum(userGenreVO);
            }

            // vinyl_genre ???????????? ??????
            VinylGenreVO vinylGenreVO = new VinylGenreVO();
            vinylGenreVO.setVinylIdx(vinylIdx);
            vinylGenreVO.setGenreIdx(genreIdx);
            vinylMapper.addVinylGenre(vinylGenreVO);

        }

        // user_vinyl ????????? ????????????
        UserVinylVO userVinylVO = new UserVinylVO();
        userVinylVO.setUserIdx(3); // ?????? ?????? ????????? ????????? 3??? ???...
        userVinylVO.setVinylIdx(vinylIdx);
        userVinylVO.setRate(addVinyl.getMy_rate());
        userVinylVO.setComment(addVinyl.getMy_memo());
        vinylMapper.addUserVinyl(userVinylVO);

        // user ???????????? vinylNum ?????? ????????????
        vinylMapper.addVinylNum(3); // ?????? ?????? ????????? ????????? 3??? ??????...

//        log.info("shibal*** - "+ addVinyl.getVinylDetail().getArtist());

        return true;
    }
}

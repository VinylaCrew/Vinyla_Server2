package com.vinyla.server.service;

import com.vinyla.server.dto.*;
import lombok.extern.slf4j.Slf4j;
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
            searchVinylDto.setCoverImage(results.get(i).cover_image);
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
        List<TrackList> responseTL = new ArrayList<>();

        Mono<DiscogsSearchDetail> resp = this.webClient.get().uri("/releases/"+id).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(DiscogsSearchDetail.class).log();
        DiscogsSearchDetail response = resp.block();

        searchDetailVinylDto.setId(response.getId());
        searchDetailVinylDto.setTitle(response.getTitle());
//        searchDetailVinylDto.setThumb(response.getThumb());
        searchDetailVinylDto.setArtist(response.getArtists_sort());
        searchDetailVinylDto.setReleased(response.getReleased());
        searchDetailVinylDto.setGenres(response.getGenres());

        Mono<List<TrackList>> respTL = resp.map(DiscogsSearchDetail::getTracklist);
        List<TrackList> tracklists = respTL.block();

        for(int i = 0; i < tracklists.size(); i++){
            TrackList tl = new TrackList();
            tl.setPosition(tracklists.get(i).getPosition());
            tl.setTitle(tracklists.get(i).getTitle());
            tl.setDuration(tracklists.get(i).getDuration());
            responseTL.add(tl);
        }
        searchDetailVinylDto.setTracklist(responseTL);

        return searchDetailVinylDto;
    }
}

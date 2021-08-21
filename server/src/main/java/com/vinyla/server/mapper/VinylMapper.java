package com.vinyla.server.mapper;

import com.vinyla.server.dto.AddTrackDto;
import com.vinyla.server.dto.GenreDto;
import com.vinyla.server.dto.RateDto;
import com.vinyla.server.vo.UserGenreVO;
import com.vinyla.server.vo.UserVinylVO;
import com.vinyla.server.vo.VinylGenreVO;
import com.vinyla.server.vo.VinylVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VinylMapper {
    Integer hasVinyl(int id);
    void addNewVinyl(VinylVO vinylVO);
    void setRate(RateDto rateDto);
    void addTrack(AddTrackDto addTrackDto);
    void addUserVinyl(UserVinylVO userVinylVO);
    void addVinylNum(int userIdx);
    Integer hasGenre(String genreName);
    void addGenre(GenreDto genreDto);
    void addVinylGenre(VinylGenreVO vinylGenreVO);
    void addUserGenre(UserGenreVO userGenreVO);
    void addGenreNum(UserGenreVO userGenreVO);
}

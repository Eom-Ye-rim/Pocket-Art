package com.pocekt.art.dto.response;

import com.pocekt.art.entity.Contest;

import java.util.List;
import java.util.stream.Collectors;

public class MypageContestResponse {
    List<String> photoList;

    public MypageContestResponse(Contest contest) {
        // Extract photo URLs from the photo list
        this.photoList = contest.getPhotoList()
                .stream()
                .map(photo -> photo.getFileUrl())
                .filter(file -> file != null)
                .collect(Collectors.toList());

    }
}


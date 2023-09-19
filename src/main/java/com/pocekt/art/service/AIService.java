package com.pocekt.art.service;


import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
    @RequiredArgsConstructor
    public class AIService {
    private final OpenAiService openAiService;
    private final StorageService s3Service;

    public List<String> generatePicture(String prompt) {
        List<String> pictureUrls = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            CreateImageRequest createImageRequest = CreateImageRequest.builder()
                    .prompt(prompt)
                    .size("256x256") //size 조정
                    .n(1) // 개수 조정
                    .build();

            String url = openAiService.createImage(createImageRequest).getData().get(0).getUrl();
            s3Service.upload(url); //AWS S3 bucket 저장
            pictureUrls.add(url);

        }

        return pictureUrls;
    }
}


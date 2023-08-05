package com.pocekt.art.controller;


import com.pocekt.art.service.AIService;
import com.pocekt.art.service.PapagoService;
import com.pocekt.art.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class Text2ImageController {
    private final AIService aiService;
    private final StorageService s3Service;

    private final PapagoService papagoService;
    @GetMapping("")
    public String test(@RequestBody String word){
        String testWord = papagoService.test(word);
        System.out.println(testWord);
        return testWord;
    }
    @PostMapping("/generate")
    public List<String> generateImage(@RequestParam  String prompt) {
        String word= papagoService.test(prompt);
        List<String> pictureUrlList = aiService.generatePicture(word);

        return pictureUrlList;

    }

    @GetMapping("/get-history")
    public ResponseEntity<?> getHistory() {

            return new ResponseEntity<>(s3Service.listObjects(), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImage(@RequestParam("name") String name) {

        s3Service.deleteObject(name);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}


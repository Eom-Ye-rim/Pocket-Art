package com.pocekt.art.controller;


import com.pocekt.art.dto.request.GPTCompletionChatRequest;
import com.pocekt.art.dto.request.GPTCompletionRequest;
import com.pocekt.art.dto.response.CompletionChatResponse;
import com.pocekt.art.dto.response.CompletionResponse;
import com.pocekt.art.service.GPTChatRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatgpt/rest")
@RequiredArgsConstructor
public class ChatGPTRestController {

    private final GPTChatRestService gptChatRestService;

    @PostMapping("/completion")
    public CompletionResponse completion(final @RequestBody GPTCompletionRequest gptCompletionRequest) {

        return gptChatRestService.completion(gptCompletionRequest);
    }

    @PostMapping("/completion/chat")
    public CompletionChatResponse completionChat(final @RequestBody GPTCompletionChatRequest gptCompletionChatRequest) {
        return gptChatRestService.completionChat(gptCompletionChatRequest);
    }

}
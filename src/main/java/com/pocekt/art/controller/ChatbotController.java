package com.pocekt.art.controller;


import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2IntentMessage;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2IntentMessageText;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v2.model.GoogleCloudDialogflowV2WebhookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatbotController {

    private static JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

    @PostMapping("/dialogFlowWebHook")
    public ResponseEntity<?> dialogFlowWebHook(@RequestBody String requestStr, HttpServletRequest servletRequest) throws IOException {
        try {
            // Dialogflow 웹훅 요청을 파싱
            GoogleCloudDialogflowV2WebhookRequest request = jacksonFactory.createJsonParser(requestStr).parse(GoogleCloudDialogflowV2WebhookRequest.class);

            // Intent의 displayName을 가져옴
            String intentName = request.getQueryResult().getIntent().getDisplayName();

            // Intent 이름을 기반으로 응답을 가져옴
            String fulfillmentText = getFulfillmentTextForIntent(intentName);

            // WebhookResponse 객체 생성 및 fulfillmentText 설정
            GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse();
            response.setFulfillmentText(fulfillmentText);

            // ResponseEntity로 응답 반환
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Intent 이름을 기반으로 응답을 가져오는 로직
    private String getFulfillmentTextForIntent(String intentName) {
        // Intent 이름에 따라 적절한 응답을 반환하는 로직을 구현
        if ("작품질문".equals(intentName)) {
            return "클로드 모네는 프랑스의 인상주의 화가로, 인상파의 개척자이며 지도자입니다. 대표작품으로는 인상,해돋이, 수련, Houses of Parliament serie 등이 있습니다.";
        } else {
            return "죄송하지만 이해하지 못했어요.";
        }
    }

}

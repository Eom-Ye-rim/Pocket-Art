package com.pocekt.art.controller;


import com.pocekt.art.dto.request.UserRequestDto;
import com.pocekt.art.dto.response.Response;
import com.pocekt.art.dto.response.ResponseMsg;
import com.pocekt.art.service.EmailService;
import com.pocekt.art.service.KaKaoService;
import com.pocekt.art.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
//@RequestMapping("/api/v1/users")
@RestController
public class UsersController {

    private final UsersService usersService;
    private final Response response;
    private final EmailService emailService;
    private final KaKaoService kaKaoService;

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Validated @RequestBody UserRequestDto.SignUp signUp, Errors errors) {
        // validation check
        if (errors.hasErrors()) {
            return response.invalidFields(ResponseMsg.refineErrors(errors));
        }
        return usersService.signUp(signUp);
    }
    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<?>  kakaoCallback(String code) throws IOException { // Data를 리턴해주는 컨트롤러 함수

        String access_Token = kaKaoService.getAccessToken(code);
        System.out.println(access_Token);

        return usersService.kakaoLogin(access_Token);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody  UserRequestDto.Login login, Errors errors) {
        // validation check
        if (errors.hasErrors()) {
            return response.invalidFields(ResponseMsg.refineErrors(errors));
        }
        return usersService.login(login);
    }


    @PostMapping("/emailConfirm")
    public ResponseEntity<String> emailConfirm(@RequestParam String email) throws Exception {

        String code= emailService.sendSimpleMessage(email);
        return ResponseEntity.status(HttpStatus.OK).body("인증 번호 발급 성공");
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<String> codeConfirm(@RequestParam("email") String email,@RequestParam("code") String code) throws Exception {
        boolean codeMatched = emailService.verifyCode(email, code);

        if (codeMatched) {
            return ResponseEntity.ok("인증번호 일치");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증번호 불일치");
        }

    }

    @PostMapping("/passConfirm")
    public String passConfirm(@RequestParam String email) throws Exception {
        String confirm = emailService.passwordMessage(email);
        System.out.println(confirm);

        String chkpass= usersService.tempPassword(confirm,email);
        return chkpass;
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody UserRequestDto.passwordConfirm password) throws Exception {
        return usersService.updatePassword(password);

    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@Validated UserRequestDto.Reissue reissue, Errors errors) {
        // validation check
        if (errors.hasErrors()) {
            return response.invalidFields(ResponseMsg.refineErrors(errors));
        }
        return usersService.reissue(reissue);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Validated UserRequestDto.Logout logout, Errors errors) {
        // validation check
        if (errors.hasErrors()) {
            return response.invalidFields(ResponseMsg.refineErrors(errors));
        }
        return usersService.logout(logout);
    }

    @GetMapping("/authority")
    public ResponseEntity<?> authority() {
        log.info("ADD ROLE_ADMIN");
        return usersService.authority();
    }

    @GetMapping("/userTest")
    public ResponseEntity<?> userTest() {
        log.info("ROLE_USER TEST");
        return response.success();
    }

    @GetMapping("/adminTest")
    public ResponseEntity<?> adminTest() {
        log.info("ROLE_ADMIN TEST");
        return response.success();
    }
}

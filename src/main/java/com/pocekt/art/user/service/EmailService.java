package com.pocekt.art.user.service;

public interface EmailService {
    String sendSimpleMessage(String to)throws Exception;
    String passwordMessage(String email) throws Exception;


    boolean verifyCode(String email, String code);
}
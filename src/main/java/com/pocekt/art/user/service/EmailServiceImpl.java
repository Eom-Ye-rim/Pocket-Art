package com.pocekt.art.user.service;

import com.pocekt.art.user.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    JavaMailSender emailSender;

    public  String eCode;
    // 코드 저장을 위한 temporary storage (e.g. HashMap or a database)
    private Map<String, String> verificationCodes=new HashMap<>();
    public static final String ePw= getTempPassword();


    private MimeMessage createMessage(String to)throws Exception{
        String eCode = createKey();
        verificationCodes.put(to,eCode);
        System.out.println(verificationCodes);
        System.out.println("보내는 대상 : "+ to);
        System.out.println("인증 번호 : "+ eCode);
        MimeMessage  message = emailSender.createMimeMessage();
        message.addRecipients(RecipientType.TO, to);//보내는 대상
        message.setSubject("이메일 인증 테스트");//제목
        String msgg="";
        msgg+= "<div style='margin:20px;'>";
        msgg+= "<h1> Poket Art </h1>";
        msgg+= "<br>";
        msgg+= "<p>Please copy and enter the code below<p>";
        msgg+= "<br>";
        msgg+= "<p>Thank you.<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= eCode +"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("yerim110324@gmail.com","Eom-ye-rim"));//보내는 사람
        return message;
    }


    private MimeMessage createPassword(String email)throws Exception{
        System.out.println("보내는 대상 : "+ email);
        System.out.println("임시 비밀번호 : "+ ePw);
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(RecipientType.TO, email);//보내는 대상
        message.setSubject("임시 비밀번호 발급");//제목
        String msgg="";
        msgg+= "<div style='margin:20px;'>";
        msgg+= "<h1> Poket Art </h1>";
        msgg+= "<br>";
        msgg+= "<p>임시 비밀번호입니다. 로그인 후 새로운 비밀번호로 변경해주세요.<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다.<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>임시 비밀번호</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= ePw +"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("yerim110324@gmail.com","Eom-ye-rim"));//보내는 사람
        return message;
    }


    public boolean verifyCode(String email, String code){
        String savedCode=verificationCodes.get(email);
        return code.equals(savedCode);
    }
    public static String getTempPassword(){
        char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        String str = "";

        // 문자 배열 길이의 값을 랜덤으로 10개를 뽑아 구문을 작성함
        int idx = 0;
        for (int i = 0; i < 10; i++) {
            idx = (int) (charSet.length * Math.random());
            str += charSet[idx];
        }
        return str;
    }



    public static String createKey() {
        StringBuffer key = new StringBuffer();
        //Random rnd = new Random();
        Random random = new Random();
        // Generate and send 4 random numbers
        for (int i = 0; i < 4; i++) {
            key.append(random.nextInt(10));

        }
        return key.toString();
    }


    @Override
    public String sendSimpleMessage(String to)throws Exception {
        // TODO Auto-generated method stub
        MimeMessage message = createMessage(to);
        try{//예외처리

            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return eCode;
    }

    @Override
    public String passwordMessage(String email) throws Exception {
        MimeMessage message = createPassword(email);
        try{//예외처리
            emailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }

        return ePw;

    }
}
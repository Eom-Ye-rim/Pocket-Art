
package com.pocekt.art.controller;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sketch")
public class SketchController {

    public static void main(String[] args) {
        // OpenCV 로드
        OpenCV.loadLocally();
        System.out.println("OpenCV 로드됨");
    }

    @PostMapping("")
    public void processImage() {
        try {
            double threshold = 150;
            double threshold1 = threshold;
            double threshold2 = threshold * 2.5;

            String imagePath = "/home/ubuntu/23_HI053/models/castle-7766794_1920.jpg";
            System.out.println("이미지 파일: " + imagePath);

            // 이미지 파일이 존재하는지 확인
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                System.out.println("이미지 파일이 존재합니다: " + imagePath);
                // 이미지 파일이 존재할 경우 이미지 로드
                BufferedImage image = loadImage(imagePath);
                Mat M_image = img2Mat(image);

                // 그레이스케일
                Imgproc.cvtColor(M_image, M_image, Imgproc.COLOR_RGB2GRAY);

                // Canny 에지 검출 시작
                Mat canny_img = new Mat();
                Imgproc.Canny(M_image, canny_img, threshold1, threshold2, 3, false);
                // Canny 에지 검출 끝

                // 이미지 처리 로직을 계속하세요.
            } else {
                // 이미지 파일이 존재하지 않는 경우 처리
                System.out.println("이미지 파일이 존재하지 않습니다: " + imagePath);
                // 예외를 throw하거나 오류 메시지를 기록하거나 적절한 조치를 취할 수 있습니다.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage loadImage(String imagePath) throws IOException {
        return ImageIO.read(Paths.get(imagePath).toFile());
    }

    public static Mat img2Mat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }
}


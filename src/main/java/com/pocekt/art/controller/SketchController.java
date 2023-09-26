
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
        // Load OpenCV
        OpenCV.loadLocally();
        System.out.println("OpenCV loaded");

        // Call your image processing method
        showCannyNSobelEdgeWithLenna();
    }

    @PostMapping("")
    public static void showCannyNSobelEdgeWithLenna() {
        try {
            OpenCV.loadLocally();
            System.out.println("OpenCV loaded");

            // Call your image processing method
            showCannyNSobelEdgeWithLenna();
            double threshold = 150;
            double threshold1 = threshold;
            double threshold2 = threshold * 2.5;

            String imagePath = "/home/ubuntu/23_HI053/models/castle-7766794_1920.jpg";
            System.out.println("Image file : " + imagePath);

            // Check if the image file exists
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                System.out.println("Image file exists: " + imagePath);
                // Load the image only if it exists
                BufferedImage image = loadImage(imagePath);
                Mat M_image = img2Mat(image);

                // GrayScale
                Imgproc.cvtColor(M_image, M_image, Imgproc.COLOR_RGB2GRAY);

                // Start Canny Edge
                Mat canny_img = new Mat();
                Imgproc.Canny(M_image, canny_img, threshold1, threshold2, 3, false);
                // End Canny Edge

                // Continue with your image processing logic
            } else {
                // Handle the case where the image file does not exist
                System.out.println("Image file does not exist: " + imagePath);
                // You can throw an exception or log an error message, or take appropriate action.
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

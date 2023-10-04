
package com.pocekt.art.controller;





import com.pocekt.art.dto.request.UserRequestDto;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/v1/sketch")
class SketchController {
    @PostMapping("")

    public static String sketch(@RequestParam("file") MultipartFile file) {
        File outputImageFile = null;
        try {
            // Load the input image

            // Convert MultipartFile to BufferedImage
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Create a grayscale image
            BufferedImage grayImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            // Convert the input image to grayscale
            for (int x = 0; x < inputImage.getWidth(); x++) {
                for (int y = 0; y < inputImage.getHeight(); y++) {
                    Color color = new Color(inputImage.getRGB(x, y));
                    int grayValue = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                    grayImage.setRGB(x, y, new Color(grayValue, grayValue, grayValue).getRGB());
                }
            }

            // Create a new image to store the edges
            BufferedImage edgeImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            // Apply the Sobel operator for edge detection
            int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
            int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

            for (int x = 1; x < inputImage.getWidth() - 1; x++) {
                for (int y = 1; y < inputImage.getHeight() - 1; y++) {
                    int gradientX = 0;
                    int gradientY = 0;

                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            int pixelValue = new Color(grayImage.getRGB(x + i, y + j)).getRed();
                            gradientX += sobelX[i + 1][j + 1] * pixelValue;
                            gradientY += sobelY[i + 1][j + 1] * pixelValue;
                        }
                    }

                    int gradientMagnitude = (int) Math.sqrt(gradientX * gradientX + gradientY * gradientY);

                    // Clamp the gradient magnitude to the valid range (0 to 255)
                    gradientMagnitude = Math.max(0, Math.min(255, gradientMagnitude));

                    // Separate the RGB channels
                    int redChannel = gradientMagnitude;
                    int greenChannel = gradientMagnitude;
                    int blueChannel = gradientMagnitude;

                    // Set the pixel color using the clamped color channels
                    edgeImage.setRGB(x, y, new Color(redChannel, greenChannel, blueChannel).getRGB());
                }
            }

            // Save the edge-detected image
            outputImageFile = new File("/home/ubuntu/23_HI053/sketch/"+file.getOriginalFilename());
            System.out.println(outputImageFile.getName());
            System.out.println(outputImageFile.getPath());
            ImageIO.write(edgeImage, "jpg", outputImageFile);
            System.out.println("Edge detection completed and saved as edge_image.jpg");
           
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputImageFile.getPath();
    }
}

//    public void showCannyNSobelEdgeWithLenna() {
//        try {ㅣ
//            double threshold = 150;
//            double threshold1 = threshold;
//            double threshold2 = threshold * 2.5;
//            String imagePath = "/Users/eom-yelim/23_HI053/models/castle-7766794_1920.jpg";
//
//            BufferedImage image = loadImage(imagePath);
//            Mat M_image = img2Mat(image);
//
//            // GrayScale
//            Imgproc.cvtColor(M_image, M_image, Imgproc.COLOR_RGB2GRAY);
//
//            // Start Canny Edge
//            Mat canny_img = new Mat();
//            Imgproc.Canny(M_image, canny_img, threshold1, threshold2, 3, false);
//            // End Canny Edge
//
//            // Display or save processed images
////            HighGui.imshow("Original Image", M_image);
////            HighGui.imshow("Canny Edge Image", canny_img);
////            HighGui.waitKey();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private BufferedImage loadImage(String imagePath) throws IOException {
//        return ImageIO.read(Paths.get(imagePath).toFile());
//    }
//
//    public Mat img2Mat(BufferedImage image) {
//        Mat mat = new Mat();
//        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        if (image != null && image.getHeight() > 0 && image.getWidth() > 0) {
//            mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
//            mat.put(0, 0, data);
//        } else {
//            System.err.println("Invalid image dimensions or null image.");
//        }
//        return mat;
//    }
//}

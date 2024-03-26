
package com.pocekt.art.controller;





import com.pocekt.art.dto.response.Response;
import com.pocekt.art.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


@RestController
@RequestMapping("/api/v1/sketch")
@RequiredArgsConstructor
class SketchController {
    private final Response response;
    private final S3Service s3Service;
    @PostMapping("")
    public ResponseEntity<?> sketch(@RequestParam("file") MultipartFile file) throws IOException {
        File outputImageFile = null;
        String test="";
            try {

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

                        // Set the pixel color using the clamped gradient magnitude
                        int invertedColor = new Color(gradientMagnitude, gradientMagnitude, gradientMagnitude).getRGB();
                        edgeImage.setRGB(x, y, invertedColor);
                    }
                }


                outputImageFile = new File("/home/ubuntu/23_HI053/sketch/"+file.getOriginalFilename());

// Create a new image with white background
                BufferedImage invertedImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

                for (int x = 0; x < inputImage.getWidth(); x++) {
                    for (int y = 0; y < inputImage.getHeight(); y++) {
                        // Get the gradient magnitude
                        int gradientMagnitude = new Color(edgeImage.getRGB(x, y)).getRed();

                        // Invert the gradient magnitude (0 to 255) to represent black lines on white background
                        gradientMagnitude = 255 - gradientMagnitude;

                        // Set the pixel color using the inverted gradient magnitude
                        int invertedColor = new Color(gradientMagnitude, gradientMagnitude, gradientMagnitude).getRGB();
                        invertedImage.setRGB(x, y, invertedColor);
                    }
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(invertedImage, "jpg", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                String s3DestinationPath = "sketch/"+outputImageFile.getName();
                test = s3Service.upload(imageBytes, s3DestinationPath);

           
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.success(test, "스케치 변환 성공", HttpStatus.OK);


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

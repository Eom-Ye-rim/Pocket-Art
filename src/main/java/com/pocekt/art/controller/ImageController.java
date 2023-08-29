package com.pocekt.art.controller;


import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.pocekt.art.dto.request.TransformedImageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;


@RestController
@RequestMapping("/api/v1/image")

public class ImageController {

    @GetMapping("")
    public static ResponseEntity<TransformedImageDTO> main(String[] args) throws ModelNotFoundException, MalformedModelException {
        String modelPath = "gogh_08.pt";
        String imagePath = "115.png";
        int targetWidth = 256;
        int targetHeight = 256;
        //ImageIO.setUseCache(false);
        File modelFile = new File(modelPath);
        // Check if the file exists
        if (modelFile.exists()) {
            System.out.println("Model file exists.");
            // Proceed with loading and using the model
        } else {
            System.err.println("Model file does not exist at the specified path: " + modelPath);
            // Handle the case where the file doesn't exist
        }


        Criteria<NDList, NDList> criteria = Criteria.builder()
                .setTypes(NDList.class, NDList.class)
                .optModelPath(Paths.get(modelPath))
                .optDevice(Device.cpu())
                .optOption("mapLocation", "true")
                .build();

        try (ZooModel<NDList, NDList> model = criteria.loadModel();) {

            // Create a Translator for resizing the input image
            Translator<BufferedImage, float[]> translator = new ImageTranslator(targetWidth, targetHeight);

            // Create a Predictor for inference
            try (Predictor<BufferedImage, float[]> predictor = model.newPredictor(translator)) {

                // Load and resize the input image
                BufferedImage image = loadImage(imagePath);

                BufferedImage t_image = transposeImage(image);

                BufferedImage resizedImage = resizeImage(t_image, targetWidth, targetHeight);


                //모델 예측
                float[] result = predictor.predict(resizedImage);


                //normalize 역과정
                for (int i = 0; i < result.length; i++) {
                    result[i] *= 0.5;
                    result[i] += 0.5;
                    result[i] *= 255;
                }


                System.out.println(Arrays.toString(result));

                //형 변환
                BufferedImage output = getImageFromFloatArray(result, targetWidth, targetHeight);
                System.out.println("test"+output);
                TransformedImageDTO transformedImageDTO = new TransformedImageDTO();
                transformedImageDTO.setWidth(targetWidth);
                transformedImageDTO.setHeight(targetHeight);
                transformedImageDTO.setData(result);

                return ResponseEntity.ok(transformedImageDTO);


                /*
                BufferedImage resizedImage2 = resizeImage(image, targetWidth, targetHeight);
              //모델 예측
                float[] result2 = predictor.predict(resizedImage2);

                //normalize 역과정
                for (int i = 0; i < result.length; i++) {
                   result2[i] *= 0.5;
                   result2[i] += 0.5;
                   result2[i] *= 255;
                }

                BufferedImage output2 = getImageFromFloatArray(result2, targetWidth, targetHeight);

                */


                //시각화
//                JFrame frame = new JFrame();
//                frame.getContentPane().setLayout(new FlowLayout());
//                //frame.getContentPane().add(new JLabel(new ImageIcon(image)));
//                frame.getContentPane().add(new JLabel(new ImageIcon(t_image)));
//                frame.getContentPane().add(new JLabel(new ImageIcon(resizedImage)));
//                frame.getContentPane().add(new JLabel(new ImageIcon(output)));
//                //frame.getContentPane().add(new JLabel(new ImageIcon(output2)));
//                frame.pack();
//                frame.setVisible(true);


            }
        } catch (IOException  | TranslateException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static BufferedImage getImageFromFloatArray(float[] data, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
       System.out.println("Image pixel array size: "
                       + ((DataBufferInt) img.getRaster().getDataBuffer())
                               .getData().length);
       System.out.println("Datasize: " + data.length);
        WritableRaster raster = img.getRaster();
        raster.setPixels(0, 0, w, h, data);
        return img;
    }

    private static BufferedImage loadImage(String imagePath) throws IOException {
        return ImageIO.read(Paths.get(imagePath).toFile());
    }




    public static BufferedImage transposeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Calculate the size of the square crop based on the shorter side
        int size = Math.min(width, height);

        // Calculate the crop coordinates for centering
        int cropX = (width - size) / 2;
        int cropY = (height - size) / 2;

        BufferedImage croppedImage = new BufferedImage(size, size, image.getType());
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(image, 0, 0, size, size, cropX, cropY, cropX + size, cropY + size, null);
        g.dispose();

        BufferedImage transposedImage = new BufferedImage(size, size, image.getType());
        Graphics2D g2 = transposedImage.createGraphics();
        g2.drawImage(croppedImage, 0, 0, null);
        g2.dispose();

        return transposedImage;
    }


    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resizedImage;
    }


    private static class ImageTranslator implements Translator<BufferedImage, float[]> {
        private int targetWidth;
        private int targetHeight;

        public ImageTranslator(int targetWidth, int targetHeight) {
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
        }

        @Override
        public NDList processInput(TranslatorContext ctx, BufferedImage image) {
            NDArray array = ImageFactory.getInstance()
                    .fromImage(image)
                    .toNDArray(ctx.getNDManager())
                    .toType(DataType.FLOAT32, true)
                    .divi(255)
                    .sub(0.5)
                    .divi(0.5);


            //System.out.println(Arrays.toString(array.toArray()));

            return new NDList(array.transpose(2,0,1));
        }
        @Override
        public float[] processOutput(TranslatorContext ctx, NDList list) {

            return list.get(0).transpose(1,2,0).toFloatArray();
        }
    }
}
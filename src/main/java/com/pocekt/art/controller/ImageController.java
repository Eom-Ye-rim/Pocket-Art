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
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.pocekt.art.dto.request.TransformedImageDTO;
import com.pocekt.art.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")

public class ImageController {

    private final S3Service s3Service;
    @PostMapping("")
    public ResponseEntity<TransformedImageDTO> goghmodel(@RequestParam String modelname, @RequestPart MultipartFile file) {
        String modelPath="";
        String imagePath = "";
        if (modelname.equals("gogh")){

            modelPath = "/home/ubuntu/23_HI053/models/gogh_08.pt";

        }
        if (modelname.equals("monet")){
            modelPath = "/home/ubuntu/23_HI053/models/monet_08_23.pt";
        }
        if (modelname.equals("edgar")){
            modelPath = "/home/ubuntu/23_HI053/models/edgar_09_02.pt";
        }
        if (modelname.equals("east1")){
            modelPath = "/home/ubuntu/23_HI053/models/east_08_23.pt";

        }
        if (modelname.equals("east2")){
            modelPath = "/home/ubuntu/23_HI053/models/ink_and_wash_09_29.pt";

        }
        if (modelname.equals("cezanne")){
            modelPath = "/home/ubuntu/23_HI053/models/cezanne_08_23.pt";
        }


        if (!file.isEmpty()) {
            try {
                // Define the directory where you want to save the uploaded file
                String uploadDirectory = "/home/ubuntu/23_HI053/models/";
                String convertedImageFileName = file.getOriginalFilename(); // Use the original filename for the converted image

                String convertedImagePath = Paths.get(uploadDirectory, convertedImageFileName).toString();
                String fileName = file.getOriginalFilename();
                Path filePath = Path.of(uploadDirectory, fileName);

                // Save the uploaded file to the temporary directory
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Set imagePath to the path of the saved file
                imagePath = filePath.toString();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception appropriately
            }
        } else {
            // Handle the case where no file was uploaded
        }
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

                //형 변환
                String uploadDirectory2= "/home/ubuntu/23_HI053/models/";
                BufferedImage output = getImageFromFloatArray(result, targetWidth, targetHeight);

                String outputPath = Paths.get(uploadDirectory2, file.getOriginalFilename()).toString();
                byte[] imageBytes = serializeImage(output);
                String s3DestinationPath = file.getOriginalFilename();
                String path =s3Service.upload(imageBytes, s3DestinationPath);


                saveImageAsPng(output, outputPath);


                // Upload the generated image to S3

                System.out.println(imagePath);
                System.out.println(output);

                TransformedImageDTO transformedImageDTO = new TransformedImageDTO();
                transformedImageDTO.setWidth(targetWidth);
                transformedImageDTO.setHeight(targetHeight);
                transformedImageDTO.setData(imageBytes);
                transformedImageDTO.setUrl(path);

                return ResponseEntity.ok(transformedImageDTO);

            }
        } catch (IOException | TranslateException | ModelNotFoundException | MalformedModelException |
                 ImageProcessingException | MetadataException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static byte[] serializeImage(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream); // Save as PNG (or other format)
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage deserializeImage(byte[] imageBytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    private static BufferedImage loadImage(String imagePath) throws IOException, ImageProcessingException, MetadataException {
        BufferedImage bufferedImage = ImageIO.read(Paths.get(imagePath).toFile());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        Metadata metadata = ImageMetadataReader.readMetadata(Paths.get(imagePath).toFile());

        ExifIFD0Directory exifIFD0 =metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        //System.out.println(exifIFD0);

        int orientation = 1;

        if(exifIFD0 != null && exifIFD0.containsTag(ExifIFD0Directory.TAG_ORIENTATION))  {
            orientation = exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        }

        //System.out.println(orientation);

        BufferedImage rotatedImage;

        if(orientation == 6 ) {
            rotatedImage = Scalr.rotate(bufferedImage, Scalr.Rotation.CW_90);
        } else if (orientation == 3) {
            rotatedImage = Scalr.rotate(bufferedImage, Scalr.Rotation.CW_180);
        } else if(orientation == 8) {
            rotatedImage = Scalr.rotate(bufferedImage, Scalr.Rotation.CW_270);
        } else {
            rotatedImage = bufferedImage;
        }

        return rotatedImage;
    }

    private static void saveImageAsPng(BufferedImage image, String filePath) {
        try {
            File output = new File(filePath);

            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }



    public static BufferedImage transposeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Calculate the size of the square crop based on the shorter side
        int size = Math.min(width, height);

        // Calculate the crop coordinates for centering
        int cropX = (width - size) / 2;
        int cropY = (height - size) / 2;

        BufferedImage croppedImage = new BufferedImage(size, size, image.getType()==0?5:image.getType());
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(image, 0, 0, size, size, cropX, cropY, cropX + size, cropY + size, null);
        g.dispose();

        BufferedImage transposedImage = new BufferedImage(size, size, image.getType()==0?5:image.getType());
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
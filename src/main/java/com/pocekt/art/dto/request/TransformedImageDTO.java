package com.pocekt.art.dto.request;

import lombok.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class TransformedImageDTO {
    private int width;
    private int height;
    private byte[] data; // Store the BufferedImage directly
    private String url;

    // Constructors, getters, and setters

    public void setImageData(byte[] image) {
        this.data = image;
    }
}

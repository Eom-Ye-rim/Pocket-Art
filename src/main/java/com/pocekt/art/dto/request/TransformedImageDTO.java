package com.pocekt.art.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransformedImageDTO {
    private int width;
    private int height;
    private float[] data;
}
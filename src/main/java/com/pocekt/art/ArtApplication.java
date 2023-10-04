package com.pocekt.art;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing //Date 적용
public class ArtApplication {

	public static void main(String[] args) {



		SpringApplication.run(ArtApplication.class, args);
	}

}

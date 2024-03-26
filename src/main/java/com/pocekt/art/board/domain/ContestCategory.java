package com.pocekt.art.board.domain;

import lombok.Getter;

public class ContestCategory {

    @Getter
    public enum Category {
        ALL("ALL"),AI("AI"),USER("USER");
        private final String catagoryName;

        Category(String catagoryName){this.catagoryName=catagoryName;}

    }

    @Getter
    public enum Paint {
        동양풍("동양풍"),서양풍("서양풍");
        private final String style ;

        Paint(String style){this.style=style;}
    }

}

package com.ecm2425.newsapp;

public class Article {
    private String title;
    private String URL;
    private String newsOutlet;
    private String pictureURL;


    /**
     * The constructor for the article class. It takes 4 arguments.
     * @param title
     * @param URL
     * @param source
     * @param pictureURL
     */
    Article(String title, String URL, String source,String pictureURL){
        this.title = title;
        this.URL = URL;
        this.newsOutlet = source;
        this.pictureURL = pictureURL;


    }

    public String getTitle() {
        return title;
    }

    public String getURL() {
        return URL;
    }

    public String getNewsOutlet() {
        return newsOutlet;
    }

    public String getPictureURL() {
        return pictureURL;
    }

}

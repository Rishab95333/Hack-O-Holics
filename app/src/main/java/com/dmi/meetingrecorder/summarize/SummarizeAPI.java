package com.dmi.meetingrecorder.summarize;


import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by Karan on 25/2/16.
 */
public interface SummarizeAPI {
    @Headers({
            "Accept: application/json","Content-Type: application/x-www-form-urlencoded"
            ,"X-Mashape-Key: 7uhwOnM7bdmsh1vijmU9KiIym980p1RqeVpjsnw4cIyCj0BtkK"
    })
    @FormUrlEncoded
    @POST("/text-summarizer-text")
    Call<SummarizedTextModel> getSummarizedText(@Field("text") String text, @Field("sentnum") int sentnum);

//    @GET("/v2")
//    Call<TranslatedTextModel> getTranslatedText(@Query("key") String key,@Query("source") String source,@Query("target") String target,@Query("q") String q);
    
}

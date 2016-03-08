package com.example.rxtest;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import rx.Observable;

public interface GoogleService {
    @GET("/")
    Observable<ResponseBody> top();
}

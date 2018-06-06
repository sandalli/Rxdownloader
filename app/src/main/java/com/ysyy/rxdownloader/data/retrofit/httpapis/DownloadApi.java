package com.ysyy.rxdownloader.data.retrofit.httpapis;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Auther: RyanLi
 * Data: 2018-06-06 16:14
 * Description: 文件下载api
 */
public interface DownloadApi {

    @GET
    @Streaming
    Observable<Response<ResponseBody>> download(@Header("range") String range, @Url String url);


}

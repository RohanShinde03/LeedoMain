package com.tribeappsoft.leedo.api;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class ApiClient
{

    private static ApiClient apiClient;

    private ApiService apiService;
    //    private ApiService sessionApiService;


    public static ApiClient getInstance() {
        if(apiClient == null) {
            apiClient = new ApiClient();
        }
        return apiClient;
    }

    public ApiClient()
    {

        try
        {

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                    .enableComplexMapKeySerialization()
                    .setPrettyPrinting()
                    .setLenient()
                    .create();

//        OkHttpClient client = new OkHttpClient();
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        client.interceptors().add(loggingInterceptor);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
            httpClient.addInterceptor(logging);
            httpClient.connectTimeout(60, TimeUnit.SECONDS);
            httpClient.readTimeout(60, TimeUnit.SECONDS);
            httpClient.writeTimeout(60, TimeUnit.SECONDS);
           /* httpClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    //return null;
                    return onOnIntercept(chain);
                }
            });*/



            Retrofit retrofit = new Retrofit.Builder()
                    .client(httpClient.build())
                    .baseUrl(WebServer.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }




//        Retrofit sessionRetrofit = new Retrofit.Builder()
//                .baseUrl(SyncStateContract.Constants.BASE_API_URL)
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .client(createRequestInterceptorClient())
//                .build();


//        sessionApiService = sessionRetrofit.create(ApiService.class);
    }

    public ApiService getApiService() {
        return apiService;
    }

//    public ApiService getSessionApiService() {
//        return sessionApiService;
//    }

//    private OkHttpClient createRequestInterceptorClient() {
//        // Set up system-wide CookieHandler to capture all cookies sent from server.
//        final CookieManager cookieManager = new CookieManager();
//        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//        CookieHandler.setDefault(cookieManager);
//
//        // Set up clientinterceptor to include cookie value in the header.
//        OkHttpClient client = new OkHttpClient();
//        client.interceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request original = chain.request();
//
//                // Customize the request
//                Request request = original.newBuilder()
//                        .addHeader(HEADER_API_TOKEN, "8915ef28cf2a154a74e59ce6aada52d0da969126a32d5455")
//                        .build();
//
//                Response response = chain.proceed(request);
//                return response;
//            }
//        });
//
//        return client;
//    }


    private Response onOnIntercept(Interceptor.Chain chain) throws IOException
    {
        try
        {
            Response response = chain.proceed(chain.request());
            //String content = UtilityMethods.convertResponseToString(response);
            String content = response.toString();
            Log.d(TAG, "lastAPICall" + " - " + content);
            return response.newBuilder().body(ResponseBody.create(response.body().contentType(), content)).build();
        }
        catch (SocketTimeoutException exception)
        {
            exception.printStackTrace();
            //if(listener != null)
              //  listener.onConnectionTimeout();
        }

        return chain.proceed(chain.request());
    }



}

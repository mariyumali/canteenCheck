package com.example.canteenchecker.canteenmanager.proxy;

import android.app.Application;

import com.example.canteenchecker.canteenmanager.CanteenManagerApplication;
import com.example.canteenchecker.canteenmanager.domain.Canteen;
import com.example.canteenchecker.canteenmanager.domain.Rating;
import com.example.canteenchecker.canteenmanager.domain.ReviewData;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class ServiceProxy {

    private static final String SERVICE_BASE_URL = "https://canteenchecker.azurewebsites.net/";
    private static final long ARTIFICIAL_DELAY = 5;

    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
        .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // if authenticated -> add authorization header
                if (CanteenManagerApplication.getInstance().isAuthenticated()) {
                    Request.Builder requestBuilder = original.newBuilder();
                    requestBuilder.addHeader("Authorization",
                            "Bearer " + CanteenManagerApplication
                                    .getInstance()
                                    .getAuthenticationToken());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
                // otherwise resume with original request
                return chain.proceed(original);
            }
        });


    private final Proxy proxy = new Retrofit.Builder()
            .baseUrl(SERVICE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Proxy.class);


    private void causeDelay() {
        try {
            Thread.sleep(ARTIFICIAL_DELAY);
        } catch (InterruptedException ignored) {
        }
    }


    public String Login(String username, String password) throws IOException {
        causeDelay();
        return  proxy.postLogin(new ProxyLogin(username, password)).execute().body();
    }



    private interface Proxy {

        @POST("/Admin/Login")
        Call<String> postLogin(@Body ProxyLogin login);

//        @GET("/Public/Canteen/{id}")
//        Call<ProxyCanteen> getCanteen(@Path("id") String canteenId);
//
//        @PUT("/Public/Canteen")
//        Call<Void> updateCanteen(@Body ProxyCanteen proxyCanteen);
//
//        @GET("/Public/Canteen/{id}/Rating?nrOfRatings=0")
//        Call<ProxyReviewData> getReviewDataForCanteen(@Path("id") String canteenId);

        //@POST("/Admin/Canteen/Rating")
        //Call<ProxyRating> postRating(@Header("Authorization") String authenticationToken, @Body ProxyNewRating rating);

    }

    private static class ProxyCanteen {
        int canteenId;
        String name;
        String meal;
        float mealPrice;
        String website;
        String phone;
        String address;
        float averageRating;
        int averageWaitingTime;
        List<Rating> ratingList;

        Canteen toCanteen() {
            return new Canteen(String.valueOf(canteenId), name, phone, website, meal, mealPrice, averageRating, address, averageWaitingTime, ratingList);
        }
    }

    private static class ProxyReviewData {
        float average;
        //int count;
        int totalCount;
        //ProxyRating[] ratings;
        int[] countsPerGrade;

        private int getRatingsForGrade(int grade) {
            grade--;
            return countsPerGrade != null && grade >= 0 && grade < countsPerGrade.length ? countsPerGrade[grade] : 0;
        }

        ReviewData toReviewData() {
            return new ReviewData(average, totalCount, getRatingsForGrade(1), getRatingsForGrade(2), getRatingsForGrade(3), getRatingsForGrade(4), getRatingsForGrade(5));
        }
    }

    private static class ProxyRating {

        int ratingId;
        String username;
        String remark;
        int ratingPoints;
        long timestamp;

    }

    private static class ProxyLogin {
        final String username;
        final String password;

        ProxyLogin(String userName, String password) {
            this.username = userName;
            this.password = password;
        }
    }

}
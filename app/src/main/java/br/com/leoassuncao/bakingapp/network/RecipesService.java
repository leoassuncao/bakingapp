package br.com.leoassuncao.bakingapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.leoassuncao.bakingapp.network.ServiceApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by leonardo.filho on 02/05/2018.
 */

public final class RecipesService {

    private static ServiceApi retrofit;
    private static final String API_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/";


    public static  ServiceApi getApi( ) {

        Gson gson = new GsonBuilder().create();
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build())
                .build().create(ServiceApi.class);

        return retrofit;
    }

}

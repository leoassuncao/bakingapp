package br.com.leoassuncao.bakingapp.network;

import java.util.ArrayList;
import java.util.List;

import br.com.leoassuncao.bakingapp.pojo.Recipe;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by leonardo.filho on 02/05/2018.
 */

public interface ServiceApi {

    @GET("baking.json")
    Call<ArrayList<Recipe>> getRecipe();
}

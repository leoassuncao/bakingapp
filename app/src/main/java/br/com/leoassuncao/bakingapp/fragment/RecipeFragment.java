package br.com.leoassuncao.bakingapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import br.com.leoassuncao.bakingapp.R;
import br.com.leoassuncao.bakingapp.SimpleIdlingResource;
import br.com.leoassuncao.bakingapp.activities.RecipeActivity;
import br.com.leoassuncao.bakingapp.adapter.RecipeAdapter;
import br.com.leoassuncao.bakingapp.network.RecipesService;
import br.com.leoassuncao.bakingapp.network.ServiceApi;
import br.com.leoassuncao.bakingapp.pojo.Recipe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static br.com.leoassuncao.bakingapp.activities.RecipeActivity.ALL_RECIPES;

/**
 * Created by leonardo.filho on 14/05/2018.
 */

public class RecipeFragment extends Fragment {

    public RecipeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView;

        View rootView = inflater.inflate(R.layout.fragment_recipe, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recipe_recycler);
        final RecipeAdapter recipesAdapter = new RecipeAdapter((RecipeActivity) getActivity());
        recyclerView.setAdapter(recipesAdapter);

        if (rootView.getTag() != null && rootView.getTag().equals("phone-land")) {
            GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 4);
            recyclerView.setLayoutManager(mLayoutManager);
        } else {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
        }

        ServiceApi service = RecipesService.getApi();
        Call<ArrayList<Recipe>> recipe = service.getRecipe();

        final SimpleIdlingResource idlingResource = (SimpleIdlingResource) ((RecipeActivity) getActivity()).getIdlingResource();

        if (idlingResource != null) {
            idlingResource.setIdleState(false);
        }

        recipe.enqueue(new Callback<ArrayList<Recipe>>() {
            @Override
            public void onResponse(Call<ArrayList<Recipe>> call, Response<ArrayList<Recipe>> response) {
                Integer statusCode = response.code();
                Log.v("status code: ", statusCode.toString());

                ArrayList<Recipe> recipes = response.body();

                Bundle recipesBundle = new Bundle();
                recipesBundle.putParcelableArrayList(ALL_RECIPES, recipes);

                recipesAdapter.setRecipeData(recipes, getContext());
                if (idlingResource != null) {
                    idlingResource.setIdleState(true);
                }

            }

            @Override
            public void onFailure(Call<ArrayList<Recipe>> call, Throwable t) {
                Log.v("http fail: ", t.getMessage());
            }
        });

        return rootView;
    }

}

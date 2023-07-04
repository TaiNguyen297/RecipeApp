package com.example.recipeapp.fragement;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.adapter.RecipeAdapter;
import com.example.recipeapp.databinding.FragmentHomeBinding;
import com.example.recipeapp.model.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRecipes();

    }

    private void loadRecipes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }

                loadPopularRecipe(recipes);
                loadFavoriteRecipe(recipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",error.getMessage());
            }
        });
    }

    private void loadPopularRecipe(List<Recipe> recipes) {

        List<Recipe> popularRecipe = new ArrayList<>();
        for(int i = 0;i < 5; i++){
            int random = (int) (Math.random()*recipes.size());
            popularRecipe.add(recipes.get(random));
        }
        binding.rvPopular.setAdapter(new RecipeAdapter());
        RecipeAdapter adapter = (RecipeAdapter) binding.rvPopular.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(popularRecipe);
            adapter.notifyDataSetChanged();
        }
    }

    private void loadFavoriteRecipe(List<Recipe> recipes) {
        List<Recipe> favoriteRecipe = new ArrayList<>();
        for(int i = 0;i < 5; i++){
            int random = (int) (Math.random()*recipes.size());
            favoriteRecipe.add(recipes.get(random));
        }
        binding.rvFavorite.setAdapter(new RecipeAdapter());
        RecipeAdapter adapter = (RecipeAdapter) binding.rvFavorite.getAdapter();

        if (adapter != null) {
            adapter.setRecipeList(favoriteRecipe);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }
}
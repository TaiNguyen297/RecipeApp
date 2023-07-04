package com.example.recipeapp.model;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String id, name, image, description, category, calories, time;
    private String authorId;

    private String step, ingredients;

    public Recipe(){

    }
    public Recipe(String name, String time,String calories, String category,String description, String image, String authorId, String step, String ingredients) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.calories = calories;
        this.time = time;
        this.image = image;
        this.authorId = authorId;
        this.step = step;
        this.ingredients = ingredients;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
}

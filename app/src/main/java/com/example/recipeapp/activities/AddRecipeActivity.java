package com.example.recipeapp.activities;

import static java.lang.System.currentTimeMillis;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.recipeapp.R;
import com.example.recipeapp.databinding.ActivityAddRecipeBinding;
import com.example.recipeapp.model.Category;
import com.example.recipeapp.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddRecipeActivity extends AppCompatActivity {

    ActivityAddRecipeBinding binding;
    private boolean isImageSelected = false;
    private ProgressDialog dialog;

    boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadCategory();
        binding.btnAddRecipe.setOnClickListener(view -> getData());
        binding.imgRecipe.setOnClickListener(v -> pickImage());

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if(isEdit){
            editRecipe();
        }
    }

    private void editRecipe(){
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        isImageSelected = true;
        binding.etRecipeName.setText(recipe.getName());
        binding.etCategory.setText(recipe.getCategory());
        binding.etDes.setText(recipe.getDescription());
        binding.etCookingTime.setText(recipe.getTime());
        binding.etCalories.setText(recipe.getCalories());
        binding.etIngredients.setText(recipe.getIngredients());
        binding.etStep.setText(recipe.getStep());
        Glide
                .with(binding.getRoot().getContext())
                .load(recipe.getImage())
                .centerCrop()
                .placeholder(R.drawable.food_img)
                .into(binding.imgRecipe);
        binding.btnAddRecipe.setText("Update Recipe");
    }

    private void pickImage() {
        PickImageDialog.build(new PickSetup()).show(AddRecipeActivity.this).setOnPickResult(r -> {
            Log.e("","onPickResult: " +r.getUri());
            binding.imgRecipe.setImageBitmap(r.getBitmap());
            binding.imgRecipe.setScaleType(ImageView.ScaleType.CENTER_CROP);
            isImageSelected = true;
        }).setOnPickCancel(() -> Toast.makeText(AddRecipeActivity.this,"Cancelled", Toast.LENGTH_SHORT).show());
    }

    private void loadCategory() {
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        binding.etCategory.setAdapter(adapter);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChildren()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        categories.add(dataSnapshot.getValue(Category.class).getName());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getData() {
        String recipeName = Objects.requireNonNull(binding.etRecipeName.getText()).toString();
        String recipeDes = Objects.requireNonNull(binding.etDes.getText()).toString();
        String cookingTime = Objects.requireNonNull(binding.etCookingTime.getText()).toString();
        String calories = Objects.requireNonNull(binding.etCalories.getText()).toString();
        String ingredients = Objects.requireNonNull(binding.etIngredients.getText()).toString();
        String step = Objects.requireNonNull(binding.etStep.getText()).toString();
        String recipeCategory = binding.etCategory.getText().toString();

        if(recipeName.isEmpty()){
            binding.etRecipeName.setError("Please enter Recipe Name");
        } else if (cookingTime.isEmpty()) {
            binding.etCookingTime.setError("Please enter Cooking Time");
        } else if (calories.isEmpty()) {
            binding.etCalories.setError("Please enter Calories");
        }else if (recipeCategory.isEmpty()) {
            binding.etCategory.setError("Please enter Category");
        } else if (recipeDes.isEmpty()) {
            binding.etDes.setError("Please enter Description");
        } else if (ingredients.isEmpty()) {
            binding.etIngredients.setError("Please enter Ingredients");
        } else if (step.isEmpty()) {
            binding.etStep.setError("Please enter Step");
        } else if (!isImageSelected) {
            Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show();
        } else {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Uploading Recipe...");
            dialog.setCancelable(false);
            dialog.show();
            Recipe recipe = new Recipe(recipeName, recipeDes, cookingTime, recipeCategory, calories,"", FirebaseAuth.getInstance().getUid(),step,ingredients);
            upLoadImage(recipe);

        }

    }

    private String upLoadImage(Recipe recipe) {
        final String [] url = {""};
        binding.imgRecipe.setDrawingCacheEnabled(true);
        Bitmap bitmap = ((BitmapDrawable) binding.imgRecipe.getDrawable()).getBitmap();
        binding.imgRecipe.setDrawingCacheEnabled(false);
        String id = isEdit ? recipe.getId() : currentTimeMillis() + "";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/"+ id + "_recipe.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()){
                throw Objects.requireNonNull(task.getException());
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Uri downloadUri = task.getResult();
                url[0] = downloadUri.toString();
                Toast.makeText(AddRecipeActivity.this,"Image Upload Successfully",Toast.LENGTH_SHORT).show();
                saveDataInDB(recipe, url[0]);
            }else {
                Toast.makeText(AddRecipeActivity.this,"Image Upload Failed",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Log.e("", "onComplete: "+Objects.requireNonNull(task.getException()).getMessage());
            }
        });
        return url[0];
    }

    private void saveDataInDB(Recipe recipe, String url) {
        recipe.setImage(url);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        String id = reference.push().getKey();
        recipe.setId(id);
        if (id != null) {
            reference.child(id).setValue(recipe).addOnCompleteListener(task -> {
                dialog.dismiss();
                if(task.isSuccessful()){
                    if (!isEdit) {
                        Toast.makeText(AddRecipeActivity.this,"Recipe Added Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddRecipeActivity.this,"Recipe Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }else{
                    Toast.makeText(AddRecipeActivity.this,"Recipe Added Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
package com.example.kitchen.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.kitchen.R;
import com.example.kitchen.data.firebase.RecipeViewModel;
import com.example.kitchen.data.local.KitchenViewModel;
import com.example.kitchen.data.local.LocalDatabaseInsertListener;
import com.example.kitchen.data.local.entities.Ingredient;
import com.example.kitchen.data.local.entities.Recipe;
import com.example.kitchen.utility.AppConstants;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity implements RecipeViewModel.RatingPostListener, LocalDatabaseInsertListener {

    private static final String KEY_SERVINGS = "servings-key";
    private static final String KEY_RATING_TRANSACTION = "rating-transaction-status-key";
    private Recipe mRecipe;
    private int mServings;
    private boolean mIsRatingProcessing;
    private AppBarLayout mAppBarLayout;
    private SharedPreferences sharedPref;
    private boolean mIsBookable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        boolean isEditable;
        if (getIntent() != null) {
            mRecipe = getIntent().getParcelableExtra(AppConstants.EXTRA_RECIPE);
            isEditable = getIntent().getBooleanExtra(AppConstants.EXTRA_EDITABLE, false);
            mIsBookable = getIntent().getBooleanExtra(AppConstants.EXTRA_BOOKABLE, false);
        } else {
            return;
        }

        mAppBarLayout = findViewById(R.id.app_bar);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Change ActionBar title as the name of the recipe.
            actionBar.setTitle(mRecipe.title);
        }
        ImageView recipeImageView = findViewById(R.id.iv_recipe_image);
        String url = mRecipe.imagePath;
        if (url != null && url.length() != 0) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.mipmap.ingredients)
                    .error(R.mipmap.ingredients)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(this)
                    .load(url)
                    .apply(options)
                    .into(recipeImageView);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecipeDetailActivity.this, RecipeEditActivity.class);
                intent.putExtra(AppConstants.EXTRA_RECIPE, mRecipe);
                startActivity(intent);
                finish();
            }
        });

        View ratingDivider = findViewById(R.id.divider_rating);
        TextView ratingLabel = findViewById(R.id.label_rating);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        int rating = sharedPref.getInt(mRecipe.publicKey, 0);
        ratingBar.setRating(rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (!mIsRatingProcessing) {
                    mIsRatingProcessing = true;
                    int currentRating = (int) rating;
                    int lastRating = sharedPref.getInt(mRecipe.publicKey, 0);
                    RecipeViewModel viewModel = ViewModelProviders.of(RecipeDetailActivity.this).get(RecipeViewModel.class);
                    viewModel.postRating(mRecipe.publicKey, currentRating, lastRating, RecipeDetailActivity.this);
                }
            }
        });
        if (!isEditable) {
            fab.setVisibility(View.GONE);
            TextView writerView = findViewById(R.id.tv_recipe_writer);
            writerView.setText(mRecipe.writerName);
        } else {
            ratingBar.setVisibility(View.GONE);
            ratingLabel.setVisibility(View.GONE);
            ratingDivider.setVisibility(View.GONE);
        }
        TextView courseTextView = findViewById(R.id.tv_course);
        courseTextView.setText(mRecipe.course);
        TextView cuisineTextView = findViewById(R.id.tv_cuisine);
        cuisineTextView.setText(mRecipe.cuisine);
        TextView prepTimeTextView = findViewById(R.id.tv_prep_time);
        prepTimeTextView.setText(String.valueOf(mRecipe.prepTime));
        TextView cookTimeTextView = findViewById(R.id.tv_cook_time);
        cookTimeTextView.setText(String.valueOf(mRecipe.cookTime));
        if (savedInstanceState == null) {
            mServings = mRecipe.servings;
        } else {
            mServings = savedInstanceState.getInt(KEY_SERVINGS);
            mIsRatingProcessing = savedInstanceState.getBoolean(KEY_RATING_TRANSACTION);
        }
        final TextView servingsTextView = findViewById(R.id.tv_servings);
        servingsTextView.setText(String.valueOf(mServings));
        Button decrementButton = findViewById(R.id.btn_servings_decrement);
        Button incrementButton = findViewById(R.id.btn_servings_increment);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServings > 1) {
                    mServings--;
                    servingsTextView.setText(String.valueOf(mServings));
                }
            }
        });
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServings++;
                servingsTextView.setText(String.valueOf(mServings));
            }
        });

        Button finishedButton = findViewById(R.id.btn_finished);
        finishedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(mAppBarLayout, "Finished.", Snackbar.LENGTH_SHORT).show();
            }
        });

        final LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout ingredientListLayout = findViewById(R.id.container_ingredients);
        final KitchenViewModel kitchenViewModel = ViewModelProviders.of(RecipeDetailActivity.this).get(KitchenViewModel.class);
        kitchenViewModel.getIngredientsByRecipe(mRecipe.id).observe(this, new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(@Nullable List<Ingredient> ingredients) {
                if (ingredients == null)
                    return;
                for (Ingredient ingredient : ingredients) {
                    View ingredientView = inflater.inflate(R.layout.item_ingredient, ingredientListLayout, false);
                    TextView ingredientTextView = ingredientView.findViewById(R.id.tv_ingredient);
                    String text = ingredient.amount + " " + ingredient.amountType + " " + ingredient.food;
                    ingredientTextView.setText(text);
                    ingredientListLayout.addView(ingredientView);
                }
            }
        });

        LinearLayout stepListLayout = findViewById(R.id.container_steps);
        for (int x = 0; x < 3; x++) {
            View stepView = inflater.inflate(R.layout.item_step, stepListLayout, false);
            TextView stepNumberView = stepView.findViewById(R.id.tv_step_number);
            stepNumberView.setText(String.valueOf(x + 1));
            TextView stepTextView = stepView.findViewById(R.id.tv_step);
            stepTextView.setText("Wwwwwwww wwwwwwwwwww wwwwwww");
            stepListLayout.addView(stepView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SERVINGS, mServings);
        outState.putBoolean(KEY_RATING_TRANSACTION, mIsRatingProcessing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (mIsBookable) {
            inflater.inflate(R.menu.menu_recipe_detail_bookable, menu);
        } else {
            inflater.inflate(R.menu.menu_recipe_detail, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.app_bar_board:
                Snackbar.make(mAppBarLayout, "Boarding...", Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.app_bar_bookmark:
                KitchenViewModel viewModel = ViewModelProviders.of(this).get(KitchenViewModel.class);
                viewModel.insertRecipe(mRecipe, this);
                Snackbar.make(mAppBarLayout, R.string.recipe_bookmarked, Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.app_bar_share:
                Snackbar.make(mAppBarLayout, "Sharing...", Snackbar.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRatingTransactionSuccessful(int rating) {
        mIsRatingProcessing = false;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(mRecipe.publicKey, rating);
        editor.apply();
    }

    @Override
    public void onDataInsert(long id) {
        mRecipe.id = (int) id;
    }
}

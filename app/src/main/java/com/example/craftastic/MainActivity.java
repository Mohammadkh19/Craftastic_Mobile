package com.example.craftastic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    ImageView homeIcon, favoritesIcon, adsIcon, accountIcon;
    FloatingActionButton postBtn;
    SearchView searchBar;
    ImageSlider imageSlider;
    ArrayList<Craft> crafts;
    RecyclerView craftRecyclerView;
    HomeAdapter adapter;
    FirebaseUser currentUser;
    private int downX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayouts();
        initImageSlider();
        Navigation.initNavButtons(homeIcon, favoritesIcon, postBtn, adsIcon, accountIcon, this);
        initRecyclerView();
        initSearch();

//        ApiService apiService = RetrofitIntance.getRetrofit().create(ApiService.class);
//        Call<List<Category>> call = apiService.getAllCategories();
//        call.enqueue(new Callback<List<Category>>() {
//            @Override
//            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
//                if (response.isSuccessful()) {
//                    // Handle successful response
//                    Toast.makeText(MainActivity.this, "  fetched categories: " + response.message(), Toast.LENGTH_SHORT).show();
//                    List<Category> categories = response.body();
//                    Log.d("HIIII", categories.toString());
//                } else {
//                    // Handle unsuccessful response
//                    Toast.makeText(MainActivity.this, "Failed to fetch categories: " + response.message(), Toast.LENGTH_SHORT).show();
//                    Log.d("fefe", response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Category>> call, Throwable t) {
//                Log.d("dede", t.getMessage());
//                // Handle network errors
//                Toast.makeText(MainActivity.this, "Network failed categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void initLayouts() {
        homeIcon = findViewById(R.id.homeIcon);
        homeIcon.setColorFilter(ContextCompat.getColor(this, R.color.pink));
        homeIcon.setEnabled(false);
        postBtn = findViewById(R.id.postBtn);
        adsIcon = findViewById(R.id.adsIcon);
        accountIcon = findViewById(R.id.accountIcon);
        favoritesIcon = findViewById(R.id.favoritesIcon);
        searchBar = findViewById(R.id.search_bar);
        imageSlider = findViewById(R.id.imageSlider);
        craftRecyclerView = findViewById(R.id.homeRecyclerView);

        int searchPlateId = searchBar.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchBar.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_background));
        }
    }

    private void initImageSlider() {
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.carousel_2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.carousel_1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.carousel_3, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void initRecyclerView() {
        try {
            CraftsDataSource ds = new CraftsDataSource(this);
            ds.open();
            crafts = ds.getAllCrafts();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                for (Craft craft : crafts) {
                    boolean isFavorite = ds.isCraftFavorited(craft.getCraftId(), currentUser.getUid());
                    craft.setFavorite(isFavorite);
                }
            }
            RecyclerView.LayoutManager layout = new GridLayoutManager(this, 2);
            craftRecyclerView.setLayoutManager(layout);
            adapter = new HomeAdapter(crafts, this);
            initAdapterOnClickListener(adapter);
            craftRecyclerView.setAdapter(adapter);
            ds.close();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initAdapterOnClickListener(HomeAdapter adapter) {
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeAdapter.HomeViewHolder holder = (HomeAdapter.HomeViewHolder) v.getTag();
                int position = holder.getAdapterPosition();
                Intent intent = new Intent(MainActivity.this, PostDetails.class);
                int craftId = crafts.get(position).getCraftId();
                intent.putExtra("craftId", craftId);
                intent.putExtra("LAST_VISITED_ACTIVITY", MainActivity.class.getSimpleName());
                startActivity(intent);
            }
        });
    }

    public void addOrRemoveFavorite(int position) {
        if (LoginRedirecting.redirectToLogin(MainActivity.this)) {
            finish();
        }
        Craft craft = crafts.get(position);
        CraftsDataSource ds = new CraftsDataSource(this);
        try {
            ds.open();
            if (craft.isFavorite()) {
                boolean didDelete = ds.removeFromFavorites(craft.getCraftId(), currentUser.getUid());
                if (didDelete) {
                    craft.setFavorite(false);
                    adapter.notifyItemChanged(position);
                }
            } else {
                boolean didAdd = ds.addToFavorites(craft.getCraftId(), currentUser.getUid());
                if (didAdd) {
                    craft.setFavorite(true);
                    adapter.notifyItemChanged(position);
                }
            }
            ds.close();
        } catch (Exception e) {
            Log.d("Failed", e.getMessage());
        }
    }

    private void initSearch() {
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBar.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getRawX();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                if (Math.abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event);
                }
                final int reducePx = 25;
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                outRect.inset(reducePx, reducePx);
                if (!outRect.contains(x, y)) {
                    v.clearFocus();
                    boolean touchTargetIsEditText = false;

                    for (View vi : v.getRootView().getTouchables()) {
                        if (vi instanceof EditText) {
                            Rect clickedViewRect = new Rect();
                            vi.getGlobalVisibleRect(clickedViewRect);

                            clickedViewRect.inset(reducePx, reducePx);
                            if (clickedViewRect.contains(x, y)) {
                                touchTargetIsEditText = true;
                                break;
                            }
                        }
                    }
                    if (!touchTargetIsEditText) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
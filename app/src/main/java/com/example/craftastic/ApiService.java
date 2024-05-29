package com.example.craftastic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/Categories/getAllCategories")
    Call<List<Category>> getAllCategories();
}

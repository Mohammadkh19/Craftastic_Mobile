package com.example.craftastic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator3;

public class PostDetails extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String phoneNumber;
    ViewPager2 imageViewPager;
    TextView titleTextView, priceTextView, descriptionTextView, locationTextView, emailTextView, sellerNameTextView, conditionTextView;
    Button callBtn, smsBtn;
    Craft currentCraft;
    CircleIndicator3 indicator;
    ImageView backBtn, addToFavoritesBtn;
    FirebaseUser currentUser;
    private static final int PERMISSION_REQUEST_SEND_SMS = 101;
    private static final int PERMISSION_REQUEST_PHONE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error: Map Fragment is null", Toast.LENGTH_SHORT).show();
        }
        initLayouts();
        backBtnNavigation();
        Bundle extra = getIntent().getExtras();
        int craftId = extra.getInt("craftId");
        initDetails(craftId);
        fetchUserDetails();
        addToFavorites();
        initButtonsClick();
    }

    private void initLayouts() {
        titleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        locationTextView = findViewById(R.id.locationTextView);
        emailTextView = findViewById(R.id.emailTextView);
        sellerNameTextView = findViewById(R.id.sellerNameTextView);
        conditionTextView = findViewById(R.id.itemConditionTextView);
        callBtn = findViewById(R.id.callButton);
        smsBtn = findViewById(R.id.messageButton);
        imageViewPager = findViewById(R.id.imageCarousel);
        indicator = findViewById(R.id.indicator);
        backBtn = findViewById(R.id.backArrow);
        addToFavoritesBtn = findViewById(R.id.addToFavorites);
    }

    private void backBtnNavigation(){
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastActivity = getIntent().getStringExtra("LAST_VISITED_ACTIVITY");

                try {
                    Class<?> redirectClass = Class.forName("com.example.craftastic." + lastActivity);
                    Intent intent = new Intent(PostDetails.this, redirectClass);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void addToFavorites(){
        addToFavoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginRedirecting.redirectToLogin(PostDetails.this)) {
                    finish();
                }
                CraftsDataSource ds = new CraftsDataSource(PostDetails.this);
                boolean isSuccessful = false;
                try{
                    ds.open();
                    if(!currentCraft.isFavorite()){
                        isSuccessful = ds.addToFavorites(currentCraft.getCraftId(), currentUser.getUid());
                        if(isSuccessful){
                            currentCraft.setFavorite(true);
                        }
                    }
                    else{
                        isSuccessful = ds.removeFromFavorites(currentCraft.getCraftId(), currentUser.getUid());
                        if(isSuccessful){
                            currentCraft.setFavorite(false);
                        }
                    }
                    ds.close();
                }catch (Exception e){
                    Log.d("Adding TO Favorites", e.getMessage());
                }
                if(isSuccessful && currentCraft.isFavorite()){
                    addToFavoritesBtn.setImageResource(R.drawable.favorite_filled);
                    addToFavoritesBtn.setColorFilter(ContextCompat.getColor(PostDetails.this, R.color.pink), android.graphics.PorterDuff.Mode.SRC_IN);
                }
                else if(isSuccessful && !currentCraft.isFavorite()){
                    addToFavoritesBtn.setImageResource(R.drawable.favorite);
                    addToFavoritesBtn.setColorFilter(ContextCompat.getColor(PostDetails.this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        });
    }

    private void initDetails(int craftId) {
        CraftsDataSource ds = new CraftsDataSource(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        try {
            ds.open();
            currentCraft = ds.getSepecfiedCraft(craftId);
            if(currentUser != null){
                currentCraft.setFavorite(ds.isCraftFavorited(craftId, currentUser.getUid()));
                if(currentCraft.isFavorite()){
                    addToFavoritesBtn.setImageResource(R.drawable.favorite_filled);
                    addToFavoritesBtn.setColorFilter(ContextCompat.getColor(PostDetails.this, R.color.pink), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
            titleTextView.setText(currentCraft.getCraftTitle());
            priceTextView.setText(String.format("$%.2f", currentCraft.getPrice()));
            descriptionTextView.setText(currentCraft.getCraftDescription());
            conditionTextView.setText(currentCraft.getCraftCondition());
            List<byte[]> imageDataList = new ArrayList<>();
            for (CraftImage image : currentCraft.getCraftImages()) {
                imageDataList.add(image.getImageData());
            }
            ImageDetailsAdapter adapter = new ImageDetailsAdapter(imageDataList);
            imageViewPager.setAdapter(adapter);
            indicator.setViewPager(imageViewPager);
            adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

            String address = getAddressFromLatLng(currentCraft.getLatitude(), currentCraft.getLongitude());
            locationTextView.setText(address);
            ds.close();
        } catch (Exception e) {
            Log.d("Failed to Load Craft", e.getMessage());
        }
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(PostDetails.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            return address.getAddressLine(0) ;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapLocation();
    }

    private void updateMapLocation() {
        if (currentCraft != null && mMap != null) {
            LatLng craftLocation = new LatLng(currentCraft.getLatitude(), currentCraft.getLongitude());
            mMap.addMarker(new MarkerOptions().position(craftLocation).title(currentCraft.getCraftTitle()));
            mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(craftLocation, 15));
        }
    }


    public void fetchUserDetails() {
        String userId = currentCraft.getUserId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        sellerNameTextView.setText(documentSnapshot.getString("fullName"));
                        emailTextView.setText(documentSnapshot.getString("email"));
                        phoneNumber = documentSnapshot.getString("phoneNumber");
                    } else {
                        Log.d("FetchEmail", "No such user");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchEmail", "Error getting user details", e);
                });
    }


    private void initButtonsClick(){
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPhonePermission(phoneNumber);
            }
        });

        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSMSPermission(phoneNumber);
            }
        });
    }

    private void checkPhonePermission(String phoneNumber) {
        if(ContextCompat.checkSelfPermission(PostDetails.this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
            callContact(phoneNumber);
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(PostDetails.this, android.Manifest.permission.CALL_PHONE)){
                Snackbar.make(findViewById(R.id.post_details), "", Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(PostDetails.this, new String[]{android.Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_PHONE);
                    }
                }).show();
            }else{
                ActivityCompat.requestPermissions(PostDetails.this, new String[]{android.Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_PHONE);
            }
        }
    }

    private void checkSMSPermission(String phoneNumber) {
        if(ContextCompat.checkSelfPermission(PostDetails.this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            sendMessage(phoneNumber);
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(PostDetails.this, android.Manifest.permission.SEND_SMS)){
                Snackbar.make(findViewById(R.id.post_details), "", Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(PostDetails.this, new String[]{android.Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
                    }
                }).show();
            }else{
                ActivityCompat.requestPermissions(PostDetails.this, new String[]{android.Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
            }
        }
    }

    private void callContact(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(  Uri.parse("tel: " + phoneNumber));
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
            startActivity(intent);
        }
    }

    private void sendMessage(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData( Uri.parse("smsto: " + "+961" + phoneNumber));
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            startActivity(intent);
        }
    }
}
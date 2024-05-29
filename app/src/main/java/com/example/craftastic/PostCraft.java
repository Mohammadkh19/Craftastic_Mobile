package com.example.craftastic;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PostCraft extends AppCompatActivity implements ImageAdapter.OnChangeListener{
    EditText manualLocationEditText, nameEditText, priceEditText, descriptionEditText;
    Button sellItemBtn;
    ImageView toolbarPostCraftBack;
    CardView parentUploadCard, childUploadCard;
    RadioButton useCurrentLocation, enterLocationManually, newConditionRadioBtn, usedConditionRadioBtn;
    RadioGroup locationOptions, conditionRadioGroup;
    LinearLayout uploadPictureLayout;
    RecyclerView imagesRecyclerView;
    ImageAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    Craft currentCraft;
    FirebaseUser currentUser;
    double latitude, longitude;
    boolean isLocationFetched = false;
    String originalAddress = "";
    private int downX;
    final int PERMISSION_REQUEST_CAMERA = 101;
    final int PERMISSION_REQUEST_STORAGE_ACCESS = 102;
    final int PERMISSION_REQUEST_LOCATION_ACCESS = 103;
    ArrayList<Uri> imageUris = new ArrayList<>();
    ArrayList<Uri> originalImageUris = new ArrayList<>();
    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent data = result.getData();
                                Bitmap photo = (Bitmap) data.getExtras().get("data");
                                Uri photoUri = saveImageAndGetUri(photo);
                                imageUris.add(photoUri);
                                updateUIWithImages(imageUris);
                            }
                        }
                    });

    ActivityResultLauncher<Intent> pickImageResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                ClipData clipData = data.getClipData();
                                if (clipData != null) {
                                    for (int i = 0; i < clipData.getItemCount(); i++) {
                                        Uri uri = clipData.getItemAt(i).getUri();
                                        imageUris.add(uri);
                                    }
                                } else if (data.getData() != null) {
                                    imageUris.add(data.getData());
                                }
                                updateUIWithImages(imageUris);
                            }
                        }
                    });

    private Uri saveImageAndGetUri(Bitmap bitmap) {
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(PostCraft.this.getCacheDir(), fileName);
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(imageFile);
    }


    private void updateUIWithImages(ArrayList<Uri> imageUris) {
        if (!imageUris.isEmpty()) {
            childUploadCard.setVisibility(View.GONE);
            imagesRecyclerView.setVisibility(View.VISIBLE);
            setupRecyclerView(imageUris);
            onChange();
        }
    }

    private ArrayList<Uri> convertByteImagesToUris(ArrayList<CraftImage> craftImages) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (CraftImage image : craftImages) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image.getImageData(), 0, image.getImageData().length);
            Uri uri = saveImageAndGetUri(bitmap);
            uris.add(uri);
        }
        return uris;
    }


    private void setupRecyclerView(ArrayList<Uri> imageUris) {
        adapter = new ImageAdapter(this, imageUris, this);
        imagesRecyclerView.setAdapter(adapter);
        initAdapterOnClickListener(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imagesRecyclerView.setLayoutManager(layoutManager);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new DragItemTouchHelper(adapter));
        touchHelper.attachToRecyclerView(imagesRecyclerView);
        checkFieldsForChanges();
    }

    @Override
    public void onChange() {
        checkFieldsForChanges();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_craft);
        if (LoginRedirecting.redirectToLogin(this)) {
            finish();
        }
        initLayouts();
        Bundle extra = getIntent().getExtras();
        if (extra != null && extra.containsKey("CRAFT_ID")) {
            int craftId = extra.getInt("CRAFT_ID");
            initPost(craftId);
            sellItemBtn.setEnabled(false);
        } else {
            currentCraft = new Craft();
        }
        setupWatchers();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        backBtnNavigation();
        initUploadImages();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocation();
        saveCraft();
    }

    private void initPost(int craftId) {
        CraftsDataSource ds = new CraftsDataSource(this);
        try {
            ds.open();
            currentCraft = ds.getSepecfiedCraft(craftId);
            imageUris = convertByteImagesToUris(currentCraft.getCraftImages());
            updateUIWithImages(imageUris);
            originalImageUris = new ArrayList<>(imageUris);
            nameEditText.setText(currentCraft.getCraftTitle());
            priceEditText.setText(String.valueOf(currentCraft.getPrice()));
            descriptionEditText.setText(currentCraft.getCraftDescription());
            enterLocationManually.setChecked(true);
            String address = getAddressFromLatLng(currentCraft.getLatitude(), currentCraft.getLongitude());
            originalAddress = address;
            manualLocationEditText.setVisibility(View.VISIBLE);
            manualLocationEditText.setText(address);
            if (currentCraft.getCraftCondition().equals("New")) {
                newConditionRadioBtn.setChecked(true);
            } else {
                usedConditionRadioBtn.setChecked(true);
            }
            ds.close();
        } catch (Exception e) {
            Log.d("Failed to Load Craft", e.getMessage());
        }
    }

    private void initLayouts() {
        parentUploadCard = findViewById(R.id.parentImageUploadCard);
        childUploadCard = findViewById(R.id.childImageUploadCard);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        uploadPictureLayout = findViewById(R.id.uploadTrigger);
        useCurrentLocation = findViewById(R.id.radioUseCurrentLocation);
        enterLocationManually = findViewById(R.id.radioEnterAddressManually);
        locationOptions = findViewById(R.id.locationOptions);
        manualLocationEditText = findViewById(R.id.editTextManualLocation);
        sellItemBtn = findViewById(R.id.sellBtn);
        conditionRadioGroup = findViewById(R.id.conditionRadioGroup);
        nameEditText = findViewById(R.id.craftTitleEditText);
        descriptionEditText = findViewById(R.id.craftDescriptionEditText);
        priceEditText = findViewById(R.id.craftPriceEditText);
        toolbarPostCraftBack = findViewById(R.id.toolbarPostCraftBack);
        newConditionRadioBtn = findViewById(R.id.newRadioBtn);
        usedConditionRadioBtn = findViewById(R.id.usedRadioBtn);
    }

    private void backBtnNavigation() {
        toolbarPostCraftBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastActivity = getIntent().getStringExtra("LAST_ACTIVITY");
                if(lastActivity == null){
                    lastActivity="MainActivity";
                }
                try {
                    Class<?> redirectClass = Class.forName("com.example.craftastic." + lastActivity);
                    Intent intent = new Intent(PostCraft.this, redirectClass);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initUploadImages() {
        uploadPictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageUploadOptions();
            }
        });
        childUploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageUploadOptions();
            }
        });
    }

    private void showImageUploadOptions() {
        String[] items = {"Take a Picture", "Pick from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    takePicture();

                } else if (which == 1) {
                    pickPicture();

                } else {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void takePicture() {
        if (ContextCompat.checkSelfPermission(PostCraft.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activityResultLauncher.launch(intent);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PostCraft.this, android.Manifest.permission.CAMERA)) {
                Snackbar.make(findViewById(R.id.activity_post_craft),
                                "The app needs permission to take photos",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(PostCraft.this,
                                        new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(PostCraft.this,
                        new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }


    private void pickPicture() {
        if (ContextCompat.checkSelfPermission(PostCraft.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            choosePick();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PostCraft.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(findViewById(R.id.activity_post_craft),
                                "The app needs permission to open Gallery",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(PostCraft.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_ACCESS);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(PostCraft.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_ACCESS);
            }
        }
    }

    private void choosePick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        pickImageResultLauncher.launch(intent);
    }

    private void initAdapterOnClickListener(ImageAdapter adapter) {
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageAdapter.ImageViewHolder holder = (ImageAdapter.ImageViewHolder) v.getTag();
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
                popupMenu.inflate(R.menu.context_menu);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int position = holder.getAdapterPosition();
                    if (item.getItemId() == R.id.cancel_click_image) {
                        popupMenu.dismiss();
                    } else if (item.getItemId() == R.id.remove_click_image) {
                        if (position != RecyclerView.NO_POSITION) {
                            removeImage(position);
                        }
                        return true;
                    }
                    return false;

                });
                popupMenu.show();
            }
        });
    }

    private void removeImage(int position) {
        imageUris.remove(position);
        adapter.notifyItemRemoved(position);
        checkFieldsForChanges();
        if (imageUris.isEmpty()) {
            childUploadCard.setVisibility(View.VISIBLE);
            imagesRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    private void initLocation() {
        locationOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkFieldsForChanges();
                if (checkedId == R.id.radioUseCurrentLocation) {
                    manualLocationEditText.setVisibility(View.GONE);
                    isLocationFetched = false;
                    fetchLocation();
                } else if (checkedId == R.id.radioEnterAddressManually) {
                    manualLocationEditText.setVisibility(View.VISIBLE);
                    if (fusedLocationClient != null && locationCallback != null) {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        });
    }

    private void getLocationFromAddress() {
        String address = manualLocationEditText.getText().toString();
        List<Address> addresses = null;
        Geocoder geo = new Geocoder(PostCraft.this);
        try {
            addresses = geo.getFromLocationName(address, 1);
        } catch (Exception e) {
            Log.d("Manual Address", e.getMessage());
        }

        currentCraft.setLongitude(addresses.get(0).getLongitude());
        currentCraft.setLatitude(addresses.get(0).getLatitude());
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(PostCraft.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            return address.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(findViewById(R.id.activity_post_craft),
                                "Location access is required to use the feature",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(PostCraft.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        PERMISSION_REQUEST_LOCATION_ACCESS);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_LOCATION_ACCESS);
            }
        } else {
            requestNewLocationData();
        }

    }


    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        isLocationFetched = true;
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void setupWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkFieldsForChanges();
            }
        };

        setupRadioGroupWatcher(conditionRadioGroup);
        nameEditText.addTextChangedListener(textWatcher);
        priceEditText.addTextChangedListener(textWatcher);
        descriptionEditText.addTextChangedListener(textWatcher);
        if (enterLocationManually.isChecked()) {
            manualLocationEditText.addTextChangedListener(textWatcher);
        }
    }

    private void setupRadioGroupWatcher(RadioGroup radioGroup) {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkFieldsForChanges();
            }
        });
    }


    private void checkFieldsForChanges() {
        String craftCondition = ((RadioButton) findViewById(conditionRadioGroup.getCheckedRadioButtonId())).getText().toString();

        boolean hasChanged = useCurrentLocation.isChecked() || !nameEditText.getText().toString().equals(currentCraft.getCraftTitle()) ||
                !( !priceEditText.getText().toString().equals("") && Float.parseFloat(priceEditText.getText().toString()) == currentCraft.getPrice()) ||
                !descriptionEditText.getText().toString().equals(currentCraft.getCraftDescription()) || !craftCondition.equals(currentCraft.getCraftCondition()) ||
                haveImageUrisChanged() || (enterLocationManually.isChecked() && !manualLocationEditText.getText().toString().equals(getAddressFromLatLng(currentCraft.getLatitude(), currentCraft.getLongitude())));
        sellItemBtn.setEnabled(hasChanged);
    }

    private boolean haveImageUrisChanged() {
        if (imageUris.size() != originalImageUris.size()) {
            return true;
        }
        for (int i = 0; i < imageUris.size(); i++) {
            if (!imageUris.get(i).equals(originalImageUris.get(i))) {
                return true;
            }
        }
        return false;
    }


    private void saveCraft() {
        sellItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEditText.getText().toString().equals("") || priceEditText.getText().toString().equals("")
                        || descriptionEditText.getText().toString().equals("") || (!enterLocationManually.isChecked() && !useCurrentLocation.isChecked())
                        || (enterLocationManually.isChecked() && manualLocationEditText.getText().toString().equals(""))
                        || imageUris.isEmpty() ) {
                    Toast.makeText(PostCraft.this, "Please Fill All The Fields", Toast.LENGTH_LONG).show();
                    return;
                }
                if(useCurrentLocation.isChecked() && !isLocationFetched){
                    Toast.makeText(PostCraft.this, "Please wait until the location is fetched and try again or enter your address manually", Toast.LENGTH_LONG).show();
                    return;
                }
                String craftCondition = ((RadioButton) findViewById(conditionRadioGroup.getCheckedRadioButtonId())).getText().toString();
                currentCraft.setCraftTitle(nameEditText.getText().toString());
                currentCraft.setCraftDescription(descriptionEditText.getText().toString());
                currentCraft.setPrice(Float.parseFloat(priceEditText.getText().toString()));
                currentCraft.setCraftCondition(craftCondition);
                currentCraft.setUserId(currentUser.getUid());
                if (enterLocationManually.isChecked() && !originalAddress.equals(manualLocationEditText.getText().toString())) {
                    getLocationFromAddress();
                }else if(useCurrentLocation.isChecked()){
                    currentCraft.setLatitude(latitude);
                    currentCraft.setLongitude(longitude);
                }

                boolean isSuccessful = false;
                CraftsDataSource dataSource = new CraftsDataSource(PostCraft.this);
                try {
                    dataSource.open();
                    if (currentCraft.getCraftId() == -1) {
                        isSuccessful = dataSource.insertCraft(currentCraft);
                        int newID = dataSource.getCraftLastID();
                        currentCraft.setCraftId(newID);
                        saveImagesToDatabase(newID, imageUris, dataSource);
                    }
                    else{
                        isSuccessful = dataSource.updateCraft(currentCraft);
                        if(haveImageUrisChanged()){
                            dataSource.deleteCraftImages(currentCraft.getCraftId());
                            saveImagesToDatabase(currentCraft.getCraftId(), imageUris, dataSource);
                        }
                    }
                    dataSource.close();
                } catch (Exception e) {
                    Log.d("Saving Error", e.getMessage());
                }
                if (isSuccessful) {
                    Intent intent = new Intent(PostCraft.this, My_posts.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    private void saveImagesToDatabase(int craftId, ArrayList<Uri> imageUris, CraftsDataSource dataSource) {
        for (Uri uri : imageUris) {
            byte[] imageData = uriToByteArray(uri);
            if (imageData != null) {
                CraftImage currentCraftImage = new CraftImage();
                currentCraftImage.setImageData(imageData);
                currentCraftImage.setCraftId(craftId);
                dataSource.insertImage(currentCraftImage);
            }
        }
    }

    private byte[] uriToByteArray(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e("PostCraft", "Error converting URI to byte array", e);
            return null;
        }
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
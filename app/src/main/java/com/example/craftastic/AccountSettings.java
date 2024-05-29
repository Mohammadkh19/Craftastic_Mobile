package com.example.craftastic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountSettings extends AppCompatActivity implements LogoutDialog.saveLogoutListener{
    ConstraintLayout constraintLayout;
    ImageView homeIcon, favoritesIcon,adsIcon, accountIcon;
    FloatingActionButton postBtn;
    EditText nameEditText, emailEditText, phoneEditText;
    User user;
    TextView saveButton, nameError, emailLabel, emailError, phoneLabel, phoneError;
    Button logout;
    FirebaseUser currentUser;
    private int downX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        if (LoginRedirecting.redirectToLogin(this)) {
            finish();
            return;
        }
        initLayouts();
        fetchUserDetails();
        Navigation.initNavButtons(homeIcon, favoritesIcon, postBtn,adsIcon, accountIcon, this);
        setupTextWatchers();
        initSaveButton();
        initLogout();
    }

    private void initLayouts() {
        accountIcon = findViewById(R.id.accountIcon);
        accountIcon.setColorFilter(ContextCompat.getColor(this, R.color.pink));
        accountIcon.setEnabled(false);
        postBtn = findViewById(R.id.postBtn);
        favoritesIcon = findViewById(R.id.favoritesIcon);
        adsIcon = findViewById(R.id.adsIcon);
        homeIcon = findViewById(R.id.homeIcon);
        nameEditText = findViewById(R.id.nameAccountEditText);
        emailEditText = findViewById(R.id.emailAccountEditText);
        phoneEditText = findViewById(R.id.phoneAccountEditText);
        saveButton = findViewById(R.id.saveAccountButton);
        logout = findViewById(R.id.logoutButton);
        nameError = findViewById(R.id.nameError);
        emailLabel = findViewById(R.id.emailLabel);
        emailError = findViewById(R.id.emailError);
        phoneLabel = findViewById(R.id.phoneLabel);
        phoneError = findViewById(R.id.phoneError);
        constraintLayout = findViewById(R.id.constraintLayoutAccount);
    }

    private void fetchUserDetails() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(currentUserId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String fullName = task.getResult().getString("fullName");
                    String email = task.getResult().getString("email");
                    String phoneNumber = task.getResult().getString("phoneNumber");
                    user = new User(currentUserId, fullName, email, phoneNumber);
                    updateUI(user);
                } else {
                    Toast.makeText(AccountSettings.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUI(User user) {
        nameEditText.setText(user.getFullName());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhoneNumber());
        ProgressBar loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadingProgressBar.setVisibility(View.INVISIBLE);
        View overlayView = findViewById(R.id.overlayView);
        overlayView.setVisibility(View.GONE);
        nameEditText.setEnabled(true);
        emailEditText.setEnabled(true);
        phoneEditText.setEnabled(true);
        logout.setEnabled(true);
    }

    private void setupTextWatchers() {
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

        nameEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);
    }

    private void checkFieldsForChanges() {

        boolean hasChanged = !nameEditText.getText().toString().equals(user.getFullName()) ||
                !emailEditText.getText().toString().equals(user.getEmail()) ||
                !phoneEditText.getText().toString().equals(user.getPhoneNumber());
        saveButton.setEnabled(hasChanged);
        if(!hasChanged){
            nameError.setText("");
            phoneError.setText("");
            emailError.setText("");
        }
    }

    private void initSaveButton() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null && saveButton.isEnabled()) {
                    if(checkFieldsForErrors()){
                        return;
                    }
                    updateFirestoreUser(user);
                }
            }
        });
    }

    private boolean checkFieldsForErrors(){
        if(TextUtils.isEmpty(nameEditText.getText().toString())){
            nameError.setText(getString(R.string.fullName_required));
            nameError.setVisibility(View.VISIBLE);
            updateTopConstraintToBottom(constraintLayout,R.id.emailLabel, R.id.nameError);
        }else{
            nameError.setVisibility(View.INVISIBLE);
            updateTopConstraintToBottom(constraintLayout,R.id.emailLabel, R.id.nameAccountEditText);
            nameError.setText("");
        }

        if (TextUtils.isEmpty(emailEditText.getText().toString())) {
            emailError.setText(getResources().getString(R.string.email_required));
            emailError.setVisibility(View.VISIBLE);
            updateTopConstraintToBottom(constraintLayout,R.id.phoneLabel, R.id.emailError);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
            emailError.setText(getResources().getString(R.string.email_format_error));
            emailError.setVisibility(View.VISIBLE);
            updateTopConstraintToBottom(constraintLayout,R.id.phoneLabel, R.id.emailError);
        } else {
            emailError.setText("");
            emailError.setVisibility(View.INVISIBLE);
            updateTopConstraintToBottom(constraintLayout,R.id.phoneLabel, R.id.emailAccountEditText);
        }

        if(TextUtils.isEmpty(phoneEditText.getText().toString())){
            phoneError.setText(getResources().getString(R.string.phone_required));
            phoneError.setVisibility(View.VISIBLE);
        }else if(!isLebanesePhoneNumber(phoneEditText.getText().toString())){
            phoneError.setText(getResources().getString(R.string.phone_lebanese));
            phoneError.setVisibility(View.VISIBLE);
        }
        else{
            phoneError.setText("");
            phoneError.setVisibility(View.INVISIBLE);
        }

        if(phoneError.getText().toString().equals("") && emailError.getText().toString().equals("") && nameError.getText().toString().equals("")){
            return false;
        }
        return true;
    }

    private void updateTopConstraintToBottom(ConstraintLayout layout, int targetViewId, int anchorViewId) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.connect(targetViewId, ConstraintSet.TOP, anchorViewId, ConstraintSet.BOTTOM, (int) getResources().getDimension(R.dimen.item_margin_top));
        constraintSet.applyTo(layout);
    }

    private boolean isLebanesePhoneNumber(String number) {
        return number.matches("^(03|71|76|78|81|80)\\d{6}$");
    }

    private void updateFirestoreUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("fullName", nameEditText.getText().toString());
        userUpdates.put("email", emailEditText.getText().toString());
        userUpdates.put("phoneNumber", phoneEditText.getText().toString());

        db.collection("users").document(user.getUserId())
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AccountSettings.this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                    user.setFullName(nameEditText.getText().toString());
                    user.setEmail(emailEditText.getText().toString());
                    user.setPhoneNumber(phoneEditText.getText().toString());
                    saveButton.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AccountSettings.this, "Error updating user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void initLogout(){
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                LogoutDialog dialog = new LogoutDialog();
                dialog.show(fm, "Logout Dialog");
            }
        });
    }

    @Override
    public void didFinishLogoutDialog(boolean choice) {
        if(choice){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AccountSettings.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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
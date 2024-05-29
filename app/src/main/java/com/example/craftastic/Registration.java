package com.example.craftastic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {
    TextView signInLink;
    EditText fullNameEditText, emailEditText, phoneEditText, passwordEditText;
    Button registerBtn;
    TextInputLayout phoneTextInputLayout, passwordTextInputLayout, emailTextInputType, nameTextInputType;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    private int downX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initLayouts();
        initSignInLink();
        registration();
    }

    private void initLayouts() {
        mAuth = FirebaseAuth.getInstance();
        fullNameEditText = findViewById(R.id.register_name);
        nameTextInputType = findViewById(R.id.nameTextInputLayout);
        emailEditText = findViewById(R.id.register_email);
        emailTextInputType = findViewById(R.id.emailTextInputLayout);
        phoneEditText = findViewById(R.id.register_phone);
        phoneTextInputLayout = findViewById(R.id.phoneTextInputLayout);
        passwordEditText = findViewById(R.id.register_password);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        registerBtn = findViewById(R.id.registerBtn);
        signInLink = findViewById(R.id.linkSignIn);
        progressBar = findViewById(R.id.registrationLoadingProgressBar);
    }

    private void initSignInLink() {
        String text = getResources().getString(R.string.already_have_an_account_sign_in);
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Registration.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#E75480"));
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, text.indexOf("Sign In"), text.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        signInLink.setText(spannableString);
        signInLink.setMovementMethod(LinkMovementMethod.getInstance());
        signInLink.setHighlightColor(Color.TRANSPARENT);
    }

    private void registration() {
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, fullName, phoneNumber;
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                fullName = fullNameEditText.getText().toString();
                phoneNumber = phoneEditText.getText().toString();

                if (errorsBeforeRegistration(email, password, fullName, phoneNumber))
                    errorsBeforeRegistration(email, password, fullName, phoneNumber);
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    emailEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    fullNameEditText.setEnabled(false);
                    phoneEditText.setEnabled(false);
                    registerBtn.setClickable(false);
                    signInLink.setEnabled(false);
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String userId = firebaseUser.getUid();
                                    addUserDetails(userId, fullName, phoneNumber, email);
                                }
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                ConstraintLayout.LayoutParams phoneParams = (ConstraintLayout.LayoutParams) phoneEditText.getLayoutParams();
                                emailTextInputType.setError(getResources().getString(R.string.email_exists_error));
                                progressBar.setVisibility(View.GONE);
                                emailEditText.setEnabled(true);
                                passwordEditText.setEnabled(true);
                                fullNameEditText.setEnabled(true);
                                phoneEditText.setEnabled(true);
                                registerBtn.setClickable(true);
                                signInLink.setEnabled(true);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                emailEditText.setEnabled(true);
                                passwordEditText.setEnabled(true);
                                fullNameEditText.setEnabled(true);
                                phoneEditText.setEnabled(true);
                                registerBtn.setClickable(true);
                                signInLink.setEnabled(true);
                                Toast.makeText(Registration.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean errorsBeforeRegistration(String email, String password, String fullName, String phoneNumber) {
        if (TextUtils.isEmpty(email)) {
            emailTextInputType.setError(getResources().getString(R.string.email_required));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTextInputType.setError(getResources().getString(R.string.email_format_error));
        } else {
            emailTextInputType.setError(null);
        }


        if (TextUtils.isEmpty(password)) {
            passwordTextInputLayout.setError(getResources().getString(R.string.password_required));
        } else if (password.length() < 6) {
            passwordTextInputLayout.setError(getResources().getString(R.string.password_length_error));
        } else {
            passwordTextInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(fullName)) {
            nameTextInputType.setError(getResources().getString(R.string.fullName_required));
        } else {
            nameTextInputType.setError("");
        }

        if(TextUtils.isEmpty(phoneNumber)){
            phoneTextInputLayout.setError(getResources().getString(R.string.phone_required));
        }else if(!isLebanesePhoneNumber(phoneNumber)){
            phoneTextInputLayout.setError(getResources().getString(R.string.phone_lebanese));
        }
        else{
            phoneTextInputLayout.setError(null);
        }

        if (nameTextInputType.getError() == null && passwordTextInputLayout.getError() == null && emailTextInputType.getError() == null && phoneTextInputLayout.getError() == null)
            return false;
        return true;
    }

    private void addUserDetails(String userId, String fullName, String phoneNumber, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("phoneNumber", phoneNumber);
        user.put("email", email);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Registration.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent redirectIntent = new Intent(Registration.this, MainActivity.class);
                    redirectIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(redirectIntent);
                })
                .addOnFailureListener(e -> {
                    deleteUser(userId);
                });
    }

    private void deleteUser(String userId) {
        mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Registration.this, "Failed to create account, please try again", Toast.LENGTH_SHORT).show();
            } else {
                Log.w("TAG", "Failed to delete user");
            }
        });
    }

    private boolean isLebanesePhoneNumber(String number) {
        return number.matches("^(03|71|76|78|81|80)\\d{6}$");
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
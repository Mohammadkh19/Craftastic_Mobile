package com.example.craftastic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class Login extends AppCompatActivity {
    TextView signUpLink;
    EditText passwordEditText, emailEditText;
    TextInputLayout passwordTextInputLayout, emailTextInputType;
    Button loginBtn;
    FirebaseAuth mAuth;
    GestureDetector gestureDetector;
    ProgressBar progressBar;
    private int downX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initGestureDetect();
        initLayouts();
        initSignUpLink();
        login();
    }

    private void initGestureDetect(){
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    private void initLayouts() {
        mAuth = FirebaseAuth.getInstance();
        signUpLink = findViewById(R.id.linkSignUp);
        passwordEditText = findViewById(R.id.login_password);
        emailEditText = findViewById(R.id.login_email);
        passwordTextInputLayout = findViewById(R.id.loginPasswordTextInputLayout);
        emailTextInputType = findViewById(R.id.loginEmailTextInputLayout);
        progressBar = findViewById(R.id.loginLoadingProgressBar);
        loginBtn = findViewById(R.id.loginBtn);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginBtn.performClick();
                return true;
            }
            return false;
        });
    }

    private void initSignUpLink() {
        String text = getResources().getString(R.string.don_t_have_an_account_sign_up);
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Login.this, Registration.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.pink));
                ds.setUnderlineText(false);
            }
        };

        spannableString.setSpan(clickableSpan, text.indexOf("Sign Up"), text.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUpLink.setText(spannableString);
        signUpLink.setMovementMethod(LinkMovementMethod.getInstance());
        signUpLink.setHighlightColor(Color.TRANSPARENT);
    }

    private void login() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                if (errorsBeforeLogin(email, password)) {
                    errorsBeforeLogin(email, password);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    emailEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    loginBtn.setClickable(false);
                    signUpLink.setEnabled(false);
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String redirectTo = getIntent().getStringExtra("REDIRECT_TO");
                                if (redirectTo != null) {
                                    try {
                                        Class<?> redirectClass = Class.forName("com.example.craftastic." + redirectTo);
                                        Intent redirectIntent = new Intent(Login.this, redirectClass);
                                        startActivity(redirectIntent);
                                        finish();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                }
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException ||
                                    task.getException() instanceof FirebaseAuthInvalidUserException) {
                                progressBar.setVisibility(View.GONE);
                                emailEditText.setEnabled(true);
                                passwordEditText.setEnabled(true);
                                loginBtn.setClickable(true);
                                signUpLink.setEnabled(true);
                                Toast.makeText(Login.this, "Invalid email or password. Please try again.", Toast.LENGTH_LONG).show();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                emailEditText.setEnabled(true);
                                passwordEditText.setEnabled(true);
                                loginBtn.setClickable(true);
                                signUpLink.setEnabled(true);
                                Toast.makeText(Login.this, "Login failed. Please try again later.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                hideKeyboard();
            }
        });
    }

    private boolean errorsBeforeLogin(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailTextInputType.setError(getResources().getString(R.string.email_required));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTextInputType.setError(getResources().getString(R.string.email_format_error));
        } else {
            emailTextInputType.setError(null);
        }


        if (TextUtils.isEmpty(password)) {
            passwordTextInputLayout.setError(getResources().getString(R.string.password_required));
        } else {
            passwordTextInputLayout.setError(null);
        }


        if (passwordTextInputLayout.getError() == null && emailTextInputType.getError() == null)
            return false;
        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
        ScrollView s = findViewById(R.id.loginScrollView);
        s.fullScroll(ScrollView.FOCUS_UP);
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
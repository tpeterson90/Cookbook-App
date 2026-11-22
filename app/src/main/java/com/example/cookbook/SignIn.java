package com.example.cookbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private MaterialButton buttonSignIn;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    /**
     * Creates the sign-in screen.
     *
     * @param savedInstanceState saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // If user is already signed in, skip login.
        if (firebaseAuth.getCurrentUser() != null) {
            openMainActivity();
            return;
        }

        setContentView(R.layout.sign_in);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        progressBar = findViewById(R.id.progressSignIn);

        buttonSignIn.setOnClickListener(view -> signIn());

        findViewById(R.id.textCreateAccount)
                .setOnClickListener(view -> {
                    Intent intent =
                            new Intent(
                                    SignIn.this,
                                    CreateProfile.class
                            );

                    startActivity(intent);
                });
    }

    /**
     * Attempts to sign the user in with Firebase Authentication.
     */
    private void signIn() {

        String email = getText(inputEmail);
        String password = getText(inputPassword);

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter your email and password.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return;
        }

        setLoading(true);

        firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    setLoading(false);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                SignIn.this,
                                "Sign in successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();

                    } else {

                        String message =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unable to sign in.";

                        showToast(message);
                    }
                });
    }

    /**
     * Opens the main Cookbook screen.
     */
    private void openMainActivity() {

        Intent intent =
                new Intent(
                        SignIn.this,
                        MainActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    /**
     * Enables or disables the loading state.
     *
     * @param loading true while signing in
     */
    private void setLoading(boolean loading) {

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        buttonSignIn.setEnabled(!loading);
    }

    /**
     * Gets trimmed text from an input.
     *
     * @param input input field
     * @return entered text
     */
    private String getText(TextInputEditText input) {

        if (input.getText() == null) {
            return "";
        }

        return input
                .getText()
                .toString()
                .trim();
    }

    /**
     * Displays a Toast message.
     *
     * @param message text to display
     */
    private void showToast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
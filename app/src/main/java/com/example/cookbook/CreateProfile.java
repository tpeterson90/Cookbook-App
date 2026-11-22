package com.example.cookbook;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateProfile extends AppCompatActivity {

    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;

    private MaterialButton buttonCreateAccount;
    private ProgressBar progressCreateAccount;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);

        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        progressCreateAccount = findViewById(R.id.progressCreateAccount);

        buttonCreateAccount.setOnClickListener(view -> {

            Toast.makeText(
                    CreateProfile.this,
                    "Create Account pressed",
                    Toast.LENGTH_SHORT
            ).show();

            createAccount();
        });

        findViewById(R.id.textBackToSignIn)
                .setOnClickListener(view -> finish());
    }

    private void createAccount() {

        String firstName = getText(inputFirstName);
        String lastName = getText(inputLastName);
        String email = getText(inputEmail);
        String password = getText(inputPassword);
        String confirmPassword = getText(inputConfirmPassword);

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            showToast("Please fill in all fields.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showToast("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match.");
            return;
        }

        setLoading(true);

        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        setLoading(false);

                        String errorMessage =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unable to create account.";

                        showToast(errorMessage);
                        return;
                    }

                    FirebaseUser firebaseUser =
                            firebaseAuth.getCurrentUser();

                    if (firebaseUser == null) {

                        setLoading(false);
                        showToast("Account creation failed.");
                        return;
                    }

                    saveUserProfile(
                            firebaseUser.getUid(),
                            firstName,
                            lastName,
                            email
                    );
                });
    }

    private void saveUserProfile(
            String userId,
            String firstName,
            String lastName,
            String email) {

        Map<String, Object> userData =
                new HashMap<>();

        userData.put("userId", userId);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("email", email);
        userData.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        database
                .collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(unused -> {

                    setLoading(false);

                    firebaseAuth.signOut();

                    Toast.makeText(
                            CreateProfile.this,
                            "Account created successfully. Sign in now.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(error -> {

                    setLoading(false);

                    showToast(
                            "Firestore error: "
                                    + error.getMessage()
                    );
                });
    }

    private void setLoading(boolean loading) {

        buttonCreateAccount.setEnabled(!loading);

        progressCreateAccount.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private String getText(
            TextInputEditText input) {

        if (input.getText() == null) {
            return "";
        }

        return input
                .getText()
                .toString()
                .trim();
    }

    private void showToast(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
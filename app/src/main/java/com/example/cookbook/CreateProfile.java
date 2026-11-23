package com.example.cookbook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles account creation and saves user profile
 * information into Cloud Firestore.
 */
public class CreateProfile extends AppCompatActivity {

    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;

    private ImageView imageProfile;

    private MaterialButton buttonChoosePhoto;
    private MaterialButton buttonCreateAccount;

    private ProgressBar progressCreateAccount;

    private Bitmap selectedImage;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    private ActivityResultLauncher<String> imagePickerLauncher;

    /**
     * Initializes the create profile screen.
     *
     * @param savedInstanceState saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.create_profile
        );

        firebaseAuth =
                FirebaseAuth.getInstance();

        database =
                FirebaseFirestore.getInstance();

        inputFirstName =
                findViewById(
                        R.id.inputFirstName
                );

        inputLastName =
                findViewById(
                        R.id.inputLastName
                );

        inputEmail =
                findViewById(
                        R.id.inputEmail
                );

        inputPassword =
                findViewById(
                        R.id.inputPassword
                );

        inputConfirmPassword =
                findViewById(
                        R.id.inputConfirmPassword
                );

        imageProfile =
                findViewById(
                        R.id.imageProfile
                );

        buttonChoosePhoto =
                findViewById(
                        R.id.buttonChoosePhoto
                );

        buttonCreateAccount =
                findViewById(
                        R.id.buttonCreateAccount
                );

        progressCreateAccount =
                findViewById(
                        R.id.progressCreateAccount
                );

        /*
         * Sets up the Android image picker.
         */
        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        this::loadImage
                );

        buttonChoosePhoto
                .setOnClickListener(
                        view -> choosePhoto()
                );

        imageProfile
                .setOnClickListener(
                        view -> choosePhoto()
                );

        buttonCreateAccount
                .setOnClickListener(
                        view -> createAccount()
                );

        findViewById(
                R.id.textBackToSignIn
        ).setOnClickListener(
                view -> finish()
        );
    }

    /**
     * Opens the Android image picker.
     */
    private void choosePhoto() {

        imagePickerLauncher.launch(
                "image/*"
        );
    }

    /**
     * Loads and previews the selected profile image.
     *
     * @param uri selected image URI
     */
    private void loadImage(Uri uri) {

        if (uri == null) {
            return;
        }

        new Thread(() -> {

            try (
                    InputStream inputStream =
                            getContentResolver()
                                    .openInputStream(uri)
            ) {

                Bitmap bitmap =
                        BitmapFactory.decodeStream(
                                inputStream
                        );

                if (bitmap != null) {

                    selectedImage = bitmap;

                    runOnUiThread(() -> {

                        imageProfile.setPadding(
                                0,
                                0,
                                0,
                                0
                        );

                        imageProfile.setImageBitmap(
                                selectedImage
                        );
                    });
                }

            } catch (IOException exception) {

                runOnUiThread(() ->
                        showToast(
                                "Unable to load image."
                        )
                );
            }

        }).start();
    }

    /**
     * Validates the form and creates a Firebase account.
     */
    private void createAccount() {

        String firstName =
                getText(
                        inputFirstName
                );

        String lastName =
                getText(
                        inputLastName
                );

        String email =
                getText(
                        inputEmail
                );

        String password =
                getText(
                        inputPassword
                );

        String confirmPassword =
                getText(
                        inputConfirmPassword
                );

        if (!validateForm(
                firstName,
                lastName,
                email,
                password,
                confirmPassword)) {

            return;
        }

        setLoading(true);

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        setLoading(false);

                        String message =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unable to create account.";

                        showToast(
                                message
                        );

                        return;
                    }

                    FirebaseUser user =
                            firebaseAuth
                                    .getCurrentUser();

                    if (user == null) {

                        setLoading(false);

                        showToast(
                                "Account creation failed."
                        );

                        return;
                    }

                    saveUserProfile(
                            user.getUid(),
                            firstName,
                            lastName,
                            email
                    );
                });
    }

    /**
     * Validates the account creation fields.
     *
     * Profile image is optional.
     *
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email
     * @param password user's password
     * @param confirmPassword repeated password
     * @return true when form is valid
     */
    private boolean validateForm(
            String firstName,
            String lastName,
            String email,
            String password,
            String confirmPassword) {

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            showToast(
                    "Please fill in all fields."
            );

            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            showToast(
                    "Please enter a valid email address."
            );

            return false;
        }

        if (password.length() < 6) {

            showToast(
                    "Password must be at least 6 characters."
            );

            return false;
        }

        if (!password.equals(
                confirmPassword)) {

            showToast(
                    "Passwords do not match."
            );

            return false;
        }

        return true;
    }

    /**
     * Saves user information to Cloud Firestore.
     *
     * If a profile image was selected, it is stored
     * as a Base64 string. If no image was selected,
     * profileImage is saved as an empty string.
     *
     * @param userId Firebase Authentication UID
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email
     */
    private void saveUserProfile(
            String userId,
            String firstName,
            String lastName,
            String email) {

        new Thread(() -> {

            String encodedImage = "";

            if (selectedImage != null) {

                encodedImage =
                        encodeImage(
                                selectedImage
                        );
            }

            String finalEncodedImage =
                    encodedImage;

            runOnUiThread(() -> {

                Map<String, Object> userData =
                        new HashMap<>();

                userData.put(
                        "userId",
                        userId
                );

                userData.put(
                        "firstName",
                        firstName
                );

                userData.put(
                        "lastName",
                        lastName
                );

                userData.put(
                        "email",
                        email
                );

                userData.put(
                        "profileImage",
                        finalEncodedImage
                );

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

                            /*
                             * Sign the newly created user out
                             * so they return to normal login.
                             */
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
            });

        }).start();
    }

    /**
     * Resizes, compresses, and encodes a profile image.
     *
     * @param bitmap selected image
     * @return Base64 encoded image
     */
    private String encodeImage(
            Bitmap bitmap) {

        int width = 200;

        int height =
                bitmap.getHeight()
                        * width
                        / bitmap.getWidth();

        Bitmap resized =
                Bitmap.createScaledBitmap(
                        bitmap,
                        width,
                        height,
                        true
                );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        resized.compress(
                Bitmap.CompressFormat.JPEG,
                65,
                outputStream
        );

        return Base64.encodeToString(
                outputStream.toByteArray(),
                Base64.NO_WRAP
        );
    }

    /**
     * Shows or hides account creation loading state.
     *
     * @param loading true while account creation is processing
     */
    private void setLoading(
            boolean loading) {

        buttonCreateAccount
                .setEnabled(
                        !loading
                );

        buttonChoosePhoto
                .setEnabled(
                        !loading
                );

        progressCreateAccount
                .setVisibility(
                        loading
                                ? View.VISIBLE
                                : View.GONE
                );
    }

    /**
     * Reads and trims a text field.
     *
     * @param input input field
     * @return trimmed field text
     */
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

    /**
     * Shows a Toast message.
     *
     * @param message text to display
     */
    private void showToast(
            String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
package com.example.cookbook.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookbook.R;
import com.example.cookbook.SignIn;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays the signed-in user's profile and provides sign out.
 */
public class ProfileFragment extends Fragment {

    private TextView textName;
    private TextView textEmail;
    private ImageView imageProfile;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_profile,
                        container,
                        false
                );

        firebaseAuth =
                FirebaseAuth.getInstance();

        database =
                FirebaseFirestore.getInstance();

        textName =
                view.findViewById(
                        R.id.textProfileName
                );

        textEmail =
                view.findViewById(
                        R.id.textProfileEmail
                );

        imageProfile =
                view.findViewById(
                        R.id.imageProfilePage
                );

        MaterialButton buttonSignOut =
                view.findViewById(
                        R.id.buttonSignOut
                );

        buttonSignOut.setOnClickListener(
                clickedView -> signOut()
        );

        loadProfile();

        return view;
    }

    /**
     * Loads profile information from Firestore.
     */
    private void loadProfile() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        database
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    String firstName =
                            document.getString(
                                    "firstName"
                            );

                    String lastName =
                            document.getString(
                                    "lastName"
                            );

                    String email =
                            document.getString(
                                    "email"
                            );

                    String profileImage =
                            document.getString(
                                    "profileImage"
                            );

                    String name =
                            ((firstName == null
                                    ? ""
                                    : firstName)
                                    + " "
                                    + (lastName == null
                                    ? ""
                                    : lastName))
                                    .trim();

                    textName.setText(
                            name.isEmpty()
                                    ? "CookBook User"
                                    : name
                    );

                    textEmail.setText(
                            email == null
                                    ? user.getEmail()
                                    : email
                    );

                    if (profileImage != null
                            && !profileImage.isEmpty()) {

                        try {

                            byte[] bytes =
                                    Base64.decode(
                                            profileImage,
                                            Base64.DEFAULT
                                    );

                            Bitmap bitmap =
                                    BitmapFactory.decodeByteArray(
                                            bytes,
                                            0,
                                            bytes.length
                                    );

                            imageProfile.setImageBitmap(
                                    bitmap
                            );

                        } catch (Exception ignored) {
                        }
                    }
                })
                .addOnFailureListener(error ->

                        Toast.makeText(
                                requireContext(),
                                "Unable to load profile.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    /**
     * Signs the current user out.
     */
    private void signOut() {

        firebaseAuth.signOut();

        Intent intent =
                new Intent(
                        requireContext(),
                        SignIn.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }
}
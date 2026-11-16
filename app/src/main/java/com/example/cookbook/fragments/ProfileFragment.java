package com.example.cookbook.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookbook.R;

public class ProfileFragment extends Fragment {
    public ProfileFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, //Creates screen and loads XML layout
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //Edit button for editing Name, EmailAddress, and Initials
        TextView editButton = view.findViewById(R.id.edit_button);
        TextView nameText = view.findViewById(R.id.textProfileName);
        TextView emailText = view.findViewById(R.id.textProfileEmail);
        TextView initialsText = view.findViewById(R.id.textAddImage);

        //Opens edit popup when user clicks edit
        editButton.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            int padding = (int) (24 * getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, 0);

            //Input for editing user firstname, lastname, and email
            EditText firstName = new EditText(requireContext());
            firstName.setHint("First Name");
            EditText lastName = new EditText(requireContext());
            lastName.setHint("Last Name");
            EditText email = new EditText(requireContext());
            email.setHint("Email");
            email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

//            firstName.setText(userFirstName);   <-- From Create Account or Stored Information
//            lastName.setText(userLastName);       <-- From Create Account or Stored Information
            email.setText(emailText.getText().toString());
            //Adds input to dialog layout
            layout.addView(firstName);
            layout.addView(lastName);
            layout.addView(email);
            //Creates dialog(popup) with save or cancel options
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Edit Profile")
                    .setView(layout)
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    //Updates profile information when user saves changes
                    .setPositiveButton("Save", (dialog, which) -> {

                        String first = firstName.getText().toString().trim();
                        String last = lastName.getText().toString().trim();
                        String emailAddress = email.getText().toString().trim();

                        nameText.setText(first + " " + last);
                        emailText.setText(emailAddress);

                        //Creates profile initials from first and last name
                        String initials = first.isEmpty() ? "" :
                                first.substring(0,1).toUpperCase();

                        if (!last.isEmpty()) {
                            initials += last.substring(0,1).toUpperCase();
                        }
                        initialsText.setText(initials);
                    })
                    .show();
        });


//  Initializes Dropdown Menu that provides options for the amount of people your cooking for
        Button peopleButton = view.findViewById(R.id.people_menu);
        peopleButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), peopleButton);
            popup.getMenuInflater().inflate(R.menu.menu_people, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();

                peopleButton.setText(selected);

                return true;
            });
            popup.show();
        });
// Initializes Dropdown Menu that provides dietary options
        Button dietButton = view.findViewById(R.id.dietary_menu);
        dietButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), dietButton);
            popup.getMenuInflater().inflate(R.menu.menu_dietary, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();

                dietButton.setText(selected);

                return true;
            });
            popup.show();
        });
// Initializes Dropdown Menu that provides measurement options
        Button metricButton = view.findViewById(R.id.metric_menu);
        metricButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), metricButton);
            popup.getMenuInflater().inflate(R.menu.menu_metric, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();

                metricButton.setText(selected);

                return true;
            });
            popup.show();
        });
        return view;
    }}

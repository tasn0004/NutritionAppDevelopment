package com.nutritionapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.parse.SaveCallback;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Settings extends AppCompatActivity {
    private ViewPager2 viewPager;
    TextView username;
    ParseUser currentUser = ParseUser.getCurrentUser();
    private ImageView userProfileImage;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        // set the default navigation bar to home
        bottomNavigationView.setSelectedItemId(R.id.navigation_account);
         userProfileImage = findViewById(R.id.userProfileImage);
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open image picker
                openImagePicker();
            }
        });
        /****************************************************************************/
        //navigation bar [ HOME - SEARCH - CHECKLIST - USER ]
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        username = findViewById(R.id.userNameSettings);
        username.setText(grabUsername());

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    Intent searchIntent = new Intent(Settings.this, HomePage.class);
                    startActivity(searchIntent);
                    finish();

                } else if (itemId == R.id.navigation_checklist) {
                    Intent searchIntent = new Intent(Settings.this, WeeklyHub.class);
                    startActivity(searchIntent);
                    finish();
                } else if (itemId == R.id.navigation_account) {
                    Intent searchIntent = new Intent(Settings.this, Settings.class);
                    startActivity(searchIntent);
                }
                return true;
            }
        });
        /****************************************************************************/
        // set content of settings page
        viewPager = findViewById(R.id.view_pager);
        SettingsAdapter settingsAdapter = new SettingsAdapter(this);
        viewPager.setAdapter(settingsAdapter);
        loadUserProfilePicture(); // Load the profile picture

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                uploadImageToBack4App(imageUri);
            }
        }
    }
    private void openImagePicker() {
        // Intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void uploadImageToBack4App(Uri imageUri) {
        String realPath = getRealPathFromURI(imageUri);
        if (realPath == null) {
            Log.e("Settings", "Real path is null.");
            return;
        }
        File imageFile = new File(realPath);
        if (!imageFile.exists() || !imageFile.canRead()) {
            Log.e("Settings", "File does not exist or cannot be read.");
            return;
        }
        ParseFile parseFile = new ParseFile(imageFile);

        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseUser user = ParseUser.getCurrentUser();
                    if (user == null) {
                        Log.e("Settings", "Current user is null.");
                        return;
                    }
                    user.put("userPictureProfile", parseFile);
                    user.saveInBackground(e1 -> {
                        if (e1 == null) {
                            userProfileImage.setImageURI(imageUri);
                        } else {
                            Log.e("Settings", "Error saving user with image: " + e1.getMessage());
                        }
                    });
                } else {
                    Log.e("Settings", "Error uploading image: " + (e != null ? e.getMessage() : "Unknown error"));
                }
            }
        });

    }
    private void loadUserProfilePicture() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            ParseFile profileImageFile = user.getParseFile("userPictureProfile");
            if (profileImageFile != null) {
                // Use Glide to load the image
                Glide.with(this)
                        .load(profileImageFile.getUrl())
                        .placeholder(R.drawable.default_profile_pic)
                        .error(R.drawable.default_profile_pic)
                        .into(userProfileImage);
            } else {
                // Set default image if userPictureProfile is null
                userProfileImage.setImageResource(R.drawable.default_profile_pic);
            }
        }
    }


    private String getRealPathFromURI(Uri uri) {
        String path = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        String displayName = cursor.getString(displayNameIndex);
                        File file = new File(getCacheDir(), displayName);
                        try (InputStream is = getContentResolver().openInputStream(uri);
                             OutputStream os = new FileOutputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) > 0) {
                                os.write(buffer, 0, length);
                            }
                            path = file.getAbsolutePath();
                        } catch (Exception e) {
                            Log.e("Settings", "Error processing file for upload: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Settings", "Error getting file path: " + e.getMessage());
            }
        }
        return path;
    }



    private String grabUsername() {
        String first = currentUser.getString("firstName");
        String last = currentUser.getString("lastName");
        String fullName = first + " " + last;

        return fullName;
    }
}
class SettingsAdapter extends FragmentStateAdapter {
    public SettingsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new SettingsContent();
    }
    @Override
    public int getItemCount() {
        return 1;
    }
}



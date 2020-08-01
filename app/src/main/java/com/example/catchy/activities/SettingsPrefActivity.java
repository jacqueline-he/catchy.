package com.example.catchy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DialogFragment;

import androidx.fragment.app.FragmentActivity;
import androidx.palette.graphics.Palette;
import androidx.preference.EditTextPreference;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.example.catchy.R;
import com.example.catchy.activities.LoginActivity;
import com.example.catchy.models.Following;
import com.example.catchy.models.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.File;

import co.revely.gradient.RevelyGradient;

public class SettingsPrefActivity extends AppCompatActivity {
    public static final String TAG = "SettingsPrefActivity";
    private RelativeLayout layout;
    private TextView tvSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_pref);
        setTheme(R.style.SettingStyle);
        getFragmentManager().beginTransaction()
                .replace(R.id.fm_pref, new SettingsFragment()).commit();

        layout = findViewById(R.id.layout);
        tvSettings = findViewById(R.id.tvSettings);
        setBackgroundColor();
    }


    private void setBackgroundColor() {
        if (User.profileBitmap != null && !User.profileBitmap.isRecycled()) {
            Palette palette = Palette.from(User.profileBitmap).generate();
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();
            // int color = palette.getDarkMutedColor(0);
            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            // swatch.getRgb()
            if (swatch != null) {
                // ((RelativeLayout) findViewById(R.id.layout)).setBackgroundColor(swatch.getRgb());

                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(270f).alpha(0.76f)
                        .onBackgroundOf(layout);
            }
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        private EditTextPreference updateBio;
        private EditTextPreference updateName;
        private Preference updateProfilePic;
        private Preference logout;
        private Preference about;
        private SwitchPreference explicitFilter;
        private SwitchPreference durationPref;

        private File photoFile;
        private String photoFileName = "photo.jpg";
        public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
            updateBio = findPreference("updatebio");
            updateName = findPreference("updatename");
            updateProfilePic = findPreference("updateprofilepic");
            logout = findPreference("logout");
            about = findPreference("about");
            explicitFilter = findPreference("explicitfilter");
            durationPref = findPreference("durationpref");

            explicitFilter.setChecked(ParseUser.getCurrentUser().getBoolean("explicitFilter")); // sets switch based on user preferences

            explicitFilter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean selected = Boolean.parseBoolean(newValue.toString());
                    if (selected) {
                        Toast.makeText(getActivity(), "Explicit filter turned on!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Explicit filter turned off!", Toast.LENGTH_LONG).show();
                    }
                    // update selected
                    ParseUser.getCurrentUser().put("explicitFilter", selected);
                    ParseUser.getCurrentUser().saveInBackground();
                    return true;
                }
            });

            durationPref.setChecked(ParseUser.getCurrentUser().getBoolean("durationPref")); // sets switch based on user preferences
            durationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean selected = Boolean.parseBoolean(newValue.toString());
                    if (selected) {
                        Toast.makeText(getActivity(), "Play 30-second snippet in feed!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Play full-length song in feed!", Toast.LENGTH_LONG).show();
                    }
                    // update selected
                    ParseUser.getCurrentUser().put("durationPref", selected);
                    ParseUser.getCurrentUser().saveInBackground();
                    return true;
                }
            });

            updateBio.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ParseUser.getCurrentUser().put("bio", (String) newValue);
                    ParseUser.getCurrentUser().saveInBackground();
                    return true;
                }
            });

            updateName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ParseUser.getCurrentUser().put("fullName", (String) newValue);
                    ParseUser.getCurrentUser().saveInBackground();
                    return true;
                }
            });

            updateProfilePic.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    launchCamera();
                    return true;
                }
            });

            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AboutFragment dialog = new AboutFragment();
                    dialog.show(getActivity().getFragmentManager(), "tag");
                    return true;
                }
            });

            logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    for (Following item : User.followingItems) {
                        item.deleteInBackground();
                    }
                    for (ParseUser user : User.following) {
                        Following following = new Following();
                        following.setFollowedBy(currentUser);
                        following.setFollowing(user);
                        following.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving relationship", e);
                                    e.printStackTrace();
                                }
                                Log.i(TAG, "Relationship save was successful!");
                            }
                        });
                    }
                    User.followers = null;
                    User.following = null;
                    ParseUser.logOut();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getActivity().finish();
                    startActivity(intent);
                    return true;
                }
            });

        }

        private void launchCamera() {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Create a File reference for future access
            photoFile = getPhotoFileUri(photoFileName + Math.random());

            // wrap File object into a content provider
            // required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
            Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.example.catchy", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }

        private File getPhotoFileUri(String fileName) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return new File(mediaStorageDir.getPath() + File.separator + fileName);
        }
        // TODO add explicit icon from materials design

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    // Bitmap taken = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    // ivProfileImage.setImageBitmap(taken);
                    ParseFile file = new ParseFile(photoFile);
                    ParseUser.getCurrentUser().put("profilePic", file);
                    ParseUser.getCurrentUser().saveInBackground();

                    new Thread(() -> {
                        try {
                            User.profileBitmap = Picasso.get().load(photoFile.getAbsolutePath()).get();
                        } catch (Exception e) {
                            Log.e("SettingsPrefActivity", "couldn't get bitmap"+e);
                        }
                    }).start();

                    // Glide.with(this).load(photoFile.getAbsolutePath()).transform(new CircleCrop()).into(ivProfileImage);
                } else { // Result was a failure
                    Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public static class AboutFragment extends DialogFragment {
        private TextView tvAbout;

        // Constructor
        public AboutFragment() {
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_about, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            tvAbout = view.findViewById(R.id.tvAbout);
            tvAbout.setText("Made with love from California");
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
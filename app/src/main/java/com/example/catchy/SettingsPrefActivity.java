package com.example.catchy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.example.catchy.activities.LoginActivity;
import com.example.catchy.activities.SettingsActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

public class SettingsPrefActivity extends AppCompatActivity {
    public static final String TAG="SettingsPrefActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_pref);
        setTheme(R.style.SettingStyle);
        getFragmentManager().beginTransaction()
                .replace(R.id.fm_pref, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        private EditTextPreference updateBio;
        private EditTextPreference updateName;
        private Preference updateProfilePic;
        private Preference logout;
        private Preference about;

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

            updateBio.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ParseUser.getCurrentUser().put("bio", (String) newValue);
                    ParseUser.getCurrentUser().saveInBackground();
                    return false;
                }
            });

            updateName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ParseUser.getCurrentUser().put("fullName", (String) newValue);
                    ParseUser.getCurrentUser().saveInBackground();
                    return false;
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
                    return false;
                }
            });

            logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ParseUser.logOut();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish();
                    return true;
                }
            });

        }

        private void launchCamera() {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Create a File reference for future access
            photoFile = getPhotoFileUri(photoFileName);

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
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return new File(mediaStorageDir.getPath() + File.separator + fileName);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    // Bitmap taken = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    // ivProfileImage.setImageBitmap(taken);
                    // Log.d("TAG", "THIS!!!! " + photoFile.getAbsolutePath());
                    ParseFile file = new ParseFile(photoFile);
                    ParseUser.getCurrentUser().put("profilePic", file);
                    ParseUser.getCurrentUser().saveInBackground();

                    // Glide.with(this).load(photoFile.getAbsolutePath()).transform(new CircleCrop()).into(ivProfileImage);
                } else { // Result was a failure
                    Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
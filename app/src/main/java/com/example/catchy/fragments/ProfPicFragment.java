package com.example.catchy.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.app.DialogFragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.example.catchy.R;
import com.example.catchy.databinding.FragmentAboutBinding;
import com.example.catchy.databinding.FragmentProfPicBinding;
import com.example.catchy.models.User;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;


public class ProfPicFragment extends DialogFragment {
    private Button btnTakePicture;
    private Button btnUpload;
    private FragmentProfPicBinding binding;

    private File photoFile;
    private String photoFileName;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final String TAG = "ProfPicFragment";


    public ProfPicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfPicBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnTakePicture = binding.btnTakePicture;
        btnUpload = binding.btnUpload;

        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void launchCamera() {
        photoFileName = "photo" + Math.random() + ".jpg";
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.example.catchy", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        intent.putExtra("photoFile", photoFile);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            User.photoFile = photoFile;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // Bitmap taken = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // ivProfileImage.setImageBitmap(taken);
                File photoFile = User.photoFile;
                ParseFile file = new ParseFile(photoFile);
                ParseUser.getCurrentUser().put("profilePic", file);
                ParseUser.getCurrentUser().saveInBackground();

                new Thread(() -> {
                    try {
                        Log.d("SettingsPrefActivity", "getting bitmap from new profile pic");
                        User.profileBitmap = Picasso.get().load(photoFile).get();
                        Log.d("SettingsPrefActivity", "successfully retrieved bitmap");
                    } catch (Exception e) {
                        Log.e("SettingsPrefActivity", "couldn't get bitmap" + e);
                    }
                }).start();

                User.profPicChanged = true;
                dismiss();
                // Glide.with(this).load(photoFile.getAbsolutePath()).transform(new CircleCrop()).into(ivProfileImage);
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
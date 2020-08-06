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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.bumptech.glide.Glide;
import com.example.catchy.R;
import com.example.catchy.activities.LoginActivity;
import com.example.catchy.databinding.ActivitySettingsPrefBinding;
import com.example.catchy.databinding.FragmentAboutBinding;
import com.example.catchy.fragments.ProfPicFragment;
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
        setTheme(R.style.SettingStyle);
        getFragmentManager().beginTransaction()
                .replace(R.id.fm_pref, new SettingsFragment()).commit();

        ActivitySettingsPrefBinding binding = ActivitySettingsPrefBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        layout = binding.layout;
        tvSettings = binding.tvSettings;
        setBackgroundColor();
    }


    private void setBackgroundColor() {
        if (User.profileBitmap != null && !User.profileBitmap.isRecycled()) {
            Palette palette = Palette.from(User.profileBitmap).generate();
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();

            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            if (swatch != null) {
                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(270f).alpha(0.76f)
                        .onBackgroundOf(layout);
            }
        } else {
            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.76f)
                    .onBackgroundOf(layout);
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
                    Toast toast;
                    if (selected) {
                        toast = Toast.makeText(getActivity(), "Explicit filter turned on!", Toast.LENGTH_LONG);
                    } else {
                        toast = Toast.makeText(getActivity(), "Explicit filter turned off!", Toast.LENGTH_LONG);
                    }
                    View view = toast.getView();

                    TextView text = (TextView) view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();
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
                    Toast toast;
                    if (selected) {
                        toast = Toast.makeText(getActivity(), "Play 30-second snippet in feed!", Toast.LENGTH_LONG);
                    } else {
                        toast = Toast.makeText(getActivity(), "Play full-length song in feed!", Toast.LENGTH_LONG);
                    }
                    View view = toast.getView();

                    TextView text = (TextView) view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();

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
                    User.bioChanged = true;
                    User.changedBio = (String) newValue;
                    return true;
                }
            });

            updateName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ParseUser.getCurrentUser().put("fullName", (String) newValue);
                    ParseUser.getCurrentUser().saveInBackground();
                    User.nameChanged = true;
                    User.changedName = (String) newValue;
                    return true;
                }
            });

            updateProfilePic.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // launchCamera();
                    ProfPicFragment dialog = new ProfPicFragment();
                    dialog.show(getActivity().getFragmentManager(), "tag");
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

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActivity().setTheme(R.style.CustomDialogTheme);
        }
    }

    public static class AboutFragment extends DialogFragment {
        private TextView tvAbout;
        private TextView tvDesc;
        private ImageView ivLogo;
        private RelativeLayout layout;
        private FragmentAboutBinding binding;

        // Constructor
        public AboutFragment() {
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = FragmentAboutBinding.inflate(inflater, container, false);
            View view = binding.getRoot();
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            tvAbout = binding.tvAbout;
            tvDesc = binding.tvDesc;
            ivLogo = binding.ivLogo;
            layout = binding.layout;

            Glide.with(this)
                    .load(R.drawable.blue_disc)
                    .into(ivLogo);

            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.13f)
                    .onBackgroundOf(layout);
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
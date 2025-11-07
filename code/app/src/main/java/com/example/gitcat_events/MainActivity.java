package com.example.gitcat_events;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.gitcat_events.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if profile exists - if not, redirect to setup
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String profileId = prefs.getString(KEY_PROFILE_ID, null);
        
        if (profileId == null) {
            // No profile exists, redirect to setup page
            Intent intent = new Intent(this, SetupProfileActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity so user can't go back
            return;
        }

        // add binding for bottom navigation menu
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if we should navigate to a specific fragment
        String targetFragment = getIntent().getStringExtra("fragment");
        if (targetFragment != null) {
            switch (targetFragment) {
                case "home":
                    replaceFragment(new HomeFragment());
                    binding.bottomNavigationView.setSelectedItemId(R.id.home);
                    break;
                case "notifs":
                    replaceFragment(new NotifsFragment());
                    binding.bottomNavigationView.setSelectedItemId(R.id.notifs);
                    break;
                case "create":
                    replaceFragment(new CreateFragment());
                    binding.bottomNavigationView.setSelectedItemId(R.id.create);
                    break;
                case "profile":
                    replaceFragment(new ProfileFragment());
                    binding.bottomNavigationView.setSelectedItemId(R.id.profile);
                    break;
                default:
                    replaceFragment(new HomeFragment());
                    break;
            }
        } else {
            // set initial page
            replaceFragment(new HomeFragment());
        }

        binding.bottomNavigationView.setOnItemSelectedListener((item) -> {
            int id = item.getItemId();

            if(id == R.id.home){
                replaceFragment(new HomeFragment());
            } else if(id == R.id.notifs) {
                replaceFragment(new NotifsFragment());
                // to do
            } else if(id == R.id.create) {
                replaceFragment(new CreateFragment());
                // to do
            } else if(id == R.id.profile) {
                replaceFragment(new ProfileFragment());
                // to do
            }

            return true;
        });
    }

    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameLayout, fragment);
        ft.commit();
    }
}
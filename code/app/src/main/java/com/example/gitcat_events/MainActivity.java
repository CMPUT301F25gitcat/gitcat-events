package com.example.gitcat_events;

import androidx.fragment.app.Fragment;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add binding for bottom navigation menu
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // set initial page
        replaceFragment(new HomeFragment());

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
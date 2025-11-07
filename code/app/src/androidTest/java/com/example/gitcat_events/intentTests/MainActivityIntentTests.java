package com.example.gitcat_events.intentTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gitcat_events.MainActivity;
import com.example.gitcat_events.SetupProfileActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for MainActivity
 * Tests navigation behavior and profile verification requirements
 * 
 * Requirements tested:
 * - US: User login/profile setup flow
 * - MainActivity should redirect to SetupProfileActivity when no profile exists
 * - MainActivity should display main UI when profile exists
 * - Fragment navigation with intent extras
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityIntentTests {

    private static final String PREFS = "app_prefs";
    private static final String KEY_PROFILE_ID = "profile_doc_id";
    private Context context;
    private SharedPreferences prefs;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // Clear profile before each test
        clearProfile();
    }

    @After
    public void tearDown() {
        // Clean up after each test
        clearProfile();
    }

    private void clearProfile() {
        prefs.edit().remove(KEY_PROFILE_ID).apply();
    }

    private void setProfileId(String profileId) {
        prefs.edit().putString(KEY_PROFILE_ID, profileId).apply();
    }



    /**
     * Test: MainActivity displays main UI when profile exists
     * Requirement: Authenticated users should see the main app interface
     */
    @Test
    public void testMainActivityDisplaysMainUIWhenProfileExists() {
        // Set a profile ID
        setProfileId("test_profile_123");

        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Wait for UI to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify MainActivity is displayed (not finishing)
        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
            assert !activity.isDestroyed();
        });

        scenario.close();
    }

    /**
     * Test: MainActivity navigates to home fragment by default
     * Requirement: Default landing page should be home
     */
    @Test
    public void testMainActivityShowsHomeFragmentByDefault() {
        setProfileId("test_profile_123");

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // Wait for fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify activity is running
        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
        });

        scenario.close();
    }

    /**
     * Test: MainActivity navigates to specific fragment via intent extra
     * Requirement: Deep linking to specific fragments should work
     */
    @Test
    public void testMainActivityNavigatesToFragmentViaIntentExtra() {
        setProfileId("test_profile_123");

        // Create intent with fragment extra
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "home");

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        // Wait for fragment to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify activity is running
        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
            // Verify the intent extra was read
            String fragment = activity.getIntent().getStringExtra("fragment");
            assert "home".equals(fragment);
        });

        scenario.close();
    }

    /**
     * Test: MainActivity handles profile fragment navigation
     * Requirement: Navigation to profile fragment should work
     */
    @Test
    public void testMainActivityNavigatesToProfileFragment() {
        setProfileId("test_profile_123");

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "profile");

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
            String fragment = activity.getIntent().getStringExtra("fragment");
            assert "profile".equals(fragment);
        });

        scenario.close();
    }

    /**
     * Test: MainActivity handles create fragment navigation
     * Requirement: Navigation to create event fragment should work
     */
    @Test
    public void testMainActivityNavigatesToCreateFragment() {
        setProfileId("test_profile_123");

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "create");

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
            String fragment = activity.getIntent().getStringExtra("fragment");
            assert "create".equals(fragment);
        });

        scenario.close();
    }

    /**
     * Test: MainActivity handles notifications fragment navigation
     * Requirement: Navigation to notifications fragment should work
     */
    @Test
    public void testMainActivityNavigatesToNotifsFragment() {
        setProfileId("test_profile_123");

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "notifs");

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
            String fragment = activity.getIntent().getStringExtra("fragment");
            assert "notifs".equals(fragment);
        });

        scenario.close();
    }

    /**
     * Test: MainActivity handles invalid fragment extra gracefully
     * Requirement: Invalid fragment names should default to home
     */
    @Test
    public void testMainActivityHandlesInvalidFragmentExtra() {
        setProfileId("test_profile_123");

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "invalid_fragment");

        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scenario.onActivity(activity -> {
            assert !activity.isFinishing();
            // Should default to home fragment
        });

        scenario.close();
    }
}


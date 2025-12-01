package com.example.gitcat_events.uiTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gitcat_events.MainActivity;
import com.example.gitcat_events.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NavigationBarTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void skipIntroIfPresent() {
        // Clear SharedPreferences to ensure fresh state
        InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("GitCatEventsPrefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Wait for activity to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if SetupProfileActivity is displayed (indicated by "Welcome!" text)
        boolean setupProfileVisible = false;
        for (int i = 0; i < 10; i++) {
            try {
                onView(withText("Welcome!")).check(matches(isDisplayed()));
                setupProfileVisible = true;
                break;
            } catch (Exception e) {
                // Not visible yet, wait and retry
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        if (setupProfileVisible) {
            // Click skip button to create minimal profile
            try {
                onView(withId(R.id.btnSkip)).perform(click());
                
                // Wait for profile creation and MainActivity to appear
                // Poll for bottom navigation which indicates MainActivity is displayed
                boolean mainActivityVisible = false;
                for (int i = 0; i < 20; i++) {
                    try {
                        onView(withId(R.id.home)).check(matches(isDisplayed()));
                        mainActivityVisible = true;
                        break;
                    } catch (Exception e) {
                        // Not visible yet, wait and retry
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
                
                if (!mainActivityVisible) {
                    // Give it more time
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                // Skip button not found or click failed, continue anyway
                e.printStackTrace();
            }
        } else {
            // SetupProfileActivity not shown, assume profile already exists
            // Wait a bit for MainActivity to be ready
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method to ensure navigation bar is visible and ready
     */
    private void ensureNavigationReady() {
        // Wait for navigation bar to be visible
        boolean navigationReady = false;
        for (int i = 0; i < 10; i++) {
            try {
                onView(withId(R.id.home)).check(matches(isDisplayed()));
                navigationReady = true;
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        
        if (!navigationReady) {
            // Give it more time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testHomeMenuDisplaysHomeFragment() {
        ensureNavigationReady();
        onView(withId(R.id.home)).perform(click());
        
        // Wait for fragment to load and check for a visible element within the fragment
        // Poll for fragment content to appear
        boolean fragmentVisible = false;
        for (int i = 0; i < 10; i++) {
            try {
                // Check for a specific element in the home fragment (like the header text)
                onView(withId(R.id.fragment_home)).check(matches(isDisplayed()));
                fragmentVisible = true;
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        
        if (!fragmentVisible) {
            // Give it more time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Final check - if fragment still not visible, the test will fail with a clear message
        onView(withId(R.id.fragment_home)).check(matches(isDisplayed()));
    }

    @Test
    public void testNotifsMenuDisplaysNotifsFragment() {
        ensureNavigationReady();
        onView(withId(R.id.notifs)).perform(click());
        
        // Wait for fragment to load
        boolean fragmentVisible = false;
        for (int i = 0; i < 10; i++) {
            try {
                onView(withId(R.id.fragment_notifs)).check(matches(isDisplayed()));
                fragmentVisible = true;
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        
        if (!fragmentVisible) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        onView(withId(R.id.fragment_notifs)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateMenuDisplaysCreateFragment() {
        ensureNavigationReady();
        onView(withId(R.id.create)).perform(click());
        
        // Wait for fragment to load
        boolean fragmentVisible = false;
        for (int i = 0; i < 10; i++) {
            try {
                onView(withId(R.id.fragment_create)).check(matches(isDisplayed()));
                fragmentVisible = true;
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        
        if (!fragmentVisible) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        onView(withId(R.id.fragment_create)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileMenuDisplaysProfileFragment() {
        ensureNavigationReady();
        onView(withId(R.id.profile)).perform(click());
        
        // Wait for fragment to load
        boolean fragmentVisible = false;
        for (int i = 0; i < 10; i++) {
            try {
                onView(withId(R.id.fragment_profile)).check(matches(isDisplayed()));
                fragmentVisible = true;
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        
        if (!fragmentVisible) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        onView(withId(R.id.fragment_profile)).check(matches(isDisplayed()));
    }
}

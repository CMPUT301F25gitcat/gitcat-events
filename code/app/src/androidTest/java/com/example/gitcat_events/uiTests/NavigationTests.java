package com.example.gitcat_events.uiTests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.gitcat_events.R;

import com.example.gitcat_events.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NavigationTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testHomeMenuDisplaysHomeFragment() {
        onView(withId(R.id.home)).perform(click());
        onView(withId(R.id.fragment_home)).check(matches(isDisplayed()));
        onView(withId(R.id.home)).check(matches(isDisplayed()));
    }

    @Test
    public void testNotifsMenuDisplaysNotifsFragment() {
        onView(withId(R.id.notifs)).perform(click());
        onView(withId(R.id.fragment_notifs)).check(matches(isDisplayed()));
        onView(withId(R.id.notifs)).check(matches(isDisplayed()));
    }

    @Test
    public void testCreateMenuDisplaysCreateFragment() {
        onView(withId(R.id.create)).perform(click());
        onView(withId(R.id.fragment_create)).check(matches(isDisplayed()));
        onView(withId(R.id.create)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileMenuDisplaysProfileFragment() {
        onView(withId(R.id.profile)).perform(click());
        onView(withId(R.id.fragment_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.profile)).check(matches(isDisplayed()));
    }
}

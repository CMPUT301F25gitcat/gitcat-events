package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.gitcat_events.core.model.Profile;

@RunWith(AndroidJUnit4.class)
public class ProfileClassTests {

    @Test
    public void testConstructorAndGetters() {
        Profile profile = new Profile("Alice", "alice@example.com", "5551234");

        assertEquals("Alice", profile.getName());
        assertEquals("alice@example.com", profile.getEmail());
        assertEquals("5551234", profile.getPhone());
    }

    @Test
    public void testNullablePhone() {
        Profile profile = new Profile("Bob", "bob@example.com", null);

        assertEquals("Bob", profile.getName());
        assertEquals("bob@example.com", profile.getEmail());
        assertNull(profile.getPhone());
    }

    @Test
    public void testDefaultConstructorAndSetters() {
        Profile profile = new Profile();

        profile.setName("Charlie");
        profile.setEmail("charlie@example.com");
        profile.setPhone("9998887");

        assertEquals("Charlie", profile.getName());
        assertEquals("charlie@example.com", profile.getEmail());
        assertEquals("9998887", profile.getPhone());
    }

    @Test
    public void testSetterUpdates() {
        Profile profile = new Profile("Initial", "initial@email.com", "12345");

        profile.setName("Updated Name");
        profile.setEmail("updated@email.com");
        profile.setPhone("67890");

        assertEquals("Updated Name", profile.getName());
        assertEquals("updated@email.com", profile.getEmail());
        assertEquals("67890", profile.getPhone());
    }

    @Test
    public void testSetPhoneToNull() {
        Profile profile = new Profile("Dave", "dave@example.com", "1234567");

        profile.setPhone(null);
        assertNull(profile.getPhone());
    }
}

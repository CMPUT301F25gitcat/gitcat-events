package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.gitcat_events.core.model.Profile;

/**
 * Unit tests for Profile model
 * Tests profile creation with optional fields and deviceId requirement
 */
@RunWith(AndroidJUnit4.class)
public class ProfileClassTests {

    @Test
    public void testConstructorWithAllFields() {
        Profile profile = new Profile("Alice", "alice@example.com", "5551234", "device123", null, false);

        assertEquals("Alice", profile.getName());
        assertEquals("alice@example.com", profile.getEmail());
        assertEquals("5551234", profile.getPhone());
        assertEquals("device123", profile.getDeviceId());
        assertNull(profile.getProfilePictureUrl());
    }

    @Test
    public void testMinimalConstructorWithDeviceIdOnly() {
        Profile profile = new Profile("device456");

        assertEquals("device456", profile.getDeviceId());
        assertNull(profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getPhone());
        assertNull(profile.getProfilePictureUrl());
    }

    @Test
    public void testNullableOptionalFields() {
        Profile profile = new Profile(null, null, null, "device789", null, false);

        assertNull(profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getPhone());
        assertEquals("device789", profile.getDeviceId());
        assertNull(profile.getProfilePictureUrl());
    }

    @Test
    public void testNoArgConstructor() {
        Profile profile = new Profile();
        assertNotNull(profile);
    }

    @Test
    public void testDefaultConstructorAndSetters() {
        Profile profile = new Profile();

        profile.setName("Charlie");
        profile.setEmail("charlie@example.com");
        profile.setPhone("9998887");
        profile.setDeviceId("deviceCharlie");
        profile.setProfilePictureUrl("https://example.com/pic.jpg");

        assertEquals("Charlie", profile.getName());
        assertEquals("charlie@example.com", profile.getEmail());
        assertEquals("9998887", profile.getPhone());
        assertEquals("deviceCharlie", profile.getDeviceId());
        assertEquals("https://example.com/pic.jpg", profile.getProfilePictureUrl());
    }

    @Test
    public void testSetterUpdates() {
        Profile profile = new Profile("Initial", "initial@email.com", "12345", "device1", null, false);

        profile.setName("Updated Name");
        profile.setEmail("updated@email.com");
        profile.setPhone("67890");
        profile.setDeviceId("deviceUpdated");

        assertEquals("Updated Name", profile.getName());
        assertEquals("updated@email.com", profile.getEmail());
        assertEquals("67890", profile.getPhone());
        assertEquals("deviceUpdated", profile.getDeviceId());
    }

    @Test
    public void testSetPhoneToNull() {
        Profile profile = new Profile("Dave", "dave@example.com", "1234567", "deviceDave", null, false);

        profile.setPhone(null);
        assertNull(profile.getPhone());
        // Other fields should remain unchanged
        assertEquals("Dave", profile.getName());
        assertEquals("deviceDave", profile.getDeviceId());
    }

    @Test
    public void testSetNameToNull() {
        Profile profile = new Profile("Bob", "bob@example.com", "5551234", "deviceBob", null, false);

        profile.setName(null);
        assertNull(profile.getName());
        // Other fields should remain unchanged
        assertEquals("bob@example.com", profile.getEmail());
        assertEquals("deviceBob", profile.getDeviceId());
    }

    @Test
    public void testSetEmailToNull() {
        Profile profile = new Profile("Eve", "eve@example.com", "5559999", "deviceEve", null, false);

        profile.setEmail(null);
        assertNull(profile.getEmail());
        // Other fields should remain unchanged
        assertEquals("Eve", profile.getName());
        assertEquals("deviceEve", profile.getDeviceId());
    }

    @Test
    public void testProfilePictureUrl() {
        String pictureUrl = "data:image/png;base64,iVBORw0KGgoAAAANS...";
        Profile profile = new Profile("Frank", "frank@example.com", "5557777", "deviceFrank", pictureUrl, false);

        assertEquals(pictureUrl, profile.getProfilePictureUrl());

        profile.setProfilePictureUrl(null);
        assertNull(profile.getProfilePictureUrl());
    }

    @Test
    public void testMinimalProfileCreation() {
        // Test that a minimal profile (deviceId only) can be created
        Profile profile = new Profile("device_minimal_123");

        assertEquals("device_minimal_123", profile.getDeviceId());
        assertNull(profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getPhone());
        assertNull(profile.getProfilePictureUrl());
    }

    @Test
    public void testDeviceIdIsRequired() {
        // Test that deviceId can be set and retrieved
        Profile profile = new Profile();
        profile.setDeviceId("required_device_id");

        assertEquals("required_device_id", profile.getDeviceId());
    }
}

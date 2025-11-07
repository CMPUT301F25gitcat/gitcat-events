package com.example.gitcat_events.testData;

import com.example.gitcat_events.core.model.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Realistic test profile data
 * Contains pre-defined profile scenarios for testing
 */
public class TestProfileData {

    /**
     * Returns a map of realistic names and emails for testing
     */
    public static Map<String, String> getNamesAndEmails() {
        Map<String, String> profiles = new HashMap<>();
        profiles.put("John Doe", "john.doe@example.com");
        profiles.put("Alice Johnson", "alice.j@example.com");
        profiles.put("Bob Williams", "bob.williams@example.com");
        profiles.put("Carol Brown", "carol.brown@example.com");
        profiles.put("David Lee", "david.lee@example.com");
        profiles.put("Emma Davis", "emma.davis@example.com");
        profiles.put("Frank Miller", "frank.miller@example.com");
        profiles.put("Grace Wilson", "grace.wilson@example.com");
        return profiles;
    }

    /**
     * Returns realistic phone numbers for testing
     */
    public static String[] getPhoneNumbers() {
        return new String[]{
                "780-123-4567",
                "780-234-5678",
                "780-345-6789",
                "780-456-7890",
                "780-567-8901",
                "403-123-4567",
                "403-234-5678",
                "587-123-4567"
        };
    }

    /**
     * Returns realistic device IDs (UUID format)
     */
    public static String[] getDeviceIds() {
        return new String[]{
                "550e8400-e29b-41d4-a716-446655440000",
                "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
                "6ba7b811-9dad-11d1-80b4-00c04fd430c8",
                "6ba7b812-9dad-11d1-80b4-00c04fd430c8",
                "6ba7b813-9dad-11d1-80b4-00c04fd430c8",
                "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "8d0e6679-7425-40de-944b-e07fc1f90ae7",
                "9e1f6679-7425-40de-944b-e07fc1f90ae7"
        };
    }

    /**
     * Creates a profile with all fields filled
     */
    public static Profile createCompleteProfile(String deviceId) {
        return new Profile(
                "John Doe",
                "john.doe@example.com",
                "780-123-4567",
                deviceId,
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD..."
        );
    }

    /**
     * Creates a profile with only name and email
     */
    public static Profile createNameEmailProfile(String deviceId) {
        Profile profile = new Profile(deviceId);
        profile.setName("Alice Johnson");
        profile.setEmail("alice.j@example.com");
        return profile;
    }

    /**
     * Creates a profile with only name
     */
    public static Profile createNameOnlyProfile(String deviceId) {
        Profile profile = new Profile(deviceId);
        profile.setName("Bob Williams");
        return profile;
    }

    /**
     * Creates a profile with only email
     */
    public static Profile createEmailOnlyProfile(String deviceId) {
        Profile profile = new Profile(deviceId);
        profile.setEmail("carol.brown@example.com");
        return profile;
    }

    /**
     * Creates a profile with only phone
     */
    public static Profile createPhoneOnlyProfile(String deviceId) {
        Profile profile = new Profile(deviceId);
        profile.setPhone("780-234-5678");
        return profile;
    }

    /**
     * Creates a minimal profile (device ID only)
     */
    public static Profile createMinimalProfile(String deviceId) {
        return new Profile(deviceId);
    }

    /**
     * Returns realistic profile picture URLs (Base64 encoded strings)
     */
    public static String[] getProfilePictureUrls() {
        return new String[]{
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...",
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
                null, // No picture
                null  // No picture
        };
    }

    /**
     * Creates profiles with various combinations of optional fields
     */
    public static Profile[] createProfileVariations(String deviceId) {
        return new Profile[]{
                createCompleteProfile(deviceId),
                createNameEmailProfile(deviceId),
                createNameOnlyProfile(deviceId),
                createEmailOnlyProfile(deviceId),
                createPhoneOnlyProfile(deviceId),
                createMinimalProfile(deviceId)
        };
    }
}


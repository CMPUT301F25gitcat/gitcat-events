package com.example.gitcat_events.entrantTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Profile;

/**
 * Unit tests for Profile Management
 * Tests US 01.02.01: As an entrant I want to create a profile
 * Tests US 01.02.02: As an entrant I want to edit my profile
 * Tests US 01.02.03: As an entrant I want to view my profile
 */
public class ProfileManagementTests {

    private Profile testProfile;
    private String testDeviceId;
    private String testName;
    private String testEmail;
    private String testPhone;

    @Before
    public void setUp() {
        testDeviceId = "device-123";
        testName = "John Doe";
        testEmail = "john.doe@example.com";
        testPhone = "555-1234";

        testProfile = new Profile();
        testProfile.setDeviceId(testDeviceId);
        testProfile.setName(testName);
        testProfile.setEmail(testEmail);
        testProfile.setPhone(testPhone);
    }

    @Test
    public void testCreateProfile() {
        Profile newProfile = new Profile();
        newProfile.setDeviceId("device-456");
        newProfile.setName("Jane Smith");
        
        assertNotNull("Profile should be created", newProfile);
        assertNotNull("Device ID should be set", newProfile.getDeviceId());
        assertNotNull("Name should be set", newProfile.getName());
    }

    @Test
    public void testProfileDeviceId() {
        assertEquals("Device ID should match", testDeviceId, testProfile.getDeviceId());
    }

    @Test
    public void testProfileName() {
        assertEquals("Name should match", testName, testProfile.getName());
    }

    @Test
    public void testProfileEmail() {
        assertEquals("Email should match", testEmail, testProfile.getEmail());
    }

    @Test
    public void testProfilePhone() {
        assertEquals("Phone should match", testPhone, testProfile.getPhone());
    }

    @Test
    public void testEditProfileName() {
        String newName = "Jane Doe";
        testProfile.setName(newName);
        
        assertEquals("Name should be updated", newName, testProfile.getName());
        assertNotEquals("Name should be different from original", testName, testProfile.getName());
    }

    @Test
    public void testEditProfileEmail() {
        String newEmail = "jane.doe@example.com";
        testProfile.setEmail(newEmail);
        
        assertEquals("Email should be updated", newEmail, testProfile.getEmail());
        assertNotEquals("Email should be different from original", testEmail, testProfile.getEmail());
    }

    @Test
    public void testEditProfilePhone() {
        String newPhone = "555-5678";
        testProfile.setPhone(newPhone);
        
        assertEquals("Phone should be updated", newPhone, testProfile.getPhone());
        assertNotEquals("Phone should be different from original", testPhone, testProfile.getPhone());
    }

    @Test
    public void testEditMultipleProfileFields() {
        String newName = "Updated Name";
        String newEmail = "updated@example.com";
        String newPhone = "555-9999";
        
        testProfile.setName(newName);
        testProfile.setEmail(newEmail);
        testProfile.setPhone(newPhone);
        
        assertEquals("Name should be updated", newName, testProfile.getName());
        assertEquals("Email should be updated", newEmail, testProfile.getEmail());
        assertEquals("Phone should be updated", newPhone, testProfile.getPhone());
    }

    @Test
    public void testViewProfile() {
        // Viewing profile should return all fields
        assertNotNull("Device ID should be viewable", testProfile.getDeviceId());
        assertNotNull("Name should be viewable", testProfile.getName());
        assertNotNull("Email should be viewable", testProfile.getEmail());
        assertNotNull("Phone should be viewable", testProfile.getPhone());
    }

    @Test
    public void testProfileWithNullFields() {
        Profile profileWithNulls = new Profile();
        profileWithNulls.setDeviceId("device-789");
        profileWithNulls.setName(null);
        profileWithNulls.setEmail(null);
        
        assertNotNull("Device ID should not be null", profileWithNulls.getDeviceId());
        assertNull("Name can be null", profileWithNulls.getName());
        assertNull("Email can be null", profileWithNulls.getEmail());
    }

    @Test
    public void testProfilePictureUrl() {
        String pictureUrl = "https://example.com/profile.jpg";
        testProfile.setProfilePictureUrl(pictureUrl);
        
        assertEquals("Profile picture URL should be set", pictureUrl, testProfile.getProfilePictureUrl());
    }

    @Test
    public void testUpdateProfilePicture() {
        String initialPicture = "https://example.com/old.jpg";
        String newPicture = "https://example.com/new.jpg";
        
        testProfile.setProfilePictureUrl(initialPicture);
        assertEquals("Initial picture should be set", initialPicture, testProfile.getProfilePictureUrl());
        
        testProfile.setProfilePictureUrl(newPicture);
        assertEquals("Picture should be updated", newPicture, testProfile.getProfilePictureUrl());
        assertNotEquals("New picture should be different", initialPicture, testProfile.getProfilePictureUrl());
    }

    @Test
    public void testRemoveProfilePicture() {
        String pictureUrl = "https://example.com/profile.jpg";
        testProfile.setProfilePictureUrl(pictureUrl);
        assertNotNull("Picture should be set", testProfile.getProfilePictureUrl());
        
        testProfile.setProfilePictureUrl(null);
        assertNull("Picture should be removed", testProfile.getProfilePictureUrl());
    }

    @Test
    public void testProfileUniquenessByDeviceId() {
        Profile profile1 = new Profile();
        profile1.setDeviceId("device-1");
        profile1.setName("User 1");
        
        Profile profile2 = new Profile();
        profile2.setDeviceId("device-2");
        profile2.setName("User 2");
        
        assertNotEquals("Different device IDs should create different profiles", 
                profile1.getDeviceId(), profile2.getDeviceId());
    }

    @Test
    public void testProfileDocumentId() {
        String documentId = "profile-doc-123";
        testProfile.setDocumentId(documentId);
        
        assertEquals("Document ID should be set", documentId, testProfile.getDocumentId());
    }

    @Test
    public void testProfilePersistence() {
        // Test that profile fields persist through operations
        String originalName = testProfile.getName();
        String originalEmail = testProfile.getEmail();
        
        // Perform other operations
        testProfile.setPhone("555-0000");
        
        // Original fields should still be there
        assertEquals("Name should persist", originalName, testProfile.getName());
        assertEquals("Email should persist", originalEmail, testProfile.getEmail());
    }

    @Test
    public void testProfileWithEmptyStrings() {
        Profile profile = new Profile();
        profile.setDeviceId("device-empty");
        profile.setName("");
        profile.setEmail("");
        profile.setPhone("");
        
        assertEquals("Empty name should be allowed", "", profile.getName());
        assertEquals("Empty email should be allowed", "", profile.getEmail());
        assertEquals("Empty phone should be allowed", "", profile.getPhone());
    }

    @Test
    public void testProfileWithSpecialCharacters() {
        String nameWithSpecialChars = "José O'Brien-Smith";
        String emailWithSpecialChars = "test+user@example.com";
        
        testProfile.setName(nameWithSpecialChars);
        testProfile.setEmail(emailWithSpecialChars);
        
        assertEquals("Should handle special characters in name", nameWithSpecialChars, testProfile.getName());
        assertEquals("Should handle special characters in email", emailWithSpecialChars, testProfile.getEmail());
    }
}


package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

/**
 * Unit tests for US 02.04.01: As an organizer I want to upload an event poster
 * Unit tests for US 02.04.02: As an organizer I want to update an event poster
 */
public class EventPosterTests {

    private Event eventWithPoster;
    private Event eventWithoutPoster;
    private String testPosterBase64;
    private Calendar eventDate;

    @Before
    public void setUp() {
        eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        // Simulate a Base64 encoded image
        testPosterBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

        eventWithPoster = new Event(
                "Event with Poster",
                "Event that has a poster",
                100,
                null,
                testPosterBase64,
                null,
                null,
                eventDate,
                false
        );

        eventWithoutPoster = new Event(
                "Event without Poster",
                "Event that has no poster",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
    }

    @Test
    public void testEventHasPoster() {
        assertNotNull("Event should have a poster", eventWithPoster.getPoster());
        assertEquals("Poster should match uploaded data", testPosterBase64, eventWithPoster.getPoster());
    }

    @Test
    public void testEventWithoutPoster() {
        assertNull("Event should not have a poster", eventWithoutPoster.getPoster());
    }

    @Test
    public void testUploadPoster() {
        String newPoster = "newBase64EncodedImageData";
        eventWithoutPoster.setPoster(newPoster);
        
        assertNotNull("Event should now have a poster", eventWithoutPoster.getPoster());
        assertEquals("Poster should match uploaded data", newPoster, eventWithoutPoster.getPoster());
    }

    @Test
    public void testUpdatePoster() {
        String oldPoster = eventWithPoster.getPoster();
        String newPoster = "updatedBase64EncodedImageData";
        
        eventWithPoster.setPoster(newPoster);
        
        assertNotEquals("New poster should be different from old", oldPoster, eventWithPoster.getPoster());
        assertEquals("Poster should match updated data", newPoster, eventWithPoster.getPoster());
    }

    @Test
    public void testRemovePoster() {
        assertNotNull("Event initially has a poster", eventWithPoster.getPoster());
        
        eventWithPoster.setPoster(null);
        
        assertNull("Poster should be removed", eventWithPoster.getPoster());
    }

    @Test
    public void testReplacePosterMultipleTimes() {
        String poster1 = "base64Data1";
        String poster2 = "base64Data2";
        String poster3 = "base64Data3";
        
        eventWithoutPoster.setPoster(poster1);
        assertEquals("First poster should be set", poster1, eventWithoutPoster.getPoster());
        
        eventWithoutPoster.setPoster(poster2);
        assertEquals("Second poster should replace first", poster2, eventWithoutPoster.getPoster());
        
        eventWithoutPoster.setPoster(poster3);
        assertEquals("Third poster should replace second", poster3, eventWithoutPoster.getPoster());
    }

    @Test
    public void testPosterPersistence() {
        // Test that poster is maintained through getter/setter operations
        String originalPoster = eventWithPoster.getPoster();
        
        // Perform other operations
        eventWithPoster.setName("Updated Name");
        eventWithPoster.setDescription("Updated Description");
        
        // Poster should still be the same
        assertEquals("Poster should remain unchanged", originalPoster, eventWithPoster.getPoster());
    }

    @Test
    public void testEmptyStringPoster() {
        eventWithoutPoster.setPoster("");
        
        assertNotNull("Empty string poster is not null", eventWithoutPoster.getPoster());
        assertEquals("Poster should be empty string", "", eventWithoutPoster.getPoster());
    }

    @Test
    public void testLargeBase64Poster() {
        // Simulate a large Base64 string (1000 characters)
        StringBuilder largePoster = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largePoster.append("A");
        }
        
        eventWithoutPoster.setPoster(largePoster.toString());
        
        assertNotNull("Large poster should be set", eventWithoutPoster.getPoster());
        assertEquals("Large poster length should match", 1000, eventWithoutPoster.getPoster().length());
    }
}


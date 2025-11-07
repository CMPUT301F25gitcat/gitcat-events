package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

/**
 * Unit tests for US 02.02.03: As an organizer I want to enable or disable the geolocation requirement for my event.
 */
public class GeolocationRequirementTests {

    private Event eventWithGeolocation;
    private Event eventWithoutGeolocation;
    private Calendar eventDate;

    @Before
    public void setUp() {
        eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        eventWithGeolocation = new Event(
                "Geolocation Event",
                "Event requiring geolocation",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                true // Geolocation required
        );

        eventWithoutGeolocation = new Event(
                "No Geolocation Event",
                "Event not requiring geolocation",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false // Geolocation not required
        );
    }

    @Test
    public void testGeolocationEnabled() {
        assertTrue("Geolocation should be required", eventWithGeolocation.getGeoLocationRequired());
    }

    @Test
    public void testGeolocationDisabled() {
        assertFalse("Geolocation should not be required", eventWithoutGeolocation.getGeoLocationRequired());
    }

    @Test
    public void testEnableGeolocation() {
        eventWithoutGeolocation.setGeoLocationRequired(true);
        
        assertTrue("Geolocation should be enabled", eventWithoutGeolocation.getGeoLocationRequired());
    }

    @Test
    public void testDisableGeolocation() {
        eventWithGeolocation.setGeoLocationRequired(false);
        
        assertFalse("Geolocation should be disabled", eventWithGeolocation.getGeoLocationRequired());
    }

    @Test
    public void testToggleGeolocation() {
        Event event = new Event(
                "Toggle Event",
                "Testing toggle",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        
        // Toggle on
        event.setGeoLocationRequired(true);
        assertTrue("Should be enabled after first toggle", event.getGeoLocationRequired());
        
        // Toggle off
        event.setGeoLocationRequired(false);
        assertFalse("Should be disabled after second toggle", event.getGeoLocationRequired());
    }

    @Test
    public void testGeolocationNotNullAfterCreation() {
        Event event = new Event(
                "Test Event",
                "Description",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        
        assertNotNull("Geolocation requirement should not be null", event.getGeoLocationRequired());
    }

    @Test
    public void testGeolocationDefaultValue() {
        // When explicitly set to false
        Event event = new Event(
                "Test Event",
                "Description",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        
        assertEquals("Default geolocation should be false", false, event.getGeoLocationRequired());
    }

    @Test
    public void testMultipleEventsWithDifferentGeolocationSettings() {
        Event event1 = new Event("Event 1", "Desc", 100, null, null, null, null, eventDate, true);
        Event event2 = new Event("Event 2", "Desc", 100, null, null, null, null, eventDate, false);
        Event event3 = new Event("Event 3", "Desc", 100, null, null, null, null, eventDate, true);
        
        assertTrue("Event 1 should require geolocation", event1.getGeoLocationRequired());
        assertFalse("Event 2 should not require geolocation", event2.getGeoLocationRequired());
        assertTrue("Event 3 should require geolocation", event3.getGeoLocationRequired());
    }
}


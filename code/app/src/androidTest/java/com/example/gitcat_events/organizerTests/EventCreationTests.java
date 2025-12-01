package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

/**
 * Unit tests for Event Creation and Editing
 * Tests US 02.01.01: As an organizer I want to create an event
 * Tests US 02.01.03: As an organizer I want to edit my event
 */
public class EventCreationTests {

    private Calendar eventDate;
    private Calendar registrationStartDate;
    private Calendar raffleDate;

    @Before
    public void setUp() {
        eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        registrationStartDate = Calendar.getInstance();
        registrationStartDate.add(Calendar.DAY_OF_MONTH, 1);

        raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, 15);
    }

    @Test
    public void testCreateEvent() {
        Event event = new Event(
                "New Event",
                "Event description",
                100,
                null,
                null,
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );

        assertNotNull("Event should be created", event);
        assertEquals("Name should match", "New Event", event.getName());
        assertEquals("Description should match", "Event description", event.getDescription());
        assertEquals("Capacity should match", 100, event.getCapacity());
    }

    @Test
    public void testCreateEventWithAllFields() {
        String poster = "base64EncodedImage";
        Event event = new Event(
                "Complete Event",
                "Full description",
                200,
                50, // maxWaitListSize
                poster,
                registrationStartDate,
                raffleDate,
                eventDate,
                true // geoLocationRequired
        );

        assertEquals("Name should match", "Complete Event", event.getName());
        assertEquals("Capacity should match", 200, event.getCapacity());
        assertEquals("Max waitlist should match", (Integer) 50, event.getMaxWaitListSize());
        assertEquals("Poster should match", poster, event.getPoster());
        assertTrue("Geolocation should be required", event.getGeoLocationRequired());
    }

    @Test
    public void testCreateEventWithOptionalFields() {
        Event event = new Event(
                "Optional Fields Event",
                "Description",
                50,
                null, // No max waitlist
                null, // No poster
                null, // No registration start
                null, // No raffle date
                eventDate,
                false
        );

        assertNull("Max waitlist can be null", event.getMaxWaitListSize());
        assertNull("Poster can be null", event.getPoster());
        assertNull("Registration start can be null", event.getRegistrationStartDate());
        assertNull("Raffle date can be null", event.getRaffleDate());
    }

    @Test
    public void testEditEventName() {
        Event event = createTestEvent();
        String newName = "Updated Event Name";
        
        event.setName(newName);
        
        assertEquals("Name should be updated", newName, event.getName());
    }

    @Test
    public void testEditEventDescription() {
        Event event = createTestEvent();
        String newDescription = "Updated description";
        
        event.setDescription(newDescription);
        
        assertEquals("Description should be updated", newDescription, event.getDescription());
    }

    @Test
    public void testEditEventCapacity() {
        Event event = createTestEvent();
        int newCapacity = 250;
        
        event.setCapacity(newCapacity);
        
        assertEquals("Capacity should be updated", newCapacity, event.getCapacity());
    }

    @Test
    public void testEditEventMaxWaitlist() {
        Event event = createTestEvent();
        Integer newMaxWaitlist = 75;
        
        event.setMaxWaitListSize(newMaxWaitlist);
        
        assertEquals("Max waitlist should be updated", newMaxWaitlist, event.getMaxWaitListSize());
    }

    @Test
    public void testEditEventRemoveMaxWaitlist() {
        Event event = createTestEvent();
        event.setMaxWaitListSize(50);
        assertNotNull("Max waitlist should be set", event.getMaxWaitListSize());
        
        event.setMaxWaitListSize(null);
        
        assertNull("Max waitlist should be removed", event.getMaxWaitListSize());
    }

    @Test
    public void testEditEventPoster() {
        Event event = createTestEvent();
        String newPoster = "newBase64Image";
        
        event.setPoster(newPoster);
        
        assertEquals("Poster should be updated", newPoster, event.getPoster());
    }

    @Test
    public void testEditEventDates() {
        Event event = createTestEvent();
        
        Calendar newRegistrationStart = Calendar.getInstance();
        newRegistrationStart.add(Calendar.DAY_OF_MONTH, 5);
        
        Calendar newRaffleDate = Calendar.getInstance();
        newRaffleDate.add(Calendar.DAY_OF_MONTH, 20);
        
        Calendar newEventDate = Calendar.getInstance();
        newEventDate.add(Calendar.DAY_OF_MONTH, 35);
        
        event.setRegistrationStartDate(newRegistrationStart);
        event.setRaffleDate(newRaffleDate);
        event.setEventDate(newEventDate);
        
        assertEquals("Registration start should be updated", newRegistrationStart, event.getRegistrationStartDate());
        assertEquals("Raffle date should be updated", newRaffleDate, event.getRaffleDate());
        assertEquals("Event date should be updated", newEventDate, event.getEventDate());
    }

    @Test
    public void testEditEventGeolocationRequirement() {
        Event event = createTestEvent();
        assertFalse("Geolocation should not be required initially", event.getGeoLocationRequired());
        
        event.setGeoLocationRequired(true);
        
        assertTrue("Geolocation should be required after update", event.getGeoLocationRequired());
    }

    @Test
    public void testEditMultipleFields() {
        Event event = createTestEvent();
        
        event.setName("Updated Name");
        event.setDescription("Updated Description");
        event.setCapacity(300);
        event.setMaxWaitListSize(100);
        event.setGeoLocationRequired(true);
        
        assertEquals("Name should be updated", "Updated Name", event.getName());
        assertEquals("Description should be updated", "Updated Description", event.getDescription());
        assertEquals("Capacity should be updated", 300, event.getCapacity());
        assertEquals("Max waitlist should be updated", (Integer) 100, event.getMaxWaitListSize());
        assertTrue("Geolocation should be required", event.getGeoLocationRequired());
    }

    @Test
    public void testEventValidation_CapacityPositive() {
        Event event = createTestEvent();
        
        // Capacity should be positive (validation would happen in UI)
        assertTrue("Capacity should be positive", event.getCapacity() > 0);
    }

    @Test
    public void testEventValidation_MaxWaitlistPositive() {
        Event event = createTestEvent();
        event.setMaxWaitListSize(50);
        
        assertTrue("Max waitlist should be positive if set", event.getMaxWaitListSize() > 0);
    }

    @Test
    public void testEventDocumentId() {
        Event event = createTestEvent();
        String documentId = "event-doc-123";
        
        event.setDocumentId(documentId);
        
        assertEquals("Document ID should be set", documentId, event.getDocumentId());
    }

    @Test
    public void testEventOrganizerDeviceId() {
        Event event = createTestEvent();
        String organizerId = "organizer-device-456";
        
        event.setOrganizerDeviceId(organizerId);
        
        assertEquals("Organizer device ID should be set", organizerId, event.getOrganizerDeviceId());
    }

    @Test
    public void testEventSelectionCriteria() {
        Event event = createTestEvent();
        String criteria = "Random selection from waitlist";
        
        event.setSelectionCriteria(criteria);
        
        assertEquals("Selection criteria should be set", criteria, event.getSelectionCriteria());
    }

    @Test
    public void testEventQRCodeUrl() {
        Event event = createTestEvent();
        String qrCodeUrl = "gitcatevents://event/123";
        
        event.setQrCodeUrl(qrCodeUrl);
        
        assertEquals("QR code URL should be set", qrCodeUrl, event.getQrCodeUrl());
    }

    @Test
    public void testEventWithEventTypes() {
        Event event = createTestEvent();
        java.util.List<String> eventTypes = new java.util.ArrayList<>();
        eventTypes.add("Technology");
        eventTypes.add("Networking");
        
        event.setEventTypes(eventTypes);
        
        assertNotNull("Event types should be set", event.getEventTypes());
        assertEquals("Should have 2 event types", 2, event.getEventTypes().size());
        assertTrue("Should contain Technology", event.getEventTypes().contains("Technology"));
        assertTrue("Should contain Networking", event.getEventTypes().contains("Networking"));
    }

    @Test
    public void testEventPersistence() {
        Event event = createTestEvent();
        String originalName = event.getName();
        int originalCapacity = event.getCapacity();
        
        // Modify other fields
        event.setMaxWaitListSize(50);
        event.setPoster("poster.png");
        
        // Original fields should persist
        assertEquals("Name should persist", originalName, event.getName());
        assertEquals("Capacity should persist", originalCapacity, event.getCapacity());
    }

    // Helper method to create a test event
    private Event createTestEvent() {
        return new Event(
                "Test Event",
                "Test Description",
                100,
                null,
                null,
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );
    }
}


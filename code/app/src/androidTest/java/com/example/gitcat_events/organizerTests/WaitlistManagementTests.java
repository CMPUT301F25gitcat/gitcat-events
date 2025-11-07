package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.WaitListEntry;

import java.util.Calendar;

/**
 * Unit tests for US 02.02.01: As an organizer I want to view the list of entrants who joined my event waiting list
 * Unit tests for US 02.03.01: As an organizer I want to OPTIONALLY limit the number of entrants who can join my waiting list.
 */
public class WaitlistManagementTests {

    private Event eventWithLimit;
    private Event eventWithoutLimit;
    private String testEventId;
    private String testUserId;

    @Before
    public void setUp() {
        testEventId = "test-event-123";
        testUserId = "test-user-456";

        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        // Event with waitlist limit
        eventWithLimit = new Event(
                "Limited Event",
                "Event with waitlist limit",
                50,
                100, // Max waitlist size
                null,
                null,
                null,
                eventDate,
                false
        );
        eventWithLimit.setDocumentId(testEventId);

        // Event without waitlist limit
        eventWithoutLimit = new Event(
                "Unlimited Event",
                "Event without waitlist limit",
                50,
                null, // No max waitlist size
                null,
                null,
                null,
                eventDate,
                false
        );
        eventWithoutLimit.setDocumentId(testEventId + "-unlimited");
    }

    @Test
    public void testEventHasWaitlistLimit() {
        assertNotNull("Event should have waitlist limit", eventWithLimit.getMaxWaitListSize());
        assertEquals("Waitlist limit should be 100", 100, (int) eventWithLimit.getMaxWaitListSize());
    }

    @Test
    public void testEventWithoutWaitlistLimit() {
        assertNull("Event should not have waitlist limit", eventWithoutLimit.getMaxWaitListSize());
    }

    @Test
    public void testSetWaitlistLimit() {
        Event event = new Event(
                "Test Event",
                "Description",
                50,
                null,
                null,
                null,
                null,
                Calendar.getInstance(),
                false
        );
        
        event.setMaxWaitListSize(75);
        
        assertNotNull("Waitlist limit should be set", event.getMaxWaitListSize());
        assertEquals("Waitlist limit should be 75", 75, (int) event.getMaxWaitListSize());
    }

    @Test
    public void testUpdateWaitlistLimit() {
        eventWithLimit.setMaxWaitListSize(150);
        
        assertEquals("Updated waitlist limit should be 150", 150, (int) eventWithLimit.getMaxWaitListSize());
    }

    @Test
    public void testRemoveWaitlistLimit() {
        eventWithLimit.setMaxWaitListSize(null);
        
        assertNull("Waitlist limit should be removed", eventWithLimit.getMaxWaitListSize());
    }

    @Test
    public void testWaitlistEntryCreation() {
        WaitListEntry entry = new WaitListEntry(testEventId, testUserId);
        
        assertEquals("Event ID should match", testEventId, entry.getEventId());
        assertEquals("User ID should match", testUserId, entry.getUserDeviceId());
    }

    @Test
    public void testWaitlistEntryGetters() {
        WaitListEntry entry = new WaitListEntry(testEventId, testUserId);
        
        assertNotNull("Event ID should not be null", entry.getEventId());
        assertNotNull("User ID should not be null", entry.getUserDeviceId());
    }

    @Test
    public void testMultipleWaitlistEntries() {
        WaitListEntry entry1 = new WaitListEntry(testEventId, "user1");
        WaitListEntry entry2 = new WaitListEntry(testEventId, "user2");
        WaitListEntry entry3 = new WaitListEntry(testEventId, "user3");
        
        assertNotEquals("Entries should have different user IDs", 
                entry1.getUserDeviceId(), entry2.getUserDeviceId());
        assertEquals("All entries should be for same event", 
                entry1.getEventId(), entry2.getEventId());
    }

    @Test
    public void testWaitlistLimitBoundary() {
        // Test boundary conditions
        eventWithLimit.setMaxWaitListSize(1);
        assertEquals("Should accept waitlist limit of 1", 1, (int) eventWithLimit.getMaxWaitListSize());
        
        eventWithLimit.setMaxWaitListSize(1000);
        assertEquals("Should accept large waitlist limit", 1000, (int) eventWithLimit.getMaxWaitListSize());
    }

    @Test
    public void testWaitlistLimitVsCapacity() {
        // Waitlist limit should be independent of event capacity
        assertTrue("Waitlist limit can be greater than capacity",
                eventWithLimit.getMaxWaitListSize() > eventWithLimit.getCapacity());
    }
}


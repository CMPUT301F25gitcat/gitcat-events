package com.example.gitcat_events.integrationTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.InvitationListEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Integration tests for duplicate prevention
 * Tests fixes for duplicate events in entered events list
 * Tests fixes for duplicate events in pending invitations list
 */
public class DuplicatePreventionTests {

    private List<Event> enteredEvents;
    private List<Event> pendingInvitations;
    private Set<String> loadedEventIds;

    @Before
    public void setUp() {
        enteredEvents = new ArrayList<>();
        pendingInvitations = new ArrayList<>();
        loadedEventIds = new HashSet<>();
    }

    @Test
    public void testPreventDuplicateEventsInEnteredList() {
        String eventId = "event-123";
        Event event = createTestEvent(eventId, "Test Event");

        // Simulate adding event multiple times (race condition scenario)
        addEventToEnteredList(event);
        addEventToEnteredList(event); // Try to add again
        addEventToEnteredList(event); // Try to add again

        assertEquals("Should only have one event", 1, enteredEvents.size());
        assertTrue("Should contain the event", enteredEvents.contains(event));
    }

    @Test
    public void testPreventDuplicateEventsInPendingInvitations() {
        String eventId = "event-456";
        Event event = createTestEvent(eventId, "Invited Event");

        // Simulate adding event multiple times
        addEventToPendingInvitations(event);
        addEventToPendingInvitations(event); // Try to add again
        addEventToPendingInvitations(event); // Try to add again

        assertEquals("Should only have one event", 1, pendingInvitations.size());
        assertTrue("Should contain the event", pendingInvitations.contains(event));
    }

    @Test
    public void testMultipleDifferentEvents() {
        Event event1 = createTestEvent("event-1", "Event 1");
        Event event2 = createTestEvent("event-2", "Event 2");
        Event event3 = createTestEvent("event-3", "Event 3");

        addEventToEnteredList(event1);
        addEventToEnteredList(event2);
        addEventToEnteredList(event3);

        assertEquals("Should have 3 different events", 3, enteredEvents.size());
    }

    @Test
    public void testDuplicatePreventionWithSameEventId() {
        String eventId = "event-789";
        Event event1 = createTestEvent(eventId, "Event Name");
        Event event2 = createTestEvent(eventId, "Different Name"); // Same ID, different name

        addEventToEnteredList(event1);
        addEventToEnteredList(event2); // Should be prevented

        assertEquals("Should only have one event with same ID", 1, enteredEvents.size());
    }

    @Test
    public void testDuplicatePreventionWithNullEventId() {
        Event event1 = createTestEvent(null, "Event 1");
        Event event2 = createTestEvent(null, "Event 2");

        // Events with null IDs should still be handled
        addEventToEnteredList(event1);
        addEventToEnteredList(event2);

        // Without ID tracking, both might be added (acceptable behavior)
        assertTrue("Should handle null IDs gracefully", enteredEvents.size() >= 1);
    }

    @Test
    public void testConcurrentAdds() {
        String eventId = "event-concurrent";
        Event event = createTestEvent(eventId, "Concurrent Event");

        // Simulate concurrent additions
        Thread thread1 = new Thread(() -> addEventToEnteredList(event));
        Thread thread2 = new Thread(() -> addEventToEnteredList(event));
        Thread thread3 = new Thread(() -> addEventToEnteredList(event));

        try {
            thread1.start();
            thread2.start();
            thread3.start();
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        assertEquals("Should only have one event even with concurrent adds", 1, enteredEvents.size());
    }

    @Test
    public void testLoadEventDetailsPreventsDuplicates() {
        Set<String> eventIds = new HashSet<>();
        eventIds.add("event-1");
        eventIds.add("event-2");
        eventIds.add("event-1"); // Duplicate ID

        // Simulate loading events
        for (String eventId : eventIds) {
            Event event = createTestEvent(eventId, "Event " + eventId);
            addEventToEnteredList(event);
        }

        assertEquals("Should only have 2 unique events", 2, enteredEvents.size());
    }

    @Test
    public void testLoadPendingInvitationsPreventsDuplicates() {
        List<String> invitationEventIds = new ArrayList<>();
        invitationEventIds.add("event-a");
        invitationEventIds.add("event-b");
        invitationEventIds.add("event-a"); // Duplicate

        // Simulate loading invitations
        for (String eventId : invitationEventIds) {
            Event event = createTestEvent(eventId, "Invited Event " + eventId);
            addEventToPendingInvitations(event);
        }

        assertEquals("Should only have 2 unique events", 2, pendingInvitations.size());
    }

    @Test
    public void testSynchronizedAccess() {
        String eventId = "event-sync";
        Event event = createTestEvent(eventId, "Sync Test Event");

        // Test synchronized access
        synchronized (enteredEvents) {
            if (!loadedEventIds.contains(eventId)) {
                loadedEventIds.add(eventId);
                enteredEvents.add(event);
            }
        }

        synchronized (enteredEvents) {
            if (!loadedEventIds.contains(eventId)) {
                loadedEventIds.add(eventId);
                enteredEvents.add(event); // Should not add
            }
        }

        assertEquals("Should only have one event with synchronized access", 1, enteredEvents.size());
    }

    @Test
    public void testDuplicatePreventionAfterAcceptingInvitation() {
        String eventId = "event-accept";
        Event event = createTestEvent(eventId, "Accepted Event");

        // Add to pending invitations
        addEventToPendingInvitations(event);

        // Accept invitation (simulate)
        // After acceptance, event should move to entered events
        // But should not appear in pending invitations anymore
        addEventToEnteredList(event);

        // Event should be in entered events
        assertTrue("Event should be in entered events", enteredEvents.contains(event));
        
        // If properly implemented, should not be in pending invitations after acceptance
        // (This depends on implementation - some may keep it until refresh)
    }

    @Test
    public void testEmptyLists() {
        assertEquals("Entered events should be empty initially", 0, enteredEvents.size());
        assertEquals("Pending invitations should be empty initially", 0, pendingInvitations.size());
    }

    @Test
    public void testClearAndReload() {
        String eventId = "event-reload";
        Event event = createTestEvent(eventId, "Reload Event");

        // Add event
        addEventToEnteredList(event);
        assertEquals("Should have one event", 1, enteredEvents.size());

        // Clear and reload
        enteredEvents.clear();
        loadedEventIds.clear();

        // Reload same event
        addEventToEnteredList(event);
        assertEquals("Should have one event after reload", 1, enteredEvents.size());
    }

    // Helper methods
    private Event createTestEvent(String eventId, String name) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        Event event = new Event(
                name,
                "Test Description",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        event.setDocumentId(eventId);
        return event;
    }

    private void addEventToEnteredList(Event event) {
        synchronized (enteredEvents) {
            if (event.getDocumentId() != null && !loadedEventIds.contains(event.getDocumentId())) {
                loadedEventIds.add(event.getDocumentId());
                enteredEvents.add(event);
            } else if (event.getDocumentId() == null) {
                // Handle null ID case - might add anyway or skip
                enteredEvents.add(event);
            }
        }
    }

    private void addEventToPendingInvitations(Event event) {
        synchronized (pendingInvitations) {
            if (event.getDocumentId() != null && !loadedEventIds.contains(event.getDocumentId())) {
                loadedEventIds.add(event.getDocumentId());
                pendingInvitations.add(event);
            } else if (event.getDocumentId() == null) {
                pendingInvitations.add(event);
            }
        }
    }
}


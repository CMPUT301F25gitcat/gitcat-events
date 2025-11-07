package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.InvitationListEntry;
import com.example.gitcat_events.core.model.AcceptedListEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for US 02.05.02: As an organizer I want to set the system to sample a specified number of attendees
 * Unit tests for US 02.05.03: As an organizer I want to draw replacement applicants
 */
public class RaffleSystemTests {

    private Event testEvent;
    private List<String> waitlist;
    private int eventCapacity;

    @Before
    public void setUp() {
        eventCapacity = 10;
        
        testEvent = new Event(
                "Raffle Event",
                "Event for testing raffle",
                eventCapacity,
                null,
                null,
                null,
                null,
                Calendar.getInstance(),
                false
        );
        testEvent.setDocumentId("raffle-event-123");

        // Create a waitlist with 20 people
        waitlist = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            waitlist.add("user-" + i);
        }
    }

    @Test
    public void testRaffleSelectionCount() {
        // Select 10 from 20
        List<String> selected = selectRandom(waitlist, eventCapacity);
        
        assertEquals("Should select exactly 10 entrants", eventCapacity, selected.size());
    }

    @Test
    public void testRaffleSelectsFromWaitlist() {
        List<String> selected = selectRandom(waitlist, eventCapacity);
        
        for (String selectedUser : selected) {
            assertTrue("Selected user should be from waitlist", waitlist.contains(selectedUser));
        }
    }

    @Test
    public void testRaffleNoDoplicates() {
        List<String> selected = selectRandom(waitlist, eventCapacity);
        
        Set<String> uniqueSelected = new HashSet<>(selected);
        assertEquals("Should not have duplicates", selected.size(), uniqueSelected.size());
    }

    @Test
    public void testRaffleWhenWaitlistSmallerThanCapacity() {
        // Only 5 people on waitlist, capacity is 10
        List<String> smallWaitlist = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            smallWaitlist.add("user-" + i);
        }
        
        List<String> selected = selectRandom(smallWaitlist, eventCapacity);
        
        assertEquals("Should select all 5 entrants", 5, selected.size());
    }

    @Test
    public void testRaffleWhenWaitlistEqualsCapacity() {
        // Exactly 10 people on waitlist, capacity is 10
        List<String> exactWaitlist = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            exactWaitlist.add("user-" + i);
        }
        
        List<String> selected = selectRandom(exactWaitlist, eventCapacity);
        
        assertEquals("Should select all 10 entrants", 10, selected.size());
    }

    @Test
    public void testInvitationListEntryCreation() {
        InvitationListEntry entry = new InvitationListEntry("event-123", "user-456");
        
        assertEquals("Event ID should match", "event-123", entry.getEventId());
        assertEquals("User ID should match", "user-456", entry.getUserDeviceId());
        assertEquals("Status should be pending", "pending", entry.getStatus());
    }

    @Test
    public void testInvitationListEntryWithDrawRound() {
        InvitationListEntry entry = new InvitationListEntry("event-123", "user-456");
        entry.setDrawRound(1);
        
        assertEquals("Draw round should be 1", 1, entry.getDrawRound());
    }

    @Test
    public void testReplacementDrawRound() {
        // First draw
        InvitationListEntry entry1 = new InvitationListEntry("event-123", "user-1");
        entry1.setDrawRound(1);
        
        // Replacement draw
        InvitationListEntry entry2 = new InvitationListEntry("event-123", "user-2");
        entry2.setDrawRound(2);
        
        assertTrue("Replacement draw should have higher round number", 
                entry2.getDrawRound() > entry1.getDrawRound());
    }

    @Test
    public void testAcceptedListEntryCreation() {
        AcceptedListEntry entry = new AcceptedListEntry("event-123", "user-456");
        
        assertEquals("Event ID should match", "event-123", entry.getEventId());
        assertEquals("User ID should match", "user-456", entry.getUserDeviceId());
        
        // AcceptedListEntry starts as "pending" by default, then gets set to "accepted" when user accepts
        assertEquals("Initial status should be pending", "pending", entry.getStatus());
        entry.setStatus("accepted");
        assertEquals("Status should be accepted after setting", "accepted", entry.getStatus());
    }

    @Test
    public void testAcceptedListEntryWithTimestamp() {
        AcceptedListEntry entry = new AcceptedListEntry("event-123", "user-456");
        long timestamp = System.currentTimeMillis();
        entry.setTimestamp(timestamp);
        
        assertEquals("Timestamp should match", timestamp, entry.getTimestamp());
    }

    @Test
    public void testEventCapacitySetting() {
        assertEquals("Event capacity should be 10", eventCapacity, testEvent.getCapacity());
        
        testEvent.setCapacity(20);
        assertEquals("Updated capacity should be 20", 20, testEvent.getCapacity());
    }

    @Test
    public void testDrawRoundIncrement() {
        // Draw rounds are tracked on InvitationListEntry, not Event
        InvitationListEntry entry1 = new InvitationListEntry("event-123", "user-1");
        entry1.setDrawRound(1);
        assertEquals("Initial draw round should be 1", 1, entry1.getDrawRound());
        
        InvitationListEntry entry2 = new InvitationListEntry("event-123", "user-2");
        entry2.setDrawRound(2);
        assertEquals("Draw round should increment to 2", 2, entry2.getDrawRound());
    }

    // Helper method to simulate random selection
    private List<String> selectRandom(List<String> source, int count) {
        List<String> shuffled = new ArrayList<>(source);
        java.util.Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }
}


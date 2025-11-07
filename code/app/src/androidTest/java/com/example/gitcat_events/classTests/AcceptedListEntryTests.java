package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gitcat_events.core.model.AcceptedListEntry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for AcceptedListEntry model
 * Tests US 01.05.02 - Acceptance of invitations and registration
 */
@RunWith(AndroidJUnit4.class)
public class AcceptedListEntryTests {
    
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.gitcat_events", appContext.getPackageName());
    }

    @Test
    public void testNoArgConstructor() {
        AcceptedListEntry entry = new AcceptedListEntry();
        assertNotNull(entry);
    }

    @Test
    public void testConstructorWithEventAndUser() {
        String eventId = "event123";
        String userDeviceId = "device456";
        
        AcceptedListEntry entry = new AcceptedListEntry(eventId, userDeviceId);
        
        assertEquals(eventId, entry.getEventId());
        assertEquals(userDeviceId, entry.getUserDeviceId());
        assertEquals("pending", entry.getStatus());
        assertEquals(1, entry.getDrawRound());
        assertTrue(entry.getTimestamp() > 0);
    }

    @Test
    public void testConstructorWithDrawRound() {
        String eventId = "event789";
        String userDeviceId = "device012";
        int drawRound = 2;
        
        AcceptedListEntry entry = new AcceptedListEntry(eventId, userDeviceId, drawRound);
        
        assertEquals(eventId, entry.getEventId());
        assertEquals(userDeviceId, entry.getUserDeviceId());
        assertEquals("pending", entry.getStatus());
        assertEquals(2, entry.getDrawRound());
        assertTrue(entry.getTimestamp() > 0);
    }

    /**
     * Test US 01.05.02 - Accept invitation and move to accepted list
     * Verify that accepted entries have "accepted" status
     */
    @Test
    public void testAcceptedStatus() {
        AcceptedListEntry entry = new AcceptedListEntry("event1", "device1");
        
        // Initial status is pending (when moved from invitation_list)
        assertEquals("pending", entry.getStatus());
        
        // Mark as accepted
        entry.setStatus("accepted");
        assertEquals("accepted", entry.getStatus());
    }

    @Test
    public void testStatusSetterAndGetter() {
        AcceptedListEntry entry = new AcceptedListEntry("event1", "device1");
        
        entry.setStatus("accepted");
        assertEquals("accepted", entry.getStatus());
        
        entry.setStatus("declined");
        assertEquals("declined", entry.getStatus());
        
        entry.setStatus("pending");
        assertEquals("pending", entry.getStatus());
    }

    @Test
    public void testDrawRoundSetterAndGetter() {
        AcceptedListEntry entry = new AcceptedListEntry("event1", "device1");
        
        assertEquals(1, entry.getDrawRound());
        
        entry.setDrawRound(3);
        assertEquals(3, entry.getDrawRound());
    }

    @Test
    public void testTimestampSetterAndGetter() {
        AcceptedListEntry entry = new AcceptedListEntry("event1", "device1");
        
        long originalTimestamp = entry.getTimestamp();
        assertTrue(originalTimestamp > 0);
        
        long newTimestamp = System.currentTimeMillis() + 5000;
        entry.setTimestamp(newTimestamp);
        assertEquals(newTimestamp, entry.getTimestamp());
    }

    /**
     * Test US 01.05.01 & US 02.05.03 - Tracking participants from replacement draws
     * Verify that accepted entries from different draw rounds are properly tracked
     */
    @Test
    public void testAcceptedFromReplacementDraw() {
        // Participant from initial draw
        AcceptedListEntry initialDraw = new AcceptedListEntry("event1", "device1", 1);
        assertEquals(1, initialDraw.getDrawRound());
        initialDraw.setStatus("accepted");
        assertEquals("accepted", initialDraw.getStatus());
        
        // Participant from first replacement draw
        AcceptedListEntry replacementDraw = new AcceptedListEntry("event1", "device2", 2);
        assertEquals(2, replacementDraw.getDrawRound());
        replacementDraw.setStatus("accepted");
        assertEquals("accepted", replacementDraw.getStatus());
    }

    @Test
    public void testMultipleParticipantsFromDifferentRounds() {
        AcceptedListEntry round1 = new AcceptedListEntry("event1", "device1", 1);
        AcceptedListEntry round2 = new AcceptedListEntry("event1", "device2", 2);
        AcceptedListEntry round3 = new AcceptedListEntry("event1", "device3", 3);
        
        assertEquals(1, round1.getDrawRound());
        assertEquals(2, round2.getDrawRound());
        assertEquals(3, round3.getDrawRound());
        
        // All can be accepted
        round1.setStatus("accepted");
        round2.setStatus("accepted");
        round3.setStatus("accepted");
        
        assertEquals("accepted", round1.getStatus());
        assertEquals("accepted", round2.getStatus());
        assertEquals("accepted", round3.getStatus());
    }

    @Test
    public void testTimestampOrdering() throws InterruptedException {
        AcceptedListEntry entry1 = new AcceptedListEntry("event1", "device1");
        long timestamp1 = entry1.getTimestamp();
        
        Thread.sleep(10); // Small delay
        
        AcceptedListEntry entry2 = new AcceptedListEntry("event1", "device2");
        long timestamp2 = entry2.getTimestamp();
        
        assertTrue(timestamp2 >= timestamp1);
    }
}


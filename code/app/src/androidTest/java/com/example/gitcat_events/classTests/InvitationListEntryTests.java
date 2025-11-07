package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gitcat_events.core.model.InvitationListEntry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for InvitationListEntry model
 * Tests US 01.05.02 and US 01.05.03 - Invitation acceptance/decline functionality
 */
@RunWith(AndroidJUnit4.class)
public class InvitationListEntryTests {
    
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.gitcat_events", appContext.getPackageName());
    }

    @Test
    public void testNoArgConstructor() {
        InvitationListEntry entry = new InvitationListEntry();
        assertNotNull(entry);
    }

    @Test
    public void testConstructorWithEventAndUser() {
        String eventId = "event123";
        String userDeviceId = "device456";
        
        InvitationListEntry entry = new InvitationListEntry(eventId, userDeviceId);
        
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
        int drawRound = 3;
        
        InvitationListEntry entry = new InvitationListEntry(eventId, userDeviceId, drawRound);
        
        assertEquals(eventId, entry.getEventId());
        assertEquals(userDeviceId, entry.getUserDeviceId());
        assertEquals("pending", entry.getStatus());
        assertEquals(3, entry.getDrawRound());
        assertTrue(entry.getTimestamp() > 0);
    }

    @Test
    public void testStatusSetterAndGetter() {
        InvitationListEntry entry = new InvitationListEntry("event1", "device1");
        
        assertEquals("pending", entry.getStatus());
        
        entry.setStatus("accepted");
        assertEquals("accepted", entry.getStatus());
        
        entry.setStatus("declined");
        assertEquals("declined", entry.getStatus());
    }

    @Test
    public void testDrawRoundSetterAndGetter() {
        InvitationListEntry entry = new InvitationListEntry("event1", "device1");
        
        assertEquals(1, entry.getDrawRound());
        
        entry.setDrawRound(2);
        assertEquals(2, entry.getDrawRound());
        
        entry.setDrawRound(5);
        assertEquals(5, entry.getDrawRound());
    }

    @Test
    public void testTimestampSetterAndGetter() {
        InvitationListEntry entry = new InvitationListEntry("event1", "device1");
        
        long originalTimestamp = entry.getTimestamp();
        assertTrue(originalTimestamp > 0);
        
        long newTimestamp = System.currentTimeMillis() + 10000;
        entry.setTimestamp(newTimestamp);
        assertEquals(newTimestamp, entry.getTimestamp());
    }

    /**
     * Test US 01.05.03 - Decline invitation flow
     * Verify that invitation can be marked as declined
     */
    @Test
    public void testDeclineInvitationStatus() {
        InvitationListEntry entry = new InvitationListEntry("event1", "device1");
        
        // Initial state is pending
        assertEquals("pending", entry.getStatus());
        
        // User declines
        entry.setStatus("declined");
        assertEquals("declined", entry.getStatus());
    }

    /**
     * Test US 01.05.02 - Accept invitation flow
     * Verify that invitation can be marked as accepted
     */
    @Test
    public void testAcceptInvitationStatus() {
        InvitationListEntry entry = new InvitationListEntry("event1", "device1");
        
        // Initial state is pending
        assertEquals("pending", entry.getStatus());
        
        // User accepts
        entry.setStatus("accepted");
        assertEquals("accepted", entry.getStatus());
    }

    /**
     * Test US 01.05.01 & US 02.05.03 - Replacement draw tracking
     * Verify that multiple draw rounds can be tracked
     */
    @Test
    public void testReplacementDrawRounds() {
        // First draw
        InvitationListEntry firstDraw = new InvitationListEntry("event1", "device1", 1);
        assertEquals(1, firstDraw.getDrawRound());
        
        // Second draw (replacement after decline)
        InvitationListEntry secondDraw = new InvitationListEntry("event1", "device2", 2);
        assertEquals(2, secondDraw.getDrawRound());
        
        // Third draw (another replacement)
        InvitationListEntry thirdDraw = new InvitationListEntry("event1", "device3", 3);
        assertEquals(3, thirdDraw.getDrawRound());
    }

    @Test
    public void testMultipleStatusChanges() {
        InvitationListEntry entry = new InvitationListEntry("event1", "device1");
        
        assertEquals("pending", entry.getStatus());
        
        entry.setStatus("accepted");
        assertEquals("accepted", entry.getStatus());
        
        // Simulate status correction
        entry.setStatus("pending");
        assertEquals("pending", entry.getStatus());
        
        entry.setStatus("declined");
        assertEquals("declined", entry.getStatus());
    }
}


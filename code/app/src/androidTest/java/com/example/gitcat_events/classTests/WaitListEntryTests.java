package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gitcat_events.core.model.WaitListEntry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for WaitListEntry model
 * Tests functionality for users on event waiting lists
 */
@RunWith(AndroidJUnit4.class)
public class WaitListEntryTests {
    
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.gitcat_events", appContext.getPackageName());
    }

    @Test
    public void testNoArgConstructor() {
        WaitListEntry entry = new WaitListEntry();
        assertNotNull(entry);
    }

    @Test
    public void testConstructorWithEventAndUser() {
        String eventId = "event123";
        String userDeviceId = "device456";
        
        WaitListEntry entry = new WaitListEntry(eventId, userDeviceId);
        
        assertEquals(eventId, entry.getEventId());
        assertEquals(userDeviceId, entry.getUserDeviceId());
    }

    @Test
    public void testSettersAndGetters() {
        WaitListEntry entry = new WaitListEntry("event1", "device1");
        
        assertEquals("event1", entry.getEventId());
        assertEquals("device1", entry.getUserDeviceId());
        
        entry.setEventId("event2");
        entry.setUserDeviceId("device2");
        
        assertEquals("event2", entry.getEventId());
        assertEquals("device2", entry.getUserDeviceId());
    }

    @Test
    public void testNullValues() {
        WaitListEntry entry = new WaitListEntry(null, null);
        assertNull(entry.getEventId());
        assertNull(entry.getUserDeviceId());
    }

    @Test
    public void testEmptyStrings() {
        WaitListEntry entry = new WaitListEntry("", "");
        assertEquals("", entry.getEventId());
        assertEquals("", entry.getUserDeviceId());
    }

    @Test
    public void testMultipleEntriesForSameEvent() {
        String eventId = "event123";
        
        WaitListEntry entry1 = new WaitListEntry(eventId, "device1");
        WaitListEntry entry2 = new WaitListEntry(eventId, "device2");
        WaitListEntry entry3 = new WaitListEntry(eventId, "device3");
        
        assertEquals(eventId, entry1.getEventId());
        assertEquals(eventId, entry2.getEventId());
        assertEquals(eventId, entry3.getEventId());
        
        assertEquals("device1", entry1.getUserDeviceId());
        assertEquals("device2", entry2.getUserDeviceId());
        assertEquals("device3", entry3.getUserDeviceId());
    }

    @Test
    public void testMultipleEntriesForSameUser() {
        String deviceId = "device123";
        
        WaitListEntry entry1 = new WaitListEntry("event1", deviceId);
        WaitListEntry entry2 = new WaitListEntry("event2", deviceId);
        WaitListEntry entry3 = new WaitListEntry("event3", deviceId);
        
        assertEquals(deviceId, entry1.getUserDeviceId());
        assertEquals(deviceId, entry2.getUserDeviceId());
        assertEquals(deviceId, entry3.getUserDeviceId());
        
        assertEquals("event1", entry1.getEventId());
        assertEquals("event2", entry2.getEventId());
        assertEquals("event3", entry3.getEventId());
    }

    @Test
    public void testLongIds() {
        String longEventId = "event_with_very_long_identifier_123456789";
        String longDeviceId = "device_with_very_long_identifier_abcdefgh";
        
        WaitListEntry entry = new WaitListEntry(longEventId, longDeviceId);
        
        assertEquals(longEventId, entry.getEventId());
        assertEquals(longDeviceId, entry.getUserDeviceId());
    }

    @Test
    public void testSpecialCharactersInIds() {
        WaitListEntry entry = new WaitListEntry("event-123_test", "device@456#test");
        assertEquals("event-123_test", entry.getEventId());
        assertEquals("device@456#test", entry.getUserDeviceId());
    }

    @Test
    public void testUpdateEventId() {
        WaitListEntry entry = new WaitListEntry("event1", "device1");
        assertEquals("event1", entry.getEventId());
        
        entry.setEventId("event999");
        assertEquals("event999", entry.getEventId());
        // Device ID should remain unchanged
        assertEquals("device1", entry.getUserDeviceId());
    }

    @Test
    public void testUpdateUserDeviceId() {
        WaitListEntry entry = new WaitListEntry("event1", "device1");
        assertEquals("device1", entry.getUserDeviceId());
        
        entry.setUserDeviceId("device999");
        assertEquals("device999", entry.getUserDeviceId());
        // Event ID should remain unchanged
        assertEquals("event1", entry.getEventId());
    }
}


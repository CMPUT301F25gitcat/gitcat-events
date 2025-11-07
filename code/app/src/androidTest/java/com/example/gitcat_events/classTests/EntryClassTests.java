package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.gitcat_events.core.model.Entry;

/**
 * Unit tests for Entry abstract class
 * Tests basic functionality of eventId and userDeviceId fields
 */
@RunWith(AndroidJUnit4.class)
public class EntryClassTests {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.gitcat_events", appContext.getPackageName());
    }

    static class TestEntry extends Entry {
        public TestEntry(String eventId, String userDeviceId) {
            super(eventId, userDeviceId);
        }
    }

    @Test
    public void testNoArgConstructor() {
        TestEntry entry = new TestEntry(null, null);
        assertNotNull(entry);
    }

    @Test
    public void testConstructorAndGetters() {
        TestEntry entry = new TestEntry("event123", "device456");
        assertEquals("event123", entry.getEventId());
        assertEquals("device456", entry.getUserDeviceId());
    }

    @Test
    public void testSetters() {
        TestEntry entry = new TestEntry("event1", "device1");

        entry.setEventId("event99");
        entry.setUserDeviceId("device77");

        assertEquals("event99", entry.getEventId());
        assertEquals("device77", entry.getUserDeviceId());
    }

    @Test
    public void testMultipleChanges() {
        Entry entry = new TestEntry("event0", "device0");

        entry.setEventId("event10");
        entry.setUserDeviceId("device20");
        assertEquals("event10", entry.getEventId());
        assertEquals("device20", entry.getUserDeviceId());

        entry.setEventId("event999");
        entry.setUserDeviceId("device888");
        assertEquals("event999", entry.getEventId());
        assertEquals("device888", entry.getUserDeviceId());
    }

    @Test
    public void testNullValues() {
        TestEntry entry = new TestEntry(null, null);
        assertNull(entry.getEventId());
        assertNull(entry.getUserDeviceId());
        
        entry.setEventId("event1");
        entry.setUserDeviceId("device1");
        assertEquals("event1", entry.getEventId());
        assertEquals("device1", entry.getUserDeviceId());
    }

    @Test
    public void testEmptyStrings() {
        TestEntry entry = new TestEntry("", "");
        assertEquals("", entry.getEventId());
        assertEquals("", entry.getUserDeviceId());
    }

    @Test
    public void testLongIds() {
        String longEventId = "event_with_very_long_id_12345678901234567890";
        String longDeviceId = "device_with_very_long_id_abcdefghijklmnopqrstuvwxyz";
        
        TestEntry entry = new TestEntry(longEventId, longDeviceId);
        assertEquals(longEventId, entry.getEventId());
        assertEquals(longDeviceId, entry.getUserDeviceId());
    }

    @Test
    public void testSpecialCharacters() {
        TestEntry entry = new TestEntry("event-123_test", "device@456#test");
        assertEquals("event-123_test", entry.getEventId());
        assertEquals("device@456#test", entry.getUserDeviceId());
    }
}
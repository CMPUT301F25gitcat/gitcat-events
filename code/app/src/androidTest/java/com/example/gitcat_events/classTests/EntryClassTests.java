package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.gitcat_events.core.model.Entry;

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
    public void testConstructorAndGetters() {
        TestEntry entry = new TestEntry("event-123", "device-456");
        assertEquals("event-123", entry.getEventId());
        assertEquals("device-456", entry.getUserDeviceId());
    }

    @Test
    public void testSetters() {
        TestEntry entry = new TestEntry("event-1", "device-2");

        entry.setEventId("event-99");
        entry.setUserDeviceId("device-77");

        assertEquals("event-99", entry.getEventId());
        assertEquals("device-77", entry.getUserDeviceId());
    }

    @Test
    public void testMultipleChanges() {
        Entry entry = new TestEntry("event-0", "device-0");

        entry.setEventId("event-10");
        entry.setUserDeviceId("device-20");
        assertEquals("event-10", entry.getEventId());
        assertEquals("device-20", entry.getUserDeviceId());

        entry.setEventId("event-999");
        entry.setUserDeviceId("device-888");
        assertEquals("event-999", entry.getEventId());
        assertEquals("device-888", entry.getUserDeviceId());
    }

}
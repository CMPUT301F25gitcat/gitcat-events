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
        public TestEntry(int eventID, int userID) {
            super(eventID, userID);
        }
    }

    @Test
    public void testConstructorAndGetters() {
        TestEntry entry = new TestEntry(123, 456);
        assertEquals(123, entry.getEventID());
        assertEquals(456, entry.getUserID());
    }

    @Test
    public void testSetters() {
        TestEntry entry = new TestEntry(1, 2);

        entry.setEventID(99);
        entry.setUserID(77);

        assertEquals(99, entry.getEventID());
        assertEquals(77, entry.getUserID());
    }

    @Test
    public void testMultipleChanges() {
        Entry entry = new TestEntry(0, 0);

        entry.setEventID(10);
        entry.setUserID(20);
        assertEquals(10, entry.getEventID());
        assertEquals(20, entry.getUserID());

        entry.setEventID(999);
        entry.setUserID(888);
        assertEquals(999, entry.getEventID());
        assertEquals(888, entry.getUserID());
    }

}
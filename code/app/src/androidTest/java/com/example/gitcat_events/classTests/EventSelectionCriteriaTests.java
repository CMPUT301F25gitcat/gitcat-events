package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.gitcat_events.core.model.Event;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

/**
 * Unit tests for Event model - Selection Criteria feature
 * Tests US 01.05.05 - Lottery selection criteria/guidelines display
 */
@RunWith(AndroidJUnit4.class)
public class EventSelectionCriteriaTests {
    
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.gitcat_events", appContext.getPackageName());
    }

    /**
     * Test US 01.05.05 - Selection criteria can be set and retrieved
     */
    @Test
    public void testSelectionCriteriaSetterAndGetter() {
        Event event = new Event();
        
        String criteria = "Random selection from all registered participants. All entrants have an equal chance.";
        event.setSelectionCriteria(criteria);
        
        assertEquals(criteria, event.getSelectionCriteria());
    }

    /**
     * Test US 01.05.05 - Selection criteria is optional (can be null)
     */
    @Test
    public void testSelectionCriteriaOptional() {
        Event event = new Event();
        
        // Initially null
        assertNull(event.getSelectionCriteria());
        
        // Can be set
        event.setSelectionCriteria("Custom criteria");
        assertNotNull(event.getSelectionCriteria());
        
        // Can be set back to null
        event.setSelectionCriteria(null);
        assertNull(event.getSelectionCriteria());
    }

    /**
     * Test US 01.05.05 - Organizer can provide detailed selection guidelines
     */
    @Test
    public void testDetailedSelectionCriteria() {
        Event event = createTestEvent();
        
        String detailedCriteria = "Selection Process:\n" +
                "1. Random lottery from all waitlist participants\n" +
                "2. Priority given to first-time attendees\n" +
                "3. Geographic diversity considered\n" +
                "4. All selected participants will be notified within 24 hours";
        
        event.setSelectionCriteria(detailedCriteria);
        assertEquals(detailedCriteria, event.getSelectionCriteria());
    }

    /**
     * Test US 01.05.05 - Default criteria can be used
     */
    @Test
    public void testDefaultCriteria() {
        Event event = createTestEvent();
        
        String defaultCriteria = "Random selection from all registered participants. All entrants have an equal chance of being selected.";
        event.setSelectionCriteria(defaultCriteria);
        
        assertEquals(defaultCriteria, event.getSelectionCriteria());
    }

    /**
     * Test US 01.05.05 - Criteria can be updated by organizer
     */
    @Test
    public void testUpdateSelectionCriteria() {
        Event event = createTestEvent();
        
        String initialCriteria = "First come, first served";
        event.setSelectionCriteria(initialCriteria);
        assertEquals(initialCriteria, event.getSelectionCriteria());
        
        String updatedCriteria = "Random selection with priority for local residents";
        event.setSelectionCriteria(updatedCriteria);
        assertEquals(updatedCriteria, event.getSelectionCriteria());
    }

    @Test
    public void testEmptySelectionCriteria() {
        Event event = new Event();
        
        event.setSelectionCriteria("");
        assertEquals("", event.getSelectionCriteria());
    }

    @Test
    public void testLongSelectionCriteria() {
        Event event = new Event();
        
        StringBuilder longCriteria = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longCriteria.append("Selection rule ").append(i).append(". ");
        }
        
        event.setSelectionCriteria(longCriteria.toString());
        assertEquals(longCriteria.toString(), event.getSelectionCriteria());
    }

    /**
     * Test US 01.05.05 - Multiple events can have different criteria
     */
    @Test
    public void testMultipleEventsWithDifferentCriteria() {
        Event event1 = createTestEvent();
        Event event2 = createTestEvent();
        Event event3 = createTestEvent();
        
        event1.setSelectionCriteria("Random selection");
        event2.setSelectionCriteria("Priority for students");
        event3.setSelectionCriteria("Geographic diversity focus");
        
        assertEquals("Random selection", event1.getSelectionCriteria());
        assertEquals("Priority for students", event2.getSelectionCriteria());
        assertEquals("Geographic diversity focus", event3.getSelectionCriteria());
    }

    // Helper method to create a test event
    private Event createTestEvent() {
        Calendar registrationStartDate = Calendar.getInstance();
        registrationStartDate.add(Calendar.DAY_OF_MONTH, 1);
        
        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, 3);
        
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 7);
        
        Event event = new Event(
                "Test Event",
                "Test Description",
                50,
                100,
                null,
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );
        event.setOrganizerDeviceId("test_device_123");
        
        return event;
    }
}


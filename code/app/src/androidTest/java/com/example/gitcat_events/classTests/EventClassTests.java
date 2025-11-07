package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
public class EventClassTests {

    @Test
    public void testConstructorAndGetters() {
        Calendar raffleDate = Calendar.getInstance();
        raffleDate.setTimeInMillis(1672531200000L); // Jan 1, 2023
        
        Calendar eventDate = Calendar.getInstance();
        eventDate.setTimeInMillis(1652731200000L);  // May 16, 2022

        Event event = new Event(
                "Hackathon",
                "24-hour coding event",
                100,
                20,
                "https://example.com/poster.png",
                raffleDate,
                eventDate,
                false
        );

        assertEquals("Hackathon", event.getName());
        assertEquals("24-hour coding event", event.getDescription());
        assertEquals(100, event.getCapacity());
        assertEquals((Integer) 20, event.getMaxWaitListSize());
        assertEquals("https://example.com/poster.png", event.getPoster());
        assertEquals(raffleDate.getTimeInMillis(), event.getRaffleDate().getTimeInMillis());
        assertEquals(eventDate.getTimeInMillis(), event.getEventDate().getTimeInMillis());
        assertFalse(event.getGeoLocationRequired());
    }

    @Test
    public void testNullableMaxWaitListSize() {
        Calendar raffleDate = Calendar.getInstance();
        Calendar eventDate = Calendar.getInstance();

        Event event = new Event(
                "Seminar",
                "Tech talk",
                50,
                null,
                "https://poster.com",
                raffleDate,
                eventDate,
                true
        );

        assertNull(event.getMaxWaitListSize());
        assertTrue(event.getGeoLocationRequired());
    }

    @Test
    public void testSetters() {
        Calendar raffleDate = Calendar.getInstance();
        raffleDate.setTimeInMillis(1700000000000L);
        
        Calendar eventDate = Calendar.getInstance();
        eventDate.setTimeInMillis(1710000000000L);

        Event event = new Event(
                "Workshop",
                "Initial description",
                10,
                5,
                "https://poster1.com",
                raffleDate,
                eventDate,
                false
        );

        // modify all fields
        event.setName("Updated Workshop");
        event.setDescription("Updated description");
        event.setCapacity(200);
        event.setMaxWaitListSize(40);
        event.setPoster("https://poster2.com");
        event.setOrganizerDeviceId("device-123");
        event.setGeoLocationRequired(true);
        
        Calendar newRaffleDate = Calendar.getInstance();
        newRaffleDate.setTimeInMillis(1800000000000L);
        event.setRaffleDate(newRaffleDate);
        
        Calendar newEventDate = Calendar.getInstance();
        newEventDate.setTimeInMillis(1810000000000L);
        event.setEventDate(newEventDate);

        assertEquals("Updated Workshop", event.getName());
        assertEquals("Updated description", event.getDescription());
        assertEquals(200, event.getCapacity());
        assertEquals((Integer) 40, event.getMaxWaitListSize());
        assertEquals("https://poster2.com", event.getPoster());
        assertEquals("device-123", event.getOrganizerDeviceId());
        assertEquals(1800000000000L, event.getRaffleDate().getTimeInMillis());
        assertEquals(1810000000000L, event.getEventDate().getTimeInMillis());
        assertTrue(event.getGeoLocationRequired());
    }

    @Test
    public void testSetMaxWaitListSizeToNull() {
        Event event = new Event(
                "Concert",
                "Music festival",
                1000,
                200,
                "poster.png",
                Calendar.getInstance(),
                Calendar.getInstance(),
                false
        );

        event.setMaxWaitListSize(null);
        assertNull(event.getMaxWaitListSize());
    }

    @Test
    public void testMultipleMutations() {
        Calendar raffleDate = Calendar.getInstance();
        raffleDate.setTimeInMillis(100000L);
        
        Calendar eventDate = Calendar.getInstance();
        eventDate.setTimeInMillis(200000L);
        
        Event event = new Event(
                "Initial Event",
                "Desc",
                10,
                null,
                "posterA.png",
                raffleDate,
                eventDate,
                false
        );

        event.setCapacity(500);
        event.setOrganizerDeviceId("device-42");
        event.setName("Final Event");
        event.setPoster("posterB.png");

        assertEquals("Final Event", event.getName());
        assertEquals("posterB.png", event.getPoster());
        assertEquals(500, event.getCapacity());
        assertEquals("device-42", event.getOrganizerDeviceId());
    }
}

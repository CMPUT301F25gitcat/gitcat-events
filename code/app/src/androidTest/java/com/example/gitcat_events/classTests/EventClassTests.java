package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.gitcat_events.core.model.Event;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class EventClassTests {

    @Test
    public void testConstructorAndGetters() {
        Date raffleDate = new Date(1672531200000L); // Jan 1, 2023
        Date eventDate = new Date(1652731200000L);  // May 16, 2022

        Event event = new Event(
                "Hackathon",
                "24-hour coding event",
                100,
                20,
                "https://example.com/poster.png",
                raffleDate,
                eventDate
        );

        assertEquals("Hackathon", event.getName());
        assertEquals("24-hour coding event", event.getDescription());
        assertEquals(100, event.getCapacity());
        assertEquals((Integer) 20, event.getMaxWaitListSize());
        assertEquals("https://example.com/poster.png", event.getPoster());
        assertEquals(raffleDate, event.getRaffleDate());
        assertEquals(eventDate, event.getEventDate());
    }

    @Test
    public void testNullableMaxWaitListSize() {
        Date raffleDate = new Date();
        Date eventDate = new Date();

        Event event = new Event(
                "Seminar",
                "Tech talk",
                50,
                null,
                "https://poster.com",
                raffleDate,
                eventDate
        );

        assertNull(event.getMaxWaitListSize());
    }

    @Test
    public void testSetters() {
        Date raffleDate = new Date(1700000000000L);
        Date eventDate = new Date(1710000000000L);

        Event event = new Event(
                "Workshop",
                "Initial description",
                10,
                5,
                "https://poster1.com",
                raffleDate,
                eventDate
        );

        // modify all fields
        event.setName("Updated Workshop");
        event.setDescription("Updated description");
        event.setCapacity(200);
        event.setMaxWaitListSize(40);
        event.setPoster("https://poster2.com");
        event.setOrganizer(99);
        event.setRaffleDate(new Date(1800000000000L));
        event.setEventDate(new Date(1810000000000L));

        assertEquals("Updated Workshop", event.getName());
        assertEquals("Updated description", event.getDescription());
        assertEquals(200, event.getCapacity());
        assertEquals((Integer) 40, event.getMaxWaitListSize());
        assertEquals("https://poster2.com", event.getPoster());
        assertEquals(99, event.getOrganizer());
        assertEquals(new Date(1800000000000L), event.getRaffleDate());
        assertEquals(new Date(1810000000000L), event.getEventDate());
    }

    @Test
    public void testSetMaxWaitListSizeToNull() {
        Event event = new Event(
                "Concert",
                "Music festival",
                1000,
                200,
                "poster.png",
                new Date(),
                new Date()
        );

        event.setMaxWaitListSize(null);
        assertNull(event.getMaxWaitListSize());
    }

    @Test
    public void testMultipleMutations() {
        Event event = new Event(
                "Initial Event",
                "Desc",
                10,
                null,
                "posterA.png",
                new Date(100000L),
                new Date(200000L)
        );

        event.setCapacity(500);
        event.setOrganizer(42);
        event.setName("Final Event");
        event.setPoster("posterB.png");

        assertEquals("Final Event", event.getName());
        assertEquals("posterB.png", event.getPoster());
        assertEquals(500, event.getCapacity());
        assertEquals(42, event.getOrganizer());
    }
}

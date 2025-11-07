package com.example.gitcat_events.classTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

/**
 * Unit tests for Event model
 * Tests event creation, getters, setters, and field management
 */
@RunWith(AndroidJUnit4.class)
public class EventClassTests {

    @Test
    public void testConstructorAndGetters() {
        Calendar registrationStartDate = Calendar.getInstance();
        registrationStartDate.set(2022, Calendar.DECEMBER, 15);
        
        Calendar raffleDate = Calendar.getInstance();
        raffleDate.set(2023, Calendar.JANUARY, 1);
        
        Calendar eventDate = Calendar.getInstance();
        eventDate.set(2023, Calendar.MAY, 16);

        Event event = new Event(
                "Hackathon",
                "24-hour coding event",
                100,
                20,
                "https://example.com/poster.png",
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );

        assertEquals("Hackathon", event.getName());
        assertEquals("24-hour coding event", event.getDescription());
        assertEquals(100, event.getCapacity());
        assertEquals((Integer) 20, event.getMaxWaitListSize());
        assertEquals("https://example.com/poster.png", event.getPoster());
        assertEquals(registrationStartDate, event.getRegistrationStartDate());
        assertEquals(raffleDate, event.getRaffleDate());
        assertEquals(eventDate, event.getEventDate());
        assertFalse(event.getGeoLocationRequired());
    }

    @Test
    public void testNullableMaxWaitListSize() {
        Calendar registrationStartDate = Calendar.getInstance();
        Calendar raffleDate = Calendar.getInstance();
        Calendar eventDate = Calendar.getInstance();

        Event event = new Event(
                "Seminar",
                "Tech talk",
                50,
                null,
                "https://poster.com",
                registrationStartDate,
                raffleDate,
                eventDate,
                true
        );

        assertNull(event.getMaxWaitListSize());
        assertTrue(event.getGeoLocationRequired());
    }

    @Test
    public void testSetters() {
        Calendar registrationStartDate = Calendar.getInstance();
        registrationStartDate.set(2023, Calendar.FEBRUARY, 1);
        
        Calendar raffleDate = Calendar.getInstance();
        raffleDate.set(2023, Calendar.MARCH, 1);
        
        Calendar eventDate = Calendar.getInstance();
        eventDate.set(2023, Calendar.APRIL, 15);

        Event event = new Event(
                "Workshop",
                "Initial description",
                10,
                5,
                "https://poster1.com",
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );

        // Modify all fields
        event.setName("Updated Workshop");
        event.setDescription("Updated description");
        event.setCapacity(200);
        event.setMaxWaitListSize(40);
        event.setPoster("https://poster2.com");
        event.setOrganizerDeviceId("device_99");
        event.setGeoLocationRequired(true);
        
        Calendar newRaffleDate = Calendar.getInstance();
        newRaffleDate.set(2023, Calendar.JUNE, 1);
        event.setRaffleDate(newRaffleDate);
        
        Calendar newEventDate = Calendar.getInstance();
        newEventDate.set(2023, Calendar.JULY, 1);
        event.setEventDate(newEventDate);

        assertEquals("Updated Workshop", event.getName());
        assertEquals("Updated description", event.getDescription());
        assertEquals(200, event.getCapacity());
        assertEquals((Integer) 40, event.getMaxWaitListSize());
        assertEquals("https://poster2.com", event.getPoster());
        assertEquals("device_99", event.getOrganizerDeviceId());
        assertTrue(event.getGeoLocationRequired());
        assertEquals(newRaffleDate, event.getRaffleDate());
        assertEquals(newEventDate, event.getEventDate());
    }

    @Test
    public void testSetMaxWaitListSizeToNull() {
        Calendar registrationStartDate = Calendar.getInstance();
        Calendar raffleDate = Calendar.getInstance();
        Calendar eventDate = Calendar.getInstance();
        
        Event event = new Event(
                "Concert",
                "Music festival",
                1000,
                200,
                "poster.png",
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );

        event.setMaxWaitListSize(null);
        assertNull(event.getMaxWaitListSize());
    }

    @Test
    public void testMultipleMutations() {
        Calendar registrationStartDate = Calendar.getInstance();
        registrationStartDate.setTimeInMillis(50000L);
        
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
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );

        event.setCapacity(500);
        event.setOrganizerDeviceId("device_42");
        event.setName("Final Event");
        event.setPoster("posterB.png");
        event.setDocumentId("doc123");

        assertEquals("Final Event", event.getName());
        assertEquals("posterB.png", event.getPoster());
        assertEquals(500, event.getCapacity());
        assertEquals("device_42", event.getOrganizerDeviceId());
        assertEquals("doc123", event.getDocumentId());
    }

    @Test
    public void testNoArgConstructor() {
        Event event = new Event();
        assertNotNull(event);
    }

    @Test
    public void testSelectionCriteria() {
        Calendar registrationStartDate = Calendar.getInstance();
        Calendar raffleDate = Calendar.getInstance();
        Calendar eventDate = Calendar.getInstance();
        
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

        assertNull(event.getSelectionCriteria());

        event.setSelectionCriteria("Random selection from all participants");
        assertEquals("Random selection from all participants", event.getSelectionCriteria());
    }

    @Test
    public void testGeoLocationRequired() {
        Calendar registrationStartDate = Calendar.getInstance();
        Calendar raffleDate = Calendar.getInstance();
        Calendar eventDate = Calendar.getInstance();
        
        Event eventWithGeo = new Event(
                "Geo Event",
                "Requires location",
                50,
                null,
                null,
                registrationStartDate,
                raffleDate,
                eventDate,
                true
        );

        assertTrue(eventWithGeo.getGeoLocationRequired());

        Event eventWithoutGeo = new Event(
                "No Geo Event",
                "No location required",
                50,
                null,
                null,
                registrationStartDate,
                raffleDate,
                eventDate,
                false
        );

        assertFalse(eventWithoutGeo.getGeoLocationRequired());
    }
}

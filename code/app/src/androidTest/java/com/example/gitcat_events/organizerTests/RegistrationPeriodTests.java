package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

/**
 * Unit tests for US 02.01.04: As an organizer, I want to set a registration period.
 */
public class RegistrationPeriodTests {

    private Event testEvent;
    private Calendar registrationStart;
    private Calendar registrationEnd;
    private Calendar eventDate;

    @Before
    public void setUp() {
        registrationStart = Calendar.getInstance();
        registrationStart.set(2024, Calendar.JANUARY, 1);

        registrationEnd = Calendar.getInstance();
        registrationEnd.set(2024, Calendar.JANUARY, 15);

        eventDate = Calendar.getInstance();
        eventDate.set(2024, Calendar.JANUARY, 20);

        testEvent = new Event(
                "Test Event",
                "Test Description",
                100,
                null,
                null,
                registrationStart,
                registrationEnd,
                eventDate,
                false
        );
    }

    @Test
    public void testSetRegistrationStartDate() {
        assertNotNull("Registration start date should not be null", testEvent.getRegistrationStartDate());
        assertEquals("Registration start date should match", registrationStart, testEvent.getRegistrationStartDate());
    }

    @Test
    public void testSetRegistrationEndDate() {
        assertNotNull("Registration end date should not be null", testEvent.getRaffleDate());
        assertEquals("Registration end date should match", registrationEnd, testEvent.getRaffleDate());
    }

    @Test
    public void testRegistrationPeriodValidation_StartBeforeEnd() {
        // Registration start should be before end
        assertTrue("Registration start should be before end",
                testEvent.getRegistrationStartDate().before(testEvent.getRaffleDate()));
    }

    @Test
    public void testRegistrationPeriodValidation_EndBeforeEvent() {
        // Registration end should be before event date
        assertTrue("Registration end should be before event date",
                testEvent.getRaffleDate().before(testEvent.getEventDate()));
    }

    @Test
    public void testUpdateRegistrationStartDate() {
        Calendar newStartDate = Calendar.getInstance();
        newStartDate.set(2024, Calendar.JANUARY, 5);
        
        testEvent.setRegistrationStartDate(newStartDate);
        
        assertEquals("Updated start date should match", newStartDate, testEvent.getRegistrationStartDate());
    }

    @Test
    public void testUpdateRegistrationEndDate() {
        Calendar newEndDate = Calendar.getInstance();
        newEndDate.set(2024, Calendar.JANUARY, 18);
        
        testEvent.setRaffleDate(newEndDate);
        
        assertEquals("Updated end date should match", newEndDate, testEvent.getRaffleDate());
    }

    @Test
    public void testRegistrationPeriodWithNullStartDate() {
        Event eventWithNullStart = new Event(
                "Test Event",
                "Test Description",
                100,
                null,
                null,
                null,
                registrationEnd,
                eventDate,
                false
        );
        
        assertNull("Registration start date can be null", eventWithNullStart.getRegistrationStartDate());
    }

    @Test
    public void testRegistrationPeriodWithNullEndDate() {
        Event eventWithNullEnd = new Event(
                "Test Event",
                "Test Description",
                100,
                null,
                null,
                registrationStart,
                null,
                eventDate,
                false
        );
        
        assertNull("Registration end date can be null", eventWithNullEnd.getRaffleDate());
    }
}


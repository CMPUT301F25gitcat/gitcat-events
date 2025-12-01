package com.example.gitcat_events.adminTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Unit tests for Admin Menu functionality
 * Tests US 03.04.01: As an administrator I want to browse events based on name of event
 */
public class AdminMenuTests {

    private List<Event> allEvents;
    private List<Event> filteredEvents;

    @Before
    public void setUp() {
        allEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();

        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        // Create test events with different names
        Event event1 = new Event(
                "Hackathon 2024",
                "Annual coding competition",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        event1.setDocumentId("event-1");

        Event event2 = new Event(
                "Music Festival",
                "Summer music event",
                500,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        event2.setDocumentId("event-2");

        Event event3 = new Event(
                "Tech Conference",
                "Technology conference",
                200,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        event3.setDocumentId("event-3");

        Event event4 = new Event(
                "Hackathon 2025",
                "Next year's hackathon",
                150,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        event4.setDocumentId("event-4");

        allEvents.add(event1);
        allEvents.add(event2);
        allEvents.add(event3);
        allEvents.add(event4);
    }

    @Test
    public void testLoadAllEvents() {
        filteredEvents.clear();
        filteredEvents.addAll(allEvents);

        assertEquals("Should load all events", 4, filteredEvents.size());
    }

    @Test
    public void testSearchEventsByName_ExactMatch() {
        String searchQuery = "Hackathon 2024";
        filterEventsByName(searchQuery);

        assertEquals("Should find exact match", 1, filteredEvents.size());
        assertEquals("Should match event name", "Hackathon 2024", filteredEvents.get(0).getName());
    }

    @Test
    public void testSearchEventsByName_PartialMatch() {
        String searchQuery = "Hackathon";
        filterEventsByName(searchQuery);

        assertEquals("Should find partial matches", 2, filteredEvents.size());
        assertTrue("Should contain Hackathon 2024", 
                filteredEvents.stream().anyMatch(e -> e.getName().equals("Hackathon 2024")));
        assertTrue("Should contain Hackathon 2025", 
                filteredEvents.stream().anyMatch(e -> e.getName().equals("Hackathon 2025")));
    }

    @Test
    public void testSearchEventsByName_CaseInsensitive() {
        String searchQuery = "music";
        filterEventsByName(searchQuery);

        assertEquals("Should find case-insensitive match", 1, filteredEvents.size());
        assertEquals("Should match Music Festival", "Music Festival", filteredEvents.get(0).getName());
    }

    @Test
    public void testSearchEventsByName_NoMatch() {
        String searchQuery = "NonExistentEvent";
        filterEventsByName(searchQuery);

        assertEquals("Should find no matches", 0, filteredEvents.size());
    }

    @Test
    public void testSearchEventsByName_EmptyQuery() {
        String searchQuery = "";
        filterEventsByName(searchQuery);

        assertEquals("Empty query should return all events", allEvents.size(), filteredEvents.size());
    }

    @Test
    public void testSearchEventsByName_WhitespaceOnly() {
        String searchQuery = "   ";
        filterEventsByName(searchQuery);

        assertEquals("Whitespace-only query should return all events", allEvents.size(), filteredEvents.size());
    }

    @Test
    public void testSearchEventsByName_SingleCharacter() {
        String searchQuery = "T";
        filterEventsByName(searchQuery);

        assertTrue("Should find events starting with T", filteredEvents.size() > 0);
        assertTrue("Should contain Tech Conference", 
                filteredEvents.stream().anyMatch(e -> e.getName().contains("Tech")));
    }

    @Test
    public void testSearchEventsByName_MiddleOfWord() {
        String searchQuery = "Festival";
        filterEventsByName(searchQuery);

        assertEquals("Should find event with Festival in name", 1, filteredEvents.size());
        assertEquals("Should match Music Festival", "Music Festival", filteredEvents.get(0).getName());
    }

    @Test
    public void testSearchEventsByName_SpecialCharacters() {
        // Test that special characters don't break search
        String searchQuery = "2024";
        filterEventsByName(searchQuery);

        assertTrue("Should find events with numbers", filteredEvents.size() > 0);
        assertTrue("Should contain Hackathon 2024", 
                filteredEvents.stream().anyMatch(e -> e.getName().contains("2024")));
    }

    @Test
    public void testEventsSortedAlphabetically() {
        // Sort events alphabetically by name
        allEvents.sort((e1, e2) -> {
            String name1 = e1.getName() != null ? e1.getName() : "";
            String name2 = e2.getName() != null ? e2.getName() : "";
            return name1.compareToIgnoreCase(name2);
        });

        assertEquals("First event should be Hackathon 2024", "Hackathon 2024", allEvents.get(0).getName());
        assertEquals("Second event should be Hackathon 2025", "Hackathon 2025", allEvents.get(1).getName());
        assertEquals("Third event should be Music Festival", "Music Festival", allEvents.get(2).getName());
        assertEquals("Fourth event should be Tech Conference", "Tech Conference", allEvents.get(3).getName());
    }

    @Test
    public void testFilterMaintainsOrder() {
        String searchQuery = "Hackathon";
        filterEventsByName(searchQuery);

        // After filtering, order should be maintained
        assertEquals("Should have 2 results", 2, filteredEvents.size());
        assertEquals("First should be Hackathon 2024", "Hackathon 2024", filteredEvents.get(0).getName());
        assertEquals("Second should be Hackathon 2025", "Hackathon 2025", filteredEvents.get(1).getName());
    }

    @Test
    public void testSearchWithNullEventName() {
        Event eventWithNullName = new Event();
        eventWithNullName.setName(null);
        eventWithNullName.setDocumentId("null-event");
        allEvents.add(eventWithNullName);

        String searchQuery = "test";
        filterEventsByName(searchQuery);

        // Events with null names should not match any search
        assertFalse("Should not include events with null names", 
                filteredEvents.contains(eventWithNullName));
    }

    @Test
    public void testMultipleSearches() {
        // First search
        filterEventsByName("Hackathon");
        assertEquals("First search should find 2 events", 2, filteredEvents.size());

        // Second search
        filterEventsByName("Music");
        assertEquals("Second search should find 1 event", 1, filteredEvents.size());

        // Third search - clear
        filterEventsByName("");
        assertEquals("Empty search should return all events", allEvents.size(), filteredEvents.size());
    }

    // Helper method to simulate filtering
    private void filterEventsByName(String searchQuery) {
        filteredEvents.clear();
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String queryLower = searchQuery.toLowerCase().trim();
            for (Event event : allEvents) {
                String eventName = event.getName();
                if (eventName != null && eventName.toLowerCase().contains(queryLower)) {
                    filteredEvents.add(event);
                }
            }
        }
    }
}


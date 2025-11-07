package com.example.gitcat_events.testData;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Realistic test event data
 * Contains pre-defined event scenarios for testing
 */
public class TestEventData {

    /**
     * Returns a map of realistic event names and descriptions
     */
    public static Map<String, String> getEventNamesAndDescriptions() {
        Map<String, String> events = new HashMap<>();
        events.put("Edmonton Tech Conference 2025",
                "Join us for a day of talks on Android development, AI, and cloud computing. " +
                        "Network with industry professionals and learn from expert speakers.");
        events.put("Campus Scavenger Hunt",
                "Explore the campus while solving clues! Location tracking required to participate.");
        events.put("Annual Charity Gala",
                "An elegant evening supporting local charities. Formal attire required.");
        events.put("Hackathon 2025",
                "24-hour coding competition. Build innovative solutions and compete for prizes.");
        events.put("Workshop: Introduction to Machine Learning",
                "Learn the basics of ML with hands-on exercises. No prior experience required.");
        events.put("Networking Mixer",
                "Connect with professionals in your field. Light refreshments provided.");
        events.put("Study Group Session",
                "Collaborative study session for upcoming exams. Bring your notes!");
        events.put("Movie Night: Sci-Fi Classics",
                "Join us for a screening of classic science fiction films. Popcorn included.");
        return events;
    }

    /**
     * Returns realistic event capacities for testing
     */
    public static int[] getRealisticCapacities() {
        return new int[]{25, 50, 100, 200, 500};
    }

    /**
     * Returns realistic waitlist sizes for testing
     */
    public static Integer[] getRealisticWaitlistSizes() {
        return new Integer[]{10, 20, 50, 100, null}; // null means unlimited
    }

    /**
     * Creates an event that is currently open for registration
     */
    public static Event createOpenRegistrationEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, 14); // Registration open for 14 more days

        Event event = new Event(
                "Open Registration Event",
                "This event is currently accepting registrations.",
                100,
                50,
                "https://example.com/posters/open-event.jpg",
                raffleDate,
                eventDate,
                false
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        return event;
    }

    /**
     * Creates an event with registration closed
     */
    public static Event createClosedRegistrationEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, -1); // Registration closed yesterday

        Event event = new Event(
                "Closed Registration Event",
                "Registration for this event has closed.",
                100,
                50,
                "https://example.com/posters/closed-event.jpg",
                raffleDate,
                eventDate,
                false
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        return event;
    }

    /**
     * Creates an event happening today
     */
    public static Event createTodayEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        // Today

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, -7); // Registration closed a week ago

        Event event = new Event(
                "Event Happening Today",
                "This event is happening right now!",
                50,
                25,
                "https://example.com/posters/today-event.jpg",
                raffleDate,
                eventDate,
                false
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        return event;
    }

    /**
     * Creates an event that has already passed
     */
    public static Event createPastEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, -30); // 30 days ago

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, -45); // Registration closed 45 days ago

        Event event = new Event(
                "Past Event",
                "This event has already concluded.",
                100,
                50,
                "https://example.com/posters/past-event.jpg",
                raffleDate,
                eventDate,
                false
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        return event;
    }
}


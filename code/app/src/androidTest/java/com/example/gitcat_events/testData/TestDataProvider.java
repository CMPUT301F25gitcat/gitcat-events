package com.example.gitcat_events.testData;

import com.example.gitcat_events.core.model.AcceptedListEntry;
import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.InvitationListEntry;
import com.example.gitcat_events.core.model.Profile;
import com.example.gitcat_events.core.model.WaitListEntry;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data provider for creating realistic test data
 * Provides factory methods for creating test instances of model classes
 */
public class TestDataProvider {

    // Realistic test device IDs
    public static final String DEVICE_ID_1 = "550e8400-e29b-41d4-a716-446655440000";
    public static final String DEVICE_ID_2 = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
    public static final String DEVICE_ID_3 = "6ba7b811-9dad-11d1-80b4-00c04fd430c8";
    public static final String DEVICE_ID_4 = "6ba7b812-9dad-11d1-80b4-00c04fd430c8";
    public static final String DEVICE_ID_5 = "6ba7b813-9dad-11d1-80b4-00c04fd430c8";

    // Realistic test profile IDs
    public static final String PROFILE_ID_1 = "0";
    public static final String PROFILE_ID_2 = "1";
    public static final String PROFILE_ID_3 = "2";
    public static final String PROFILE_ID_4 = "3";
    public static final String PROFILE_ID_5 = "4";

    // Realistic test event IDs
    public static final String EVENT_ID_1 = "0";
    public static final String EVENT_ID_2 = "1";
    public static final String EVENT_ID_3 = "2";

    /**
     * Creates a realistic test profile with all fields
     */
    public static Profile createFullProfile(String deviceId) {
        return new Profile(
                "John Doe",
                "john.doe@example.com",
                "780-123-4567",
                deviceId,
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD..."
        );
    }

    /**
     * Creates a minimal test profile with only device ID
     */
    public static Profile createMinimalProfile(String deviceId) {
        return new Profile(deviceId);
    }

    /**
     * Creates a test profile with partial information
     */
    public static Profile createPartialProfile(String deviceId) {
        Profile profile = new Profile(deviceId);
        profile.setName("Jane Smith");
        profile.setEmail("jane.smith@example.com");
        // Phone and picture are null
        return profile;
    }

    /**
     * Creates a realistic test event
     */
    public static Event createTestEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30); // 30 days from now

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, 7); // 7 days from now

        Event event = new Event(
                "Edmonton Tech Conference 2025",
                "Join us for a day of talks on Android development, AI, and cloud computing. " +
                        "Network with industry professionals and learn from expert speakers.",
                100,
                50, // max waitlist size
                "https://example.com/posters/tech-conf-2025.jpg",
                raffleDate,
                eventDate,
                false // geo location not required
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        event.setSelectionCriteria("Random selection from all registered participants. " +
                "All entrants have an equal chance of being selected.");

        return event;
    }

    /**
     * Creates a test event with geo location required
     */
    public static Event createGeoLocationEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 14);

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, 3);

        Event event = new Event(
                "Campus Scavenger Hunt",
                "Explore the campus while solving clues! Location tracking required to participate.",
                50,
                25,
                "https://example.com/posters/scavenger-hunt.jpg",
                raffleDate,
                eventDate,
                true // geo location required
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        event.setSelectionCriteria("First come, first served. Location verification required.");

        return event;
    }

    /**
     * Creates a test event with no waitlist limit
     */
    public static Event createUnlimitedWaitlistEvent(String eventId, String organizerDeviceId) {
        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 60);

        Calendar raffleDate = Calendar.getInstance();
        raffleDate.add(Calendar.DAY_OF_MONTH, 14);

        Event event = new Event(
                "Annual Charity Gala",
                "An elegant evening supporting local charities. Formal attire required.",
                200,
                null, // no waitlist limit
                "https://example.com/posters/charity-gala.jpg",
                raffleDate,
                eventDate,
                false
        );
        event.setDocumentId(eventId);
        event.setOrganizerDeviceId(organizerDeviceId);
        event.setSelectionCriteria("Random lottery selection. All registered participants eligible.");

        return event;
    }

    /**
     * Creates a waitlist entry
     */
    public static WaitListEntry createWaitListEntry(String eventId, String userDeviceId) {
        return new WaitListEntry(eventId, userDeviceId);
    }

    /**
     * Creates an accepted list entry
     */
    public static AcceptedListEntry createAcceptedListEntry(String eventId, String userDeviceId) {
        return new AcceptedListEntry(eventId, userDeviceId);
    }

    /**
     * Creates an accepted list entry with specific draw round
     */
    public static AcceptedListEntry createAcceptedListEntryWithRound(String eventId, String userDeviceId, int drawRound) {
        return new AcceptedListEntry(eventId, userDeviceId, drawRound);
    }

    /**
     * Creates an invitation list entry
     */
    public static InvitationListEntry createInvitationListEntry(String eventId, String userDeviceId) {
        return new InvitationListEntry(eventId, userDeviceId);
    }

    /**
     * Creates an invitation list entry with specific draw round
     */
    public static InvitationListEntry createInvitationListEntryWithRound(String eventId, String userDeviceId, int drawRound) {
        return new InvitationListEntry(eventId, userDeviceId, drawRound);
    }

    /**
     * Creates a list of realistic test profiles
     */
    public static List<Profile> createTestProfiles() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(createFullProfile(DEVICE_ID_1));
        profiles.add(new Profile("Alice Johnson", "alice.j@example.com", "780-234-5678", DEVICE_ID_2, null));
        profiles.add(new Profile("Bob Williams", "bob.williams@example.com", null, DEVICE_ID_3, null));
        profiles.add(createPartialProfile(DEVICE_ID_4));
        profiles.add(createMinimalProfile(DEVICE_ID_5));
        return profiles;
    }

    /**
     * Creates a list of realistic test events
     */
    public static List<Event> createTestEvents(String organizerDeviceId) {
        List<Event> events = new ArrayList<>();
        events.add(createTestEvent(EVENT_ID_1, organizerDeviceId));
        events.add(createGeoLocationEvent(EVENT_ID_2, organizerDeviceId));
        events.add(createUnlimitedWaitlistEvent(EVENT_ID_3, organizerDeviceId));
        return events;
    }

    /**
     * Creates realistic test data for waitlist entries
     */
    public static List<WaitListEntry> createWaitListEntries(String eventId, int count) {
        List<WaitListEntry> entries = new ArrayList<>();
        String[] deviceIds = {DEVICE_ID_1, DEVICE_ID_2, DEVICE_ID_3, DEVICE_ID_4, DEVICE_ID_5};
        for (int i = 0; i < count && i < deviceIds.length; i++) {
            entries.add(createWaitListEntry(eventId, deviceIds[i]));
        }
        return entries;
    }

    /**
     * Creates realistic test data for accepted list entries
     */
    public static List<AcceptedListEntry> createAcceptedListEntries(String eventId, int count) {
        List<AcceptedListEntry> entries = new ArrayList<>();
        String[] deviceIds = {DEVICE_ID_1, DEVICE_ID_2, DEVICE_ID_3, DEVICE_ID_4, DEVICE_ID_5};
        for (int i = 0; i < count && i < deviceIds.length; i++) {
            entries.add(createAcceptedListEntry(eventId, deviceIds[i]));
        }
        return entries;
    }

    /**
     * Creates a calendar date in the future
     */
    public static Calendar createFutureDate(int daysFromNow) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, daysFromNow);
        return date;
    }

    /**
     * Creates a calendar date in the past
     */
    public static Calendar createPastDate(int daysAgo) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, -daysAgo);
        return date;
    }
}


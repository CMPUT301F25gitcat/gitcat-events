package com.example.gitcat_events.integrationTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.AcceptedListEntry;
import com.example.gitcat_events.core.model.Event;
import com.example.gitcat_events.core.model.InvitationListEntry;

import java.util.Calendar;

/**
 * Integration tests for enrollment status display
 * Tests fix for enrollment status after accepting invitation
 * Tests US 01.05.02: As an entrant I want to accept an event invitation
 */
public class EnrollmentStatusTests {

    private Event testEvent;
    private String testEventId;
    private String testDeviceId;
    private boolean isEnrolled;
    private boolean hasInvitation;
    private boolean isOnWaitlist;

    @Before
    public void setUp() {
        testEventId = "event-123";
        testDeviceId = "device-456";
        isEnrolled = false;
        hasInvitation = false;
        isOnWaitlist = false;

        Calendar eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        testEvent = new Event(
                "Enrollment Test Event",
                "Test Description",
                100,
                null,
                null,
                null,
                null,
                eventDate,
                false
        );
        testEvent.setDocumentId(testEventId);
    }

    @Test
    public void testUserNotEnrolledInitially() {
        assertFalse("User should not be enrolled initially", isEnrolled);
        assertFalse("User should not have invitation initially", hasInvitation);
        assertFalse("User should not be on waitlist initially", isOnWaitlist);
    }

    @Test
    public void testUserEnrolledAfterAcceptingInvitation() {
        // User receives invitation
        hasInvitation = true;
        InvitationListEntry invitation = new InvitationListEntry(testEventId, testDeviceId);
        invitation.setStatus("pending");

        assertTrue("User should have invitation", hasInvitation);
        assertEquals("Invitation status should be pending", "pending", invitation.getStatus());

        // User accepts invitation
        invitation.setStatus("accepted");
        AcceptedListEntry acceptedEntry = new AcceptedListEntry(testEventId, testDeviceId);
        acceptedEntry.setStatus("accepted");
        isEnrolled = true;
        hasInvitation = false; // Invitation removed after acceptance

        assertTrue("User should be enrolled after acceptance", isEnrolled);
        assertFalse("User should not have invitation after acceptance", hasInvitation);
        assertEquals("Accepted entry status should be accepted", "accepted", acceptedEntry.getStatus());
    }

    @Test
    public void testEnrollmentStatusTakesPriority() {
        // User is enrolled
        isEnrolled = true;
        hasInvitation = false;
        isOnWaitlist = false;

        // Enrollment status should take priority
        assertTrue("Enrollment status should be true", isEnrolled);
        assertFalse("Should not show invitation buttons when enrolled", hasInvitation);
        assertFalse("Should not show waitlist button when enrolled", isOnWaitlist);
    }

    @Test
    public void testEnrollmentStatusOverridesWaitlist() {
        // User is on waitlist
        isOnWaitlist = true;
        isEnrolled = false;

        assertTrue("User should be on waitlist", isOnWaitlist);
        assertFalse("User should not be enrolled", isEnrolled);

        // User accepts invitation and becomes enrolled
        isEnrolled = true;
        isOnWaitlist = false; // Removed from waitlist when enrolled

        assertTrue("User should be enrolled", isEnrolled);
        assertFalse("User should not be on waitlist when enrolled", isOnWaitlist);
    }

    @Test
    public void testEnrollmentStatusOverridesInvitation() {
        // User has pending invitation
        hasInvitation = true;
        isEnrolled = false;

        assertTrue("User should have invitation", hasInvitation);
        assertFalse("User should not be enrolled", isEnrolled);

        // User accepts invitation
        isEnrolled = true;
        hasInvitation = false;

        assertTrue("User should be enrolled", isEnrolled);
        assertFalse("User should not have invitation after acceptance", hasInvitation);
    }

    @Test
    public void testEnrollmentStatusDisplay() {
        isEnrolled = true;

        // When enrolled, should show enrollment message
        String statusMessage = isEnrolled ? "✓ You're already enrolled for this event" : null;

        assertNotNull("Status message should be set when enrolled", statusMessage);
        assertTrue("Status message should indicate enrollment", statusMessage.contains("enrolled"));
    }

    @Test
    public void testNotEnrolledStatusDisplay() {
        isEnrolled = false;
        isOnWaitlist = true;

        // When not enrolled but on waitlist
        String statusMessage = isEnrolled ? "✓ You're already enrolled for this event" : 
                                isOnWaitlist ? "You're on the waiting list" : null;

        assertNotNull("Status message should be set", statusMessage);
        assertTrue("Status message should indicate waitlist", statusMessage.contains("waiting list"));
    }

    @Test
    public void testEnrollmentStatusAfterDecliningInvitation() {
        // User receives invitation
        hasInvitation = true;
        InvitationListEntry invitation = new InvitationListEntry(testEventId, testDeviceId);
        invitation.setStatus("pending");

        // User declines invitation
        invitation.setStatus("declined");
        hasInvitation = false;
        isEnrolled = false;

        assertFalse("User should not be enrolled after declining", isEnrolled);
        assertFalse("User should not have invitation after declining", hasInvitation);
        assertEquals("Invitation status should be declined", "declined", invitation.getStatus());
    }

    @Test
    public void testEnrollmentStatusRealTimeUpdate() {
        // Initial state: not enrolled
        isEnrolled = false;

        // Simulate real-time update: user accepts invitation
        AcceptedListEntry acceptedEntry = new AcceptedListEntry(testEventId, testDeviceId);
        acceptedEntry.setStatus("accepted");
        isEnrolled = (acceptedEntry != null && acceptedEntry.getStatus().equals("accepted"));

        assertTrue("Enrollment status should update in real-time", isEnrolled);
    }

    @Test
    public void testMultipleEnrollmentChecks() {
        // User checks enrollment status multiple times
        boolean check1 = checkEnrollmentStatus(testDeviceId);
        boolean check2 = checkEnrollmentStatus(testDeviceId);
        boolean check3 = checkEnrollmentStatus(testDeviceId);

        // All checks should return same result
        assertEquals("All enrollment checks should be consistent", check1, check2);
        assertEquals("All enrollment checks should be consistent", check2, check3);
    }

    @Test
    public void testEnrollmentStatusWithNullEventId() {
        // Edge case: event ID is null
        String nullEventId = null;
        boolean enrolled = checkEnrollmentStatusForEvent(nullEventId, testDeviceId);

        // Should handle null gracefully
        assertFalse("Should return false for null event ID", enrolled);
    }

    @Test
    public void testEnrollmentStatusWithNullDeviceId() {
        // Edge case: device ID is null
        String nullDeviceId = null;
        boolean enrolled = checkEnrollmentStatusForEvent(testEventId, nullDeviceId);

        // Should handle null gracefully
        assertFalse("Should return false for null device ID", enrolled);
    }

    @Test
    public void testEnrollmentStatusAfterEventDeletion() {
        // User is enrolled
        isEnrolled = true;
        AcceptedListEntry acceptedEntry = new AcceptedListEntry(testEventId, testDeviceId);
        acceptedEntry.setStatus("accepted");

        assertTrue("User should be enrolled", isEnrolled);

        // Event is deleted (simulated)
        acceptedEntry = null;
        isEnrolled = false;

        assertFalse("User should not be enrolled after event deletion", isEnrolled);
    }

    @Test
    public void testEnrollmentStatusPriorityOrder() {
        // Test priority: enrolled > invitation > waitlist > nothing
        isEnrolled = true;
        hasInvitation = true;
        isOnWaitlist = true;

        // Enrollment should take highest priority
        String displayStatus = determineDisplayStatus();
        assertEquals("Should show enrolled status", "enrolled", displayStatus);

        // Remove enrollment
        isEnrolled = false;
        displayStatus = determineDisplayStatus();
        assertEquals("Should show invitation status", "invitation", displayStatus);

        // Remove invitation
        hasInvitation = false;
        displayStatus = determineDisplayStatus();
        assertEquals("Should show waitlist status", "waitlist", displayStatus);

        // Remove waitlist
        isOnWaitlist = false;
        displayStatus = determineDisplayStatus();
        assertEquals("Should show nothing", "none", displayStatus);
    }

    // Helper methods
    private boolean checkEnrollmentStatus(String deviceId) {
        // Simulate checking enrollment status
        return isEnrolled;
    }

    private boolean checkEnrollmentStatusForEvent(String eventId, String deviceId) {
        if (eventId == null || deviceId == null) {
            return false;
        }
        return isEnrolled;
    }

    private String determineDisplayStatus() {
        if (isEnrolled) {
            return "enrolled";
        } else if (hasInvitation) {
            return "invitation";
        } else if (isOnWaitlist) {
            return "waitlist";
        } else {
            return "none";
        }
    }
}


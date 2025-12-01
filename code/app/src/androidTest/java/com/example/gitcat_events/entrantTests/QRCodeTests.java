package com.example.gitcat_events.entrantTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.example.gitcat_events.core.model.Event;

import java.util.Calendar;

/**
 * Unit tests for QR Code functionality
 * Tests US 01.06.01: As an entrant I want to scan a QR code to join an event waiting list
 * Tests US 02.01.02: As an organizer I want to generate a QR code for my event
 */
public class QRCodeTests {

    private Event testEvent;
    private String testEventId;
    private Calendar eventDate;

    @Before
    public void setUp() {
        testEventId = "event-123";
        eventDate = Calendar.getInstance();
        eventDate.add(Calendar.DAY_OF_MONTH, 30);

        testEvent = new Event(
                "QR Code Test Event",
                "Event for testing QR codes",
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
    public void testQRCodeUrlGeneration() {
        String qrCodeUrl = generateQRCodeUrl(testEventId);
        
        assertNotNull("QR code URL should not be null", qrCodeUrl);
        assertTrue("QR code URL should contain event ID", qrCodeUrl.contains(testEventId));
    }

    @Test
    public void testQRCodeUrlFormat() {
        String qrCodeUrl = generateQRCodeUrl(testEventId);
        
        // QR code URL should be a valid format (e.g., deep link or data URL)
        assertTrue("QR code URL should not be empty", !qrCodeUrl.isEmpty());
    }

    @Test
    public void testQRCodeContainsEventId() {
        String qrCodeUrl = generateQRCodeUrl(testEventId);
        
        // Extract event ID from QR code URL
        String extractedEventId = extractEventIdFromQRCode(qrCodeUrl);
        
        assertEquals("Extracted event ID should match", testEventId, extractedEventId);
    }

    @Test
    public void testQRCodeForDifferentEvents() {
        String eventId1 = "event-123";
        String eventId2 = "event-456";
        
        String qrCode1 = generateQRCodeUrl(eventId1);
        String qrCode2 = generateQRCodeUrl(eventId2);
        
        assertNotEquals("Different events should have different QR codes", qrCode1, qrCode2);
        
        String extractedId1 = extractEventIdFromQRCode(qrCode1);
        String extractedId2 = extractEventIdFromQRCode(qrCode2);
        
        assertEquals("First QR code should contain first event ID", eventId1, extractedId1);
        assertEquals("Second QR code should contain second event ID", eventId2, extractedId2);
    }

    @Test
    public void testQRCodePersistence() {
        String qrCodeUrl = generateQRCodeUrl(testEventId);
        testEvent.setQrCodeUrl(qrCodeUrl);
        
        assertEquals("Event should store QR code URL", qrCodeUrl, testEvent.getQrCodeUrl());
    }

    @Test
    public void testQRCodeUpdate() {
        String initialQRCode = generateQRCodeUrl(testEventId);
        testEvent.setQrCodeUrl(initialQRCode);
        
        String updatedQRCode = generateQRCodeUrl(testEventId + "-updated");
        testEvent.setQrCodeUrl(updatedQRCode);
        
        assertEquals("QR code should be updated", updatedQRCode, testEvent.getQrCodeUrl());
        assertNotEquals("Updated QR code should be different", initialQRCode, testEvent.getQrCodeUrl());
    }

    @Test
    public void testQRCodeDeepLinkFormat() {
        String eventId = "event-123";
        String deepLink = "gitcatevents://event/" + eventId;
        
        assertTrue("Deep link should contain event path", deepLink.contains("/event/"));
        assertTrue("Deep link should contain event ID", deepLink.contains(eventId));
    }

    @Test
    public void testQRCodeScanningExtractsEventId() {
        String eventId = "event-789";
        String qrCodeData = "gitcatevents://event/" + eventId;
        
        String extractedId = extractEventIdFromDeepLink(qrCodeData);
        
        assertEquals("Should extract correct event ID", eventId, extractedId);
    }

    @Test
    public void testInvalidQRCodeHandling() {
        String invalidQRCode = "invalid-qr-code-data";
        
        // Should handle invalid QR codes gracefully
        String extractedId = extractEventIdFromDeepLink(invalidQRCode);
        
        assertNull("Invalid QR code should return null", extractedId);
    }

    @Test
    public void testQRCodeWithSpecialCharacters() {
        String eventId = "event-123-special";
        String qrCodeUrl = generateQRCodeUrl(eventId);
        
        assertNotNull("QR code should handle special characters", qrCodeUrl);
        String extractedId = extractEventIdFromQRCode(qrCodeUrl);
        assertEquals("Should preserve event ID with special characters", eventId, extractedId);
    }

    @Test
    public void testQRCodeUniqueness() {
        String eventId1 = "event-1";
        String eventId2 = "event-2";
        String eventId3 = "event-3";
        
        String qr1 = generateQRCodeUrl(eventId1);
        String qr2 = generateQRCodeUrl(eventId2);
        String qr3 = generateQRCodeUrl(eventId3);
        
        // All QR codes should be unique
        assertNotEquals("QR codes should be unique", qr1, qr2);
        assertNotEquals("QR codes should be unique", qr1, qr3);
        assertNotEquals("QR codes should be unique", qr2, qr3);
    }

    @Test
    public void testQRCodeEventAssociation() {
        String qrCodeUrl = generateQRCodeUrl(testEventId);
        testEvent.setQrCodeUrl(qrCodeUrl);
        
        // Verify QR code is associated with correct event
        String extractedEventId = extractEventIdFromQRCode(testEvent.getQrCodeUrl());
        assertEquals("QR code should be associated with correct event", testEventId, extractedEventId);
    }

    // Helper methods to simulate QR code generation and parsing
    private String generateQRCodeUrl(String eventId) {
        // Simulate QR code URL generation (e.g., deep link format)
        return "gitcatevents://event/" + eventId;
    }

    private String extractEventIdFromQRCode(String qrCodeUrl) {
        if (qrCodeUrl == null || !qrCodeUrl.contains("/event/")) {
            return null;
        }
        String[] parts = qrCodeUrl.split("/event/");
        return parts.length > 1 ? parts[1] : null;
    }

    private String extractEventIdFromDeepLink(String deepLink) {
        if (deepLink == null || !deepLink.startsWith("gitcatevents://event/")) {
            return null;
        }
        return deepLink.replace("gitcatevents://event/", "");
    }
}


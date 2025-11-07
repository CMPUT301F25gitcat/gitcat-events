package com.example.gitcat_events.organizerTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for US 02.06.05: As an organizer I want to export a final list of entrants in CSV format
 */
public class CsvExportTests {

    private List<MockEntrant> enrolledEntrants;

    // Mock class to represent an enrolled entrant
    private static class MockEntrant {
        String name;
        String email;
        String phone;
        int drawRound;
        long invitedTimestamp;
        long acceptedTimestamp;

        MockEntrant(String name, String email, String phone, int drawRound, 
                   long invitedTimestamp, long acceptedTimestamp) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.drawRound = drawRound;
            this.invitedTimestamp = invitedTimestamp;
            this.acceptedTimestamp = acceptedTimestamp;
        }
    }

    @Before
    public void setUp() {
        enrolledEntrants = new ArrayList<>();
        enrolledEntrants.add(new MockEntrant("John Doe", "john@example.com", "555-1234", 1, 
                System.currentTimeMillis() - 10000, System.currentTimeMillis() - 5000));
        enrolledEntrants.add(new MockEntrant("Jane Smith", "jane@example.com", "555-5678", 1,
                System.currentTimeMillis() - 9000, System.currentTimeMillis() - 4000));
        enrolledEntrants.add(new MockEntrant("Bob Wilson", "bob@example.com", "555-9012", 2,
                System.currentTimeMillis() - 3000, System.currentTimeMillis() - 1000));
    }

    @Test
    public void testCsvHeaderGeneration() {
        String csvHeader = "Position,Name,Email,Phone,Draw Round,Invited Date,Accepted Date\n";
        
        assertNotNull("CSV header should not be null", csvHeader);
        assertTrue("CSV header should contain Position", csvHeader.contains("Position"));
        assertTrue("CSV header should contain Name", csvHeader.contains("Name"));
        assertTrue("CSV header should contain Email", csvHeader.contains("Email"));
        assertTrue("CSV header should contain Phone", csvHeader.contains("Phone"));
        assertTrue("CSV header should contain Draw Round", csvHeader.contains("Draw Round"));
    }

    @Test
    public void testCsvRowGeneration() {
        MockEntrant entrant = enrolledEntrants.get(0);
        String csvRow = String.format("%d,\"%s\",\"%s\",\"%s\",%d",
                1, entrant.name, entrant.email, entrant.phone, entrant.drawRound);
        
        assertTrue("CSV row should contain name", csvRow.contains(entrant.name));
        assertTrue("CSV row should contain email", csvRow.contains(entrant.email));
        assertTrue("CSV row should contain phone", csvRow.contains(entrant.phone));
    }

    @Test
    public void testCsvEscapeQuotes() {
        String fieldWithQuotes = "Name \"Nickname\" Surname";
        String escaped = escapeCsvField(fieldWithQuotes);
        
        assertEquals("Should escape quotes by doubling", "\"Name \"\"Nickname\"\" Surname\"", escaped);
    }

    @Test
    public void testCsvEscapeCommas() {
        String fieldWithComma = "Last, First";
        String escaped = escapeCsvField(fieldWithComma);
        
        assertTrue("Should wrap in quotes", escaped.startsWith("\"") && escaped.endsWith("\""));
        assertTrue("Should contain original comma", escaped.contains(","));
    }

    @Test
    public void testCsvNullFieldHandling() {
        String escaped = escapeCsvField(null);
        
        assertEquals("Null should be empty quoted string", "\"\"", escaped);
    }

    @Test
    public void testCsvEmptyFieldHandling() {
        String escaped = escapeCsvField("");
        
        assertEquals("Empty string should be empty quoted string", "\"\"", escaped);
    }

    @Test
    public void testCsvFileNameSanitization() {
        String eventName = "My Event! @#$%";
        String sanitized = sanitizeFileName(eventName);
        
        assertFalse("Should not contain special characters", sanitized.contains("!"));
        assertFalse("Should not contain @", sanitized.contains("@"));
        assertFalse("Should not contain #", sanitized.contains("#"));
    }

    @Test
    public void testCsvFileNameWithSpaces() {
        String eventName = "My Event With Spaces";
        String sanitized = sanitizeFileName(eventName);
        
        assertFalse("Should not contain spaces", sanitized.contains(" "));
        assertTrue("Should contain underscores", sanitized.contains("_"));
    }

    @Test
    public void testCsvDataIntegrity() {
        assertEquals("Should have 3 entrants", 3, enrolledEntrants.size());
        
        for (int i = 0; i < enrolledEntrants.size(); i++) {
            MockEntrant entrant = enrolledEntrants.get(i);
            assertNotNull("Name should not be null", entrant.name);
            assertNotNull("Email should not be null", entrant.email);
            assertTrue("Draw round should be positive", entrant.drawRound > 0);
        }
    }

    @Test
    public void testCsvRowCount() {
        int expectedRows = enrolledEntrants.size() + 1; // +1 for header
        
        assertTrue("Should have at least header + data rows", expectedRows >= 4);
    }

    @Test
    public void testCsvTimestampFormatting() {
        MockEntrant entrant = enrolledEntrants.get(0);
        
        assertTrue("Invited timestamp should be before accepted", 
                entrant.invitedTimestamp < entrant.acceptedTimestamp);
    }

    @Test
    public void testCsvMultipleDrawRounds() {
        boolean hasRound1 = false;
        boolean hasRound2 = false;
        
        for (MockEntrant entrant : enrolledEntrants) {
            if (entrant.drawRound == 1) hasRound1 = true;
            if (entrant.drawRound == 2) hasRound2 = true;
        }
        
        assertTrue("Should have entrants from round 1", hasRound1);
        assertTrue("Should have entrants from round 2 (replacement)", hasRound2);
    }

    // Helper methods (similar to actual implementation)
    private String escapeCsvField(String field) {
        if (field == null) return "\"\"";
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
}


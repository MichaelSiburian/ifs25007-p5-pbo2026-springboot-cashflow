package org.delcom.ifs25007springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    @Test
    void hashIsSha256Hex() {
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
                PasswordHasher.hash("password"));
    }

    @Test
    void matchesComparesHash() {
        String hash = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.matches("rahasia", hash));
        assertFalse(PasswordHasher.matches("salah", hash));
    }

    @Test
    void unknownAlgorithmIsRejected() {
        assertThrows(IllegalStateException.class, () -> PasswordHasher.digest("NOPE-256", "x"));
    }
}

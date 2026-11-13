package org.delcom.ifs25007springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25007springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthUseCaseTest {
    private AuthUseCase auth;

    @BeforeEach
    void setUp() {
        auth = new AuthUseCase(new UserRepository());
    }

    @Test
    void registerCreatesUserWithHashedPassword() throws Exception {
        User u = auth.register("  bos  ", "rahasia", "  Bos Besar ");
        assertEquals("bos", u.getUsername());
        assertEquals("Bos Besar", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals(PasswordHasher.hash("rahasia"), u.getPasswordHash());
    }

    @Test
    void registerValidation() throws Exception {
        assertThrows(ValidationException.class, () -> auth.register(null, "rahasia", "Bos"));
        assertThrows(ValidationException.class, () -> auth.register("  ", "rahasia", "Bos"));
        assertThrows(ValidationException.class, () -> auth.register("ab", "rahasia", "Bos"));
        assertThrows(ValidationException.class, () -> auth.register("bos", "rahasia", " "));
        assertThrows(ValidationException.class, () -> auth.register("bos", null, "Bos"));
        assertThrows(ValidationException.class, () -> auth.register("bos", "123", "Bos"));
        auth.register("bos", "rahasia", "Bos");
        assertThrows(ValidationException.class, () -> auth.register("bos", "rahasia", "Bos"));
    }

    @Test
    void loginSucceedsAndFails() throws Exception {
        auth.register("bos", "rahasia", "Bos");
        assertEquals("bos", auth.login(" bos ", "rahasia").getUsername());
        assertThrows(ValidationException.class, () -> auth.login(null, "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("bos", null));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "rahasia"));
        assertThrows(ValidationException.class, () -> auth.login("bos", "salah"));
    }

    @Test
    void ensureRootAdminSeedsOnce() {
        User first = auth.ensureRootAdmin("admin", "admin");
        User second = auth.ensureRootAdmin("admin", "lain");
        assertEquals(Role.ADMIN, first.getRole());
        assertEquals("Administrator", first.getFullName());
        assertSame(first, second);
    }
}

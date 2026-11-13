package org.delcom.ifs25007springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25007springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCaseTest {
    private UserUseCase useCase;
    private User user;

    @BeforeEach
    void setUp() {
        UserRepository repo = new UserRepository();
        user = repo.save("bos", PasswordHasher.hash("lama123"), "Bos", Role.USER);
        useCase = new UserUseCase(repo);
    }

    @Test
    void listAndGet() throws Exception {
        assertEquals(1, useCase.findAll().size());
        assertSame(user, useCase.get(user.getId()));
        assertThrows(EntityNotFoundException.class, () -> useCase.get(99));
    }

    @Test
    void updateProfileKeepsPasswordWhenBlank() throws Exception {
        String oldHash = user.getPasswordHash();
        useCase.updateProfile(user.getId(), "  Bos Baru ", null);
        assertEquals("Bos Baru", user.getFullName());
        useCase.updateProfile(user.getId(), "Bos Baru", "   ");
        assertEquals(oldHash, user.getPasswordHash());
    }

    @Test
    void updateProfileChangesPassword() throws Exception {
        useCase.updateProfile(user.getId(), "Bos", "baru1234");
        assertEquals(PasswordHasher.hash("baru1234"), user.getPasswordHash());
    }

    @Test
    void updateProfileValidation() {
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "Bos", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), null, null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), " ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(user.getId(), "Bos", "123"));
    }

    @Test
    void updatePhoto() throws Exception {
        useCase.updatePhoto(user.getId(), "foto.png");
        assertEquals("foto.png", user.getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "x.png"));
    }
}

package org.delcom.ifs25007springbootcashflow.usecase;

import org.delcom.ifs25007springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;

/** Registrasi, login, dan seed admin. Tanpa I/O UI. */
public class AuthUseCase {
    private static final String LOGIN_FAILED = "Username atau password salah.";

    private final IUserRepository repository;

    public AuthUseCase(IUserRepository repository) {
        this.repository = repository;
    }

    public User register(String username, String password, String fullName)
            throws DomainException {
        String cleanUsername = requireText(username, "Username");
        if (cleanUsername.length() < 3) {
            throw new ValidationException("Username minimal 3 karakter.");
        }
        String cleanName = requireText(fullName, "Nama lengkap");
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password minimal 6 karakter.");
        }
        if (repository.findByUsername(cleanUsername).isPresent()) {
            throw new ValidationException("Username sudah dipakai.");
        }
        return repository.save(cleanUsername, PasswordHasher.hash(password), cleanName, Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        if (username == null || password == null) {
            throw new ValidationException("Username dan password wajib diisi.");
        }
        User user = repository.findByUsername(username.trim())
                .orElseThrow(() -> new ValidationException(LOGIN_FAILED));
        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException(LOGIN_FAILED);
        }
        return user;
    }

    /** Membuat admin dari konfigurasi ROOT_* bila belum ada. */
    public User ensureRootAdmin(String username, String password) {
        return repository.findByUsername(username).orElseGet(() ->
                repository.save(username, PasswordHasher.hash(password), "Administrator",
                        Role.ADMIN));
    }

    private static String requireText(String value, String label) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(label + " tidak boleh kosong.");
        }
        return value.trim();
    }
}

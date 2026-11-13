package org.delcom.ifs25007springbootcashflow.usecase;

import org.delcom.ifs25007springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import java.util.List;

/** Daftar pengguna dan pembaruan profil. Tanpa I/O UI. */
public class UserUseCase {
    private final IUserRepository repository;

    public UserUseCase(IUserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User get(int id) throws EntityNotFoundException {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    /** Password baru opsional: kosong berarti password tidak diubah. */
    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = get(id);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong.");
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                throw new ValidationException("Password minimal 6 karakter.");
            }
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        user.changeFullName(fullName.trim());
        repository.update(user);
        return user;
    }

    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = get(id);
        user.changePhotoPath(photoPath);
        repository.update(user);
        return user;
    }
}

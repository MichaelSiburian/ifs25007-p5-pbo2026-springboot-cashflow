package org.delcom.ifs25007springbootcashflow.adapter.repository;

import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** Penyimpanan pengguna di memori dengan ID otomatis. */
public class UserRepository extends InMemoryRepository<User, Integer> implements IUserRepository {
    private final AtomicInteger sequence = new AtomicInteger(1);

    @Override
    protected Integer idOf(User entity) {
        return entity.getId();
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return save(new User(sequence.getAndIncrement(), username, passwordHash, fullName, role,
                null));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findBy(u -> u.getUsername().equals(username)).stream().findFirst();
    }
}

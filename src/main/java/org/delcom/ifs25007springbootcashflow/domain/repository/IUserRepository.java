package org.delcom.ifs25007springbootcashflow.domain.repository;

import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import java.util.Optional;

/** Port penyimpanan pengguna. ID dibuat otomatis oleh implementasi. */
public interface IUserRepository extends IRepository<User, Integer> {
    User save(String username, String passwordHash, String fullName, Role role);

    Optional<User> findByUsername(String username);
}

package org.delcom.ifs25007springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port penyimpanan generik. */
public interface IRepository<T, ID> {
    T save(T entity);

    Optional<T> findById(ID id);

    /** Mengembalikan salinan list (defensif). */
    List<T> findAll();

    void update(T entity);

    /** @return true bila ada entitas yang dihapus. */
    boolean deleteById(ID id);

    default List<T> findBy(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).toList();
    }
}

package org.delcom.ifs25007springbootcashflow.adapter.repository;

import org.delcom.ifs25007springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementasi generik penyimpanan di memori (untuk unit test). */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    private final Map<ID, T> store = new LinkedHashMap<>();

    protected abstract ID idOf(T entity);

    @Override
    public T save(T entity) {
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(T entity) {
        store.put(idOf(entity), entity);
    }

    @Override
    public boolean deleteById(ID id) {
        return store.remove(id) != null;
    }
}

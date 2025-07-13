package dev.hyperapi.runtime.core.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

public interface BasePanacheRepository<ENTITY, ID> extends PanacheRepositoryBase<ENTITY, ID> {
}
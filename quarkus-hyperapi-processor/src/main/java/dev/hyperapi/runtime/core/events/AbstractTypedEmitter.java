package dev.hyperapi.runtime.core.events;

public abstract class AbstractTypedEmitter<T> implements EntityEmitter<T> {
    private final Class<T> entityType;

    protected AbstractTypedEmitter(Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public void emit(EntityEvent.Type type, T entity) {
        if (entityType.isInstance(entity) || entity == null) {
            emitTyped(type, entity);
        }
    }

    protected abstract void emitTyped(EntityEvent.Type type, T entity);
}

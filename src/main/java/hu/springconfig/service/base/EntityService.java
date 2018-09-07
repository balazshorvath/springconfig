package hu.springconfig.service.base;

import hu.springconfig.data.query.model.Condition;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.util.SpecificationsUtils;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.Collections;

// TODO somehow it would be great to have these messages also as static strings
public abstract class EntityService<T, ID extends Serializable> extends LoggingComponent {
    @Autowired
    private ITypeValidator<T> validator;

    /**
     * Saves the entity and performs validation checks with {@link ITypeValidator<T>}.
     * Throws {@link ValidationException}, if {@link DataIntegrityViolationException} is thrown while performing save
     * action.
     *
     * @param entity
     * @return
     */
    protected T save(T entity) {
        try {
            validator.validate(entity);
            return getRepository().save(entity);
        } catch (DataIntegrityViolationException e) {
            FieldValidationError error = new FieldValidationError();
            String entityName = getEntityName();
            String message = entityName + ".validation.error";

            if (e.getCause() instanceof ConstraintViolationException) {
                String constraintName = ((ConstraintViolationException) e.getCause()).getConstraintName();
                error.setField(constraintName);
                error.setMessage(entityName + "." + constraintName + ".violation");
            } else {
                error.setField("UNKNOWN");
                error.setMessage(entityName + ".integrity.violation");
            }
            throw new ValidationException(
                    message,
                    new TypeValidationError(
                            entity.getClass(),
                            message,
                            Collections.singletonList(error)
                    )
            );
        }
    }

    /**
     * Deletes the entity with the id in the parameter.
     * Performs a check, if it exists.
     * Throws {@link NotFoundException}, if the referenced entity doesn't exist.
     *
     * @param id
     */
    public void delete(ID id) {
        // Check if it exists
        get(id);
        getRepository().delete(id);
    }

    /**
     * Tries to get the entity from the repository, if fails, throws {@link NotFoundException}
     *
     * @param id
     * @return
     */
    public T get(ID id) {
        T entity = getRepository().findOne(id);
        if (entity == null) {
            throw new NotFoundException(getEntityName() + ".not_found");
        }
        return entity;
    }

    /**
     * Constructs a Specification query using {@link SpecificationsUtils#withQuery(Condition)}, if the repository is
     * capable.
     * Throws {@link UnsupportedOperationException}, if the repository doesn't implement
     * {@link JpaSpecificationExecutor}.
     *
     * @param condition
     * @param pageable
     * @return
     */
    public Page<T> list(Condition condition, Pageable pageable) {
        CrudRepository<T, ID> repository = getRepository();
        if (repository instanceof JpaSpecificationExecutor) {
            return ((JpaSpecificationExecutor<T>) repository).findAll(
                    SpecificationsUtils.withQuery(condition),
                    pageable
            );
        }
        throw new UnsupportedOperationException("Repository doesn't implement JpaSpecificationExecutor.");
    }

    protected abstract CrudRepository<T, ID> getRepository();

    /**
     * Returns the simple entity name.
     * Used to construct messages.
     *
     * @return simple entity name as it would be in the messages.properties files. (identity, role, etc.)
     */
    protected abstract String getEntityName();
}

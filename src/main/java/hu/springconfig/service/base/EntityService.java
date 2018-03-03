package hu.springconfig.service.base;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.Collections;

// TODO somehow it would be great to have these messages also as static strings
public abstract class EntityService<T, ID extends Serializable> extends LoggingComponent {
    @Autowired
    private ITypeValidator<T> validator;

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
                            Identity.class,
                            message,
                            Collections.singletonList(error)
                    )
            );
        }
    }

    public T get(ID id) {
        T entity = getRepository().findOne(id);
        if (entity == null) {
            throw new NotFoundException(getEntityName() + ".not_found");
        }
        return entity;
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

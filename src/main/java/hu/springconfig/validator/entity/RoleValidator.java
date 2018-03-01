package hu.springconfig.validator.entity;

import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.util.Util;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.beans.factory.annotation.Value;

public class RoleValidator implements ITypeValidator<Role> {
    @Value("${role.name.length.min}")
    private Integer nameMin;
    @Value("${role.name.length.max}")
    private Integer nameMax;
    @Value("${role.name.charset}")
    private String nameCharset;

    @Override
    public void validate(Role role) throws ValidationException {
        TypeValidationError error = createTypeValidationError();
        error.addErrorIfNotNull(validateId(role.getId()));
        error.addErrorIfNotNull(validateRole(role.getRole()));
        checkResult(error);
    }

    private FieldValidationError validateRole(String role) {
        if (!Util.validateString(role, nameMin, nameMax, nameCharset)) {
            return new FieldValidationError("role", "role.name.invalid");
        }
        return null;
    }

    private FieldValidationError validateId(Integer id) {
        if (id == null) {
            return new FieldValidationError("id", "role.id.null");
        }
        if (RoleService.USER_ROLE_ID >= id && id >= RoleService.ADMIN_ROLE_ID) {
            return new FieldValidationError("id", "role.id.range");
        }
        return null;
    }


    @Override
    public Class<Role> getType() {
        return Role.class;
    }
}

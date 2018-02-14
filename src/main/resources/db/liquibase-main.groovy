databaseChangeLog {
    changeSet(id: '1518449663155-2', author: 'balazs_horvath (generated)') {
        createTable(tableName: 'identity') {
            column(name: 'id', type: 'BIGSERIAL', autoIncrement: true) {
                constraints(primaryKey: true, primaryKeyName: 'identity_pkey')
            }
            column(name: 'email', type: 'VARCHAR(255)')
            column(name: 'password', type: 'VARCHAR(255)')
            column(name: 'username', type: 'VARCHAR(255)')
            column(name: 'version', type: 'BIGINT') {
                constraints(nullable: false)
            }
        }
    }

    changeSet(id: '1518449663155-3', author: 'balazs_horvath (generated)') {
        createTable(tableName: 'identity_roles') {
            column(name: 'identity_id', type: 'BIGINT') {
                constraints(nullable: false)
            }
            column(name: 'role_id', type: 'INT') {
                constraints(nullable: false)
            }
        }
    }

    changeSet(id: '1518449663155-4', author: 'balazs_horvath (generated)') {
        createTable(tableName: 'privilege') {
            column(name: 'id', type: 'INT') {
                constraints(nullable: false)
            }
            column(name: 'privilege', type: 'VARCHAR(255)')
        }
    }

    changeSet(id: '1518449663155-5', author: 'balazs_horvath (generated)') {
        createTable(tableName: 'role') {
            column(name: 'id', type: 'INT') {
                constraints(nullable: false)
            }
            column(name: 'role', type: 'VARCHAR(255)')
        }
    }

    changeSet(id: '1518449663155-6', author: 'balazs_horvath (generated)') {
        createTable(tableName: 'role_privileges') {
            column(name: 'role_id', type: 'INT') {
                constraints(nullable: false)
            }
            column(name: 'privilege_id', type: 'INT') {
                constraints(nullable: false)
            }
        }
    }

    changeSet(id: '1518449663155-7', author: 'balazs_horvath (generated)') {
        addPrimaryKey(columnNames: 'identity_id, role_id', constraintName: 'identity_roles_pkey', tableName: 'identity_roles')
    }

    changeSet(id: '1518449663155-8', author: 'balazs_horvath (generated)') {
        addPrimaryKey(columnNames: 'id', constraintName: 'privilege_pkey', tableName: 'privilege')
    }

    changeSet(id: '1518449663155-9', author: 'balazs_horvath (generated)') {
        addPrimaryKey(columnNames: 'id', constraintName: 'role_pkey', tableName: 'role')
    }

    changeSet(id: '1518449663155-10', author: 'balazs_horvath (generated)') {
        addPrimaryKey(columnNames: 'role_id, privilege_id', constraintName: 'role_privileges_pkey', tableName: 'role_privileges')
    }

    changeSet(id: '1518449663155-11', author: 'balazs_horvath (generated)') {
        addUniqueConstraint(columnNames: 'privilege', constraintName: 'privilege_unique', tableName: 'privilege')
    }

    changeSet(id: '1518449663155-12', author: 'balazs_horvath (generated)') {
        addUniqueConstraint(columnNames: 'role', constraintName: 'role_unique', tableName: 'role')
    }

    changeSet(id: '1518449663155-13', author: 'balazs_horvath (generated)') {
        addUniqueConstraint(columnNames: 'username', constraintName: 'username_unique', tableName: 'identity')
    }

    changeSet(id: '1518449663155-14', author: 'balazs_horvath (generated)') {
        addUniqueConstraint(columnNames: 'email', constraintName: 'email_unique', tableName: 'identity')
    }

    changeSet(id: '1518449663155-15', author: 'balazs_horvath (generated)') {
        addForeignKeyConstraint(baseColumnNames: 'privilege_id', baseTableName: 'role_privileges', constraintName: 'privilege_role_privileges_fk', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'privilege')
    }

    changeSet(id: '1518449663155-16', author: 'balazs_horvath (generated)') {
        addForeignKeyConstraint(baseColumnNames: 'identity_id', baseTableName: 'identity_roles', constraintName: 'identity_identity_roles_fk', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'identity')
    }

    changeSet(id: '1518449663155-17', author: 'balazs_horvath (generated)') {
        addForeignKeyConstraint(baseColumnNames: 'role_id', baseTableName: 'role_privileges', constraintName: 'role_role_privileges_fk', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'role')
    }

    changeSet(id: '1518449663155-18', author: 'balazs_horvath (generated)') {
        addForeignKeyConstraint(baseColumnNames: 'role_id', baseTableName: 'identity_roles', constraintName: 'role_identity_roles_fk', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'role')
    }

    changeSet(id: 'default-roles', author: 'balazs_horvath') {
        loadData(tableName: 'role', file: 'db/defaults/roles.csv', encoding: 'UTF8') {
            column(name: 'id', header: 'id', type: 'NUMERIC')
            column(name: 'role', header: 'role', type: 'STRING')
        }
    }
    changeSet(id: 'default-privileges', author: 'balazs_horvath') {
        loadData(tableName: 'privilege', file: 'db/defaults/privileges.csv', encoding: 'UTF8') {
            column(name: 'id', header: 'id', type: 'NUMERIC')
            column(name: 'privilege', header: 'privilege', type: 'STRING')
        }
    }
    changeSet(id: 'default-role_privileges', author: 'balazs_horvath') {
        loadData(tableName: 'role_privileges', file: 'db/defaults/role_privileges.csv', encoding: 'UTF8') {
            column(name: 'role_id', header: 'role_id', type: 'NUMERIC')
            column(name: 'privilege_id', header: 'privilege_id', type: 'NUMERIC')
        }
    }

    changeSet(id: 'identity-token-expiration', author: 'balazs_horvath'){
        addColumn(tableName: 'identity'){
            column(name: 'token_expiration', type: 'DATETIME')
        }
    }
}

databaseChangeLog {
  changeSet(id: '1518448348442-1', author: 'balazs_horvath (generated)') {
    createSequence(sequenceName: 'identity_id_seq')
  }

  changeSet(id: '1518448348442-2', author: 'balazs_horvath (generated)') {
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

  changeSet(id: '1518448348442-3', author: 'balazs_horvath (generated)') {
    createTable(tableName: 'identity_roles') {
      column(name: 'identity_id', type: 'BIGINT') {
        constraints(nullable: false)
      }
      column(name: 'role_id', type: 'INT') {
        constraints(nullable: false)
      }
    }
  }

  changeSet(id: '1518448348442-4', author: 'balazs_horvath (generated)') {
    createTable(tableName: 'privilege') {
      column(name: 'id', type: 'INT') {
        constraints(nullable: false)
      }
      column(name: 'privilege', type: 'VARCHAR(255)')
    }
  }

  changeSet(id: '1518448348442-5', author: 'balazs_horvath (generated)') {
    createTable(tableName: 'role') {
      column(name: 'id', type: 'INT') {
        constraints(nullable: false)
      }
      column(name: 'role', type: 'VARCHAR(255)')
    }
  }

  changeSet(id: '1518448348442-6', author: 'balazs_horvath (generated)') {
    createTable(tableName: 'role_privileges') {
      column(name: 'role_id', type: 'INT') {
        constraints(nullable: false)
      }
      column(name: 'privilege_id', type: 'INT') {
        constraints(nullable: false)
      }
    }
  }

  changeSet(id: '1518448348442-7', author: 'balazs_horvath (generated)') {
    addPrimaryKey(columnNames: 'identity_id, role_id', constraintName: 'identity_roles_pkey', tableName: 'identity_roles')
  }

  changeSet(id: '1518448348442-8', author: 'balazs_horvath (generated)') {
    addPrimaryKey(columnNames: 'id', constraintName: 'privilege_pkey', tableName: 'privilege')
  }

  changeSet(id: '1518448348442-9', author: 'balazs_horvath (generated)') {
    addPrimaryKey(columnNames: 'id', constraintName: 'role_pkey', tableName: 'role')
  }

  changeSet(id: '1518448348442-10', author: 'balazs_horvath (generated)') {
    addPrimaryKey(columnNames: 'role_id, privilege_id', constraintName: 'role_privileges_pkey', tableName: 'role_privileges')
  }

  changeSet(id: '1518448348442-11', author: 'balazs_horvath (generated)') {
    addUniqueConstraint(columnNames: 'privilege', constraintName: 'privilegeunique', tableName: 'privilege')
  }

  changeSet(id: '1518448348442-12', author: 'balazs_horvath (generated)') {
    addUniqueConstraint(columnNames: 'role', constraintName: 'roleunique', tableName: 'role')
  }

  changeSet(id: '1518448348442-13', author: 'balazs_horvath (generated)') {
    addUniqueConstraint(columnNames: 'username', constraintName: 'uk_86awcomb24n76iy2imns9aux4', tableName: 'identity')
  }

  changeSet(id: '1518448348442-14', author: 'balazs_horvath (generated)') {
    addUniqueConstraint(columnNames: 'email', constraintName: 'uk_r0q6gmtax44u1qbbanjo8soam', tableName: 'identity')
  }

  changeSet(id: '1518448348442-15', author: 'balazs_horvath (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'privilege_id', baseTableName: 'role_privileges', constraintName: 'fk9bh6h5cm4bq0u3q9pcotkydq8', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'privilege')
  }

  changeSet(id: '1518448348442-16', author: 'balazs_horvath (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'identity_id', baseTableName: 'identity_roles', constraintName: 'fkcw5d7mi88sekt7hrtt3821dom', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'identity')
  }

  changeSet(id: '1518448348442-17', author: 'balazs_horvath (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'role_id', baseTableName: 'role_privileges', constraintName: 'fkgelpp2j5e63axp7bcguwaqec5', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'role')
  }

  changeSet(id: '1518448348442-18', author: 'balazs_horvath (generated)') {
    addForeignKeyConstraint(baseColumnNames: 'role_id', baseTableName: 'identity_roles', constraintName: 'fkj7bflgci6bgvoj12yvsobvl73', deferrable: false, initiallyDeferred: false, onDelete: 'NO ACTION', onUpdate: 'NO ACTION', referencedColumnNames: 'id', referencedTableName: 'role')
  }

}

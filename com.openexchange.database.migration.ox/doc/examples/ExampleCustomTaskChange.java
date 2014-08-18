package com.openexchange.database.migration.custom;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;


public class ExampleCustomTaskChange implements CustomTaskChange {

    @Override
    public String getConfirmationMessage() {
        String s = this.getClass().getSimpleName() + ".getConfirmationMessage";
        System.out.println(s);
        return s;
    }

    @Override
    public void setUp() {
        System.out.println(this.getClass().getSimpleName() + ".setUp");
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        System.out.println(this.getClass().getSimpleName() + ".setFileOpener");
    }

    @Override
    public ValidationErrors validate(Database database) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Executed when given in the xml file {@inheritDoc}
     */
    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            database.checkDatabaseChangeLogLockTable();
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(this.getClass().getSimpleName() + ".execute");
    }

}

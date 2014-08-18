package com.openexchange.database.migration.ox.tasks;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;


public class MyTestClass implements CustomTaskChange {

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

    @Override
    public void execute(Database database) throws CustomChangeException {
        System.out.println(this.getClass().getSimpleName() + ".execute");
    }

}

package com.openexchange.rest.services.database.osgi;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.rest.services.database.DBRESTService;
import com.openexchange.rest.services.database.migrations.DBVersionChecker;
import com.openexchange.rest.services.database.migrations.VersionChecker;
import com.openexchange.rest.services.database.sql.CreateServiceSchemaLockTable;
import com.openexchange.rest.services.database.sql.CreateServiceSchemaLockTableTask;
import com.openexchange.rest.services.database.sql.CreateServiceSchemaVersionTable;
import com.openexchange.rest.services.database.sql.CreateServiceSchemaVersionTableTask;
import com.openexchange.rest.services.database.transactions.InMemoryTransactionKeeper;
import com.openexchange.rest.services.osgiservice.OXRESTActivator;
import com.openexchange.timer.TimerService;

public class DatabaseRESTActivator extends OXRESTActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]{DatabaseService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final InMemoryTransactionKeeper transactions = new InMemoryTransactionKeeper();
        VersionChecker versions = new DBVersionChecker();
        
        registerWebService(DBRESTService.class, new DBRESTService.Environment(transactions, versions));
        
        // Transaction Cleanup
        
        getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                transactions.tick(System.currentTimeMillis());
            }
            
        }, 2, 1, TimeUnit.MINUTES);
        // DB Stuff
        
        registerService(CreateTableService.class, new CreateServiceSchemaVersionTable());
        registerService(CreateTableService.class, new CreateServiceSchemaLockTable());

        registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<? extends UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(new CreateServiceSchemaVersionTableTask(getService(DatabaseService.class)), new CreateServiceSchemaLockTableTask(getService(DatabaseService.class)));
            }
        });

    }


}



package com.openexchange.admin.tools;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;

public class DatabaseDataMover implements Callable<Void> {

    private final static Log log = LogFactory.getLog(DatabaseDataMover.class);
    
    private Context ctx = null;

    private Database db = null;

    private MaintenanceReason reason_id = null;

    /**
     * 
     */
    public DatabaseDataMover(final Context ctx, final Database db, final MaintenanceReason reason) {
        this.ctx = ctx;
        this.db = db;
        this.reason_id = reason;
    }

    public Void call() throws StorageException {
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.moveDatabaseContext(ctx, db, reason_id);
        } catch (final StorageException e) {
            log.error(e);
            throw e;
        } catch (final RuntimeException e) {
            log.error(e);
            throw e;
        }
        return null;
    }

}

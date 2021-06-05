/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */


package com.openexchange.admin.plugin.hosting.tools;

import java.util.concurrent.Callable;
import com.openexchange.admin.plugin.hosting.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class DatabaseDataMover implements Callable<Void> {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseDataMover.class);

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

    @Override
    public Void call() throws StorageException {
        try {
            final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
            oxcox.moveDatabaseContext(ctx, db, reason_id);
        } catch (StorageException e) {
            log.error("", e);
            // Because the client side only knows of the exceptions defined in the core we have
            // to throw the trace as string
            throw e;
        } catch (RuntimeException e) {
            log.error("", e);
            throw StorageException.storageExceptionFor(e);
        }
        return null;
    }

}

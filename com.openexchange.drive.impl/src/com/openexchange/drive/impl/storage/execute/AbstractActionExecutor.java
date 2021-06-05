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

package com.openexchange.drive.impl.storage.execute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.storage.StorageOperation;
import com.openexchange.drive.impl.sync.DefaultSyncResult;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractActionExecutor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractActionExecutor<T extends DriveVersion> implements ActionExecutor<T> {

    protected final SyncSession session;
    protected final boolean transactional;

    protected List<AbstractAction<T>> newActionsForClient;

    /**
     * Initializes a new {@link AbstractActionExecutor}.
     *
     * @param session The session
     * @param transactional <code>true</code> to operate in a transactional mode, <code>false</code>, otherwise
     */
    protected AbstractActionExecutor(SyncSession session, boolean transactional) {
        super();
        this.session = session;
        this.transactional = transactional;
    }

    /**
     * Adds a new action the client should execute as a result of the executed server actions.
     *
     * @param action The action to add.
     */
    protected void addNewActionForClient(AbstractAction<T> action) {
        if (null == newActionsForClient) {
            newActionsForClient = new ArrayList<AbstractAction<T>>();
        }
        newActionsForClient.add(action);
    }

    /**
     * Adds multiple new actions the client should execute as a result of the executed server actions.
     *
     * @param actions The actions to add.
     */
    protected void addNewActionsForClient(Collection<? extends AbstractAction<T>> actions) {
        if (null == newActionsForClient) {
            newActionsForClient = new ArrayList<AbstractAction<T>>();
        }
        newActionsForClient.addAll(actions);
    }

    @Override
    public List<AbstractAction<T>> execute(IntermediateSyncResult<T> syncResult) throws OXException {
        List<AbstractAction<T>> actionsForServer = syncResult.getActionsForServer();
        if (null == actionsForServer || 0 == actionsForServer.size()) {
            return syncResult.getActionsForClient();
        }
        /*
         * execute each server action & return resulting client actions
         */
        for (final AbstractAction<T> action : actionsForServer) {
            if (transactional) {
                session.getStorage().wrapInTransaction(new StorageOperation<Void>() {

                    @Override
                    public Void call() throws OXException {
                        execute(action);
                        return null;
                    }
                });
            } else {
                execute(action);
            }
        }
        return getActionsForClient(syncResult);
    }

    @SuppressWarnings("unused")
    protected List<AbstractAction<T>> getActionsForClient(IntermediateSyncResult<T> syncResult) throws OXException {
        /*
         * return new actions for client if set
         */
        if (null != newActionsForClient) {
            if (session.isTraceEnabled()) {
                session.trace("Execution of server actions resulted in new actions for client. New actions for client:");
                session.trace(new DefaultSyncResult<T>(newActionsForClient, ""));
            }
            return newActionsForClient;
        }
        /*
         * stick to actions from sync result otherwise
         */
        return syncResult.getActionsForClient();
    }

    protected abstract void execute(AbstractAction<T> action) throws OXException;

}

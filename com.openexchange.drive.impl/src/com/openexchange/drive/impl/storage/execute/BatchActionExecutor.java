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

import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.storage.StorageOperation;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;


/**
 * {@link BatchActionExecutor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class BatchActionExecutor<T extends DriveVersion> extends AbstractActionExecutor<T> {

    protected final boolean allowBatches;

    /**
     * Initializes a new {@link BatchActionExecutor}.
     *
     * @param session The session
     */
    protected BatchActionExecutor(SyncSession session, boolean transactional, boolean allowBatches) {
        super(session, transactional);
        this.allowBatches = allowBatches;
    }

    @Override
    public List<AbstractAction<T>> execute(IntermediateSyncResult<T> syncResult) throws OXException {
        if (false == allowBatches) {
            return super.execute(syncResult); // execute each action separately
        }
        List<AbstractAction<T>> actionsForServer = syncResult.getActionsForServer();
        if (null == actionsForServer || 0 == actionsForServer.size()) {
            return syncResult.getActionsForClient();
        }
        /*
         * execute server actions in batches
         */
        int currentBatchStart = 0;
        Action currentAction = actionsForServer.get(0).getAction();
        for (int i = 1; i < actionsForServer.size(); i++) {
            if (false == currentAction.equals(actionsForServer.get(i).getAction())) {
                /*
                 * execute batch
                 */
                execute(currentAction, actionsForServer.subList(currentBatchStart, i));
                /*
                 * prepare next batch
                 */
                currentBatchStart = i;
                currentAction = actionsForServer.get(i).getAction();
            }
        }
        /*
         * execute remaining batch & return resulting client actions
         */
        execute(currentAction, actionsForServer.subList(currentBatchStart, actionsForServer.size()));
        return getActionsForClient(syncResult);
    }

    private void execute(final Action action, final List<AbstractAction<T>> actions) throws OXException {
        if (transactional) {
            session.getStorage().wrapInTransaction(new StorageOperation<Void>() {

                @Override
                public Void call() throws OXException {
                    if (1 == actions.size()) {
                        execute(actions.get(0));
                    } else {
                        batchExecute(action, actions);
                    }
                    return null;
                }
            });
        } else {
            if (1 == actions.size()) {
                execute(actions.get(0));
            } else {
                batchExecute(action, actions);
            }
        }
    }

    protected abstract void batchExecute(Action action, List<AbstractAction<T>> actions) throws OXException;

}

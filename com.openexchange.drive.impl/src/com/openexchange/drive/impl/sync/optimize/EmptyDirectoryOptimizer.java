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

package com.openexchange.drive.impl.sync.optimize;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.AcknowledgeDirectoryAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link EmptyDirectoryOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EmptyDirectoryOptimizer extends DirectoryActionOptimizer {

    public EmptyDirectoryOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> optimize(SyncSession session, IntermediateSyncResult<DirectoryVersion> result) {
        if (session.getDriveSession().useDriveMeta()) {
            /*
             * no optimization when dealing with .drive-meta files
             */
            return result;
        }
        List<AbstractAction<DirectoryVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<DirectoryVersion>>(result.getActionsForClient());
        List<AbstractAction<DirectoryVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<DirectoryVersion>>(result.getActionsForServer());
        /*
         * check for client SYNC of new directories
         */
        for (AbstractAction<DirectoryVersion> clientAction : result.getActionsForClient()) {
            if (Action.SYNC.equals(clientAction.getAction()) && clientAction.wasCausedBy(Change.NEW, Change.NONE) &&
                null != clientAction.getVersion() && DriveConstants.EMPTY_MD5.equals(clientAction.getVersion().getChecksum())) {
                optimizedActionsForClient.remove(clientAction);
                optimizedActionsForClient.add(new AcknowledgeDirectoryAction(null, clientAction.getVersion(), null));
            }
        }
        /*
         * return new sync results
         */
        return new IntermediateSyncResult<DirectoryVersion>(optimizedActionsForServer, optimizedActionsForClient);

    }

}

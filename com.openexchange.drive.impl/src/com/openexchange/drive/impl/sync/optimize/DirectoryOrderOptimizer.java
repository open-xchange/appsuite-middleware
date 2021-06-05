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

import java.util.Collections;
import java.util.List;
import com.openexchange.drive.Action;
import com.openexchange.drive.DefaultDirectoryVersion;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.EditDirectoryAction;
import com.openexchange.drive.impl.actions.RemoveDirectoryAction;
import com.openexchange.drive.impl.actions.SyncDirectoryAction;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link DirectoryOrderOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryOrderOptimizer extends DirectoryActionOptimizer {

    public DirectoryOrderOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> optimize(SyncSession session, IntermediateSyncResult<DirectoryVersion> result) {
        List<AbstractAction<DirectoryVersion>> actionsForClient = result.getActionsForClient();
        List<AbstractAction<DirectoryVersion>> actionsForServer = result.getActionsForServer();
        Collections.sort(actionsForClient);
        Collections.sort(actionsForServer);
        actionsForClient = propagateRenames(actionsForClient);
        actionsForServer = propagateRenames(actionsForServer);
        return new IntermediateSyncResult<DirectoryVersion>(actionsForServer, actionsForClient);
    }

    private static List<AbstractAction<DirectoryVersion>> propagateRenames(List<AbstractAction<DirectoryVersion>> actions) {
        /*
         * propagate previous rename operations
         */
        for (int i = 0; i < actions.size(); i++) {
            if (Action.EDIT.equals(actions.get(i).getAction()) && null != actions.get(i).getVersion() && null != actions.get(i).getNewVersion()) {
                String oldPath = actions.get(i).getVersion().getPath();
                String newPath = actions.get(i).getNewVersion().getPath();
                for (int j = i + 1; j < actions.size(); j++) {
                    if (Action.EDIT.equals(actions.get(j).getAction()) &&
                        actions.get(j).getVersion().getPath().startsWith(oldPath + DriveConstants.PATH_SEPARATOR)) {
                        String newOldPath = newPath + actions.get(j).getVersion().getPath().substring(oldPath.length());
                        DirectoryVersion modifiedOldVersion = new DefaultDirectoryVersion(newOldPath, actions.get(j).getVersion().getChecksum());
                        actions.set(j, new EditDirectoryAction(modifiedOldVersion, actions.get(j).getNewVersion(), actions.get(j).getComparison()));
                    } else if (Action.REMOVE.equals(actions.get(j).getAction()) &&
                        actions.get(j).getVersion().getPath().startsWith(oldPath + DriveConstants.PATH_SEPARATOR)) {
                        String newOldPath = newPath + actions.get(j).getVersion().getPath().substring(oldPath.length());
                        DirectoryVersion modifiedOldVersion = new DefaultDirectoryVersion(newOldPath, actions.get(j).getVersion().getChecksum());
                        actions.set(j, new RemoveDirectoryAction(modifiedOldVersion, actions.get(j).getComparison()));
                    } else if (Action.SYNC.equals(actions.get(j).getAction()) &&
                        actions.get(j).getVersion().getPath().startsWith(oldPath + DriveConstants.PATH_SEPARATOR)) {
                        String newOldPath = newPath + actions.get(j).getVersion().getPath().substring(oldPath.length());
                        DirectoryVersion modifiedOldVersion = new DefaultDirectoryVersion(newOldPath, actions.get(j).getVersion().getChecksum());
                        actions.set(j, new SyncDirectoryAction(modifiedOldVersion, actions.get(j).getComparison()));
                    }
                }
            }
        }
        return actions;
    }

}

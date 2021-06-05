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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link DirectoryRemoveOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryRemoveOptimizer extends DirectoryActionOptimizer {

    public DirectoryRemoveOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<DirectoryVersion> optimize(SyncSession session, IntermediateSyncResult<DirectoryVersion> result) {
        List<AbstractAction<DirectoryVersion>> clientActions = result.getActionsForClient();
        clientActions.removeAll(getRedundantRemoves(clientActions));
        List<AbstractAction<DirectoryVersion>> serverActions = result.getActionsForServer();
        serverActions.removeAll(getRedundantRemoves(serverActions));
        return new IntermediateSyncResult<DirectoryVersion>(serverActions, clientActions);
    }

    private static List<AbstractAction<DirectoryVersion>> getRedundantRemoves(List<AbstractAction<DirectoryVersion>> driveActions) {
        /*
         * get non-conflicting removes
         */
        List<AbstractAction<DirectoryVersion>> removeActions = getNonConflictingRemoves(driveActions);
        /*
         * sort & reverse order so that parent paths are before their children
         */
        Collections.sort(removeActions);
        Collections.reverse(removeActions);
        /*
         * find those removes where the parent directory is already removed
         */
        List<AbstractAction<DirectoryVersion>> redundantRemoves = new ArrayList<AbstractAction<DirectoryVersion>>();
        for (int i = 0; i < removeActions.size(); i++) {
            String prefix = removeActions.get(i).getVersion().getPath() + '/';
            for (int j = i + 1; j < removeActions.size(); j++) {
                if (removeActions.get(j).getVersion().getPath().startsWith(prefix)) {
                    redundantRemoves.add(removeActions.get(j));
                    List<AbstractAction<DirectoryVersion>> nestedRemoves;
                    Map<String, Object> parameters = removeActions.get(i).getParameters();
                    if (parameters.containsKey("nestedRemoves")) {
                        nestedRemoves = (List<AbstractAction<DirectoryVersion>>)removeActions.get(i).getParameters().get("nestedRemoves");
                    } else {
                        nestedRemoves = new ArrayList<AbstractAction<DirectoryVersion>>();
                        parameters.put("nestedRemoves", nestedRemoves);
                    }
                    nestedRemoves.add(removeActions.get(j));
                }
            }
        }
        /*
         * those are redundant
         */
        return redundantRemoves;
    }

    private static List<AbstractAction<DirectoryVersion>> getNonConflictingRemoves(List<AbstractAction<DirectoryVersion>> driveActions) {
        List<AbstractAction<DirectoryVersion>> removeActions = new ArrayList<AbstractAction<DirectoryVersion>>();
        for (AbstractAction<DirectoryVersion> driveAction : driveActions) {
            if (Action.REMOVE.equals(driveAction.getAction()) && (driveAction.wasCausedBy(Change.DELETED, Change.NONE) ||
                driveAction.wasCausedBy(Change.NONE, Change.DELETED) || driveAction.wasCausedBy(Change.DELETED, Change.DELETED))) {
                removeActions.add(driveAction);
            }
        }
        return removeActions;
    }

}

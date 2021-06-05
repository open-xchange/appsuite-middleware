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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.Action;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link FileMultipleUploadsOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileMultipleUploadsOptimizer extends FileActionOptimizer {

    public FileMultipleUploadsOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        /*
         * filter out all non-conflicting, duplicate UPLOAD actions so that those can be ACKNOWLEDGEd directly during next
         * synchronization cycle
         */
        List<AbstractAction<FileVersion>> optimizedActionsForClients =
            new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient().size());
        Set<String> uploadedVersionChecksums = new HashSet<String>();
        for (AbstractAction<FileVersion> action : result.getActionsForClient()) {
            if (Action.UPLOAD.equals(action.getAction())) {
                boolean alreadyKnown = false == uploadedVersionChecksums.add(action.getNewVersion().getChecksum());
                if (action.wasCausedBy(Change.NEW, Change.NONE) && alreadyKnown) {
                    continue;
                }
            }
            optimizedActionsForClients.add(action);
        }
        return new IntermediateSyncResult<FileVersion>(result.getActionsForServer(), optimizedActionsForClients);
    }

}

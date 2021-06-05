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
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link FileDelayMetadataDownloadOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileDelayMetadataDownloadOptimizer extends FileActionOptimizer {

    public FileDelayMetadataDownloadOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        /*
         * filter out all DOWNLOAD actions for .drive-meta file in case there are outstanding client-side modifications of the directory
         * contents
         */
        AbstractAction<FileVersion> downloadMetadataAction = null;
        boolean pendingClientChanges = false;
        for (AbstractAction<FileVersion> action : result.getActionsForClient()) {
            if (Action.UPLOAD.equals(action.getAction())) {
                pendingClientChanges = true;
            } else if (Action.DOWNLOAD.equals(action.getAction()) &&
                DriveConstants.METADATA_FILENAME.equals(action.getNewVersion().getName())) {
                downloadMetadataAction = action;
            }
        }
        if (pendingClientChanges && null != downloadMetadataAction) {
            List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
            optimizedActionsForClient.remove(downloadMetadataAction);
            return new IntermediateSyncResult<FileVersion>(result.getActionsForServer(), optimizedActionsForClient);
        } else {
            return result;
        }
    }

}

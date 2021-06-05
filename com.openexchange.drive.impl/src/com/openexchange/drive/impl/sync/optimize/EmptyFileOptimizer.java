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
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.actions.AcknowledgeFileAction;
import com.openexchange.drive.impl.actions.DownloadFileAction;
import com.openexchange.drive.impl.comparison.Change;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;


/**
 * {@link EmptyFileOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EmptyFileOptimizer extends FileActionOptimizer {

    public EmptyFileOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        List<AbstractAction<FileVersion>> optimizedActionsForClient = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForClient());
        List<AbstractAction<FileVersion>> optimizedActionsForServer = new ArrayList<AbstractAction<FileVersion>>(result.getActionsForServer());
        /*
         * for client UPLOADs of new files, check for the empty checksum
         */
        for (AbstractAction<FileVersion> clientAction : result.getActionsForClient()) {
            if (Action.UPLOAD == clientAction.getAction() && clientAction.wasCausedBy(Change.NEW, Change.NONE) &&
                null == clientAction.getVersion() && null != clientAction.getNewVersion() &&
                DriveConstants.EMPTY_MD5.equals(clientAction.getNewVersion().getChecksum())) {
                /*
                 * no need to upload, just create file on server and let client update it's metadata
                 */
                String path = (String)clientAction.getParameters().get(DriveAction.PARAMETER_PATH);
                optimizedActionsForClient.remove(clientAction);
                DownloadFileAction serverDownload = new DownloadFileAction(session, null, clientAction.getNewVersion(), null, path, null);
                AcknowledgeFileAction clientAcknowledge = new AcknowledgeFileAction(session, null, clientAction.getNewVersion(), null, path, null);
                clientAcknowledge.setDependingAction(serverDownload);
                optimizedActionsForServer.add(serverDownload);
                optimizedActionsForClient.add(clientAcknowledge);
            }
        }
        /*
         * return new sync result
         */
        return new IntermediateSyncResult<FileVersion>(optimizedActionsForServer, optimizedActionsForClient);
    }

}

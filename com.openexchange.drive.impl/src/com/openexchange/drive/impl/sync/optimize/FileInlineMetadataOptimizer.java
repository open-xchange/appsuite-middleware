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

import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveMetaMode;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.actions.AbstractAction;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.metadata.DriveMetadata;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;


/**
 * {@link FileInlineMetadataOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class FileInlineMetadataOptimizer extends FileActionOptimizer {

    /**
     * Initializes a new {@link FileInlineMetadataOptimizer}.
     *
     * @param mapper The version mapper to use
     */
    public FileInlineMetadataOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    @Override
    public IntermediateSyncResult<FileVersion> optimize(SyncSession session, IntermediateSyncResult<FileVersion> result) {
        /*
         * supply data inline in DOWNLOAD actions for .drive-meta files in case requested
         */
        DriveMetaMode mode = session.getDriveSession().getDriveMetaMode();
        if (DriveMetaMode.INLINE.equals(mode)) {
            for (AbstractAction<FileVersion> action : result.getActionsForClient()) {
                if (Action.DOWNLOAD.equals(action.getAction()) && DriveConstants.METADATA_FILENAME.equals(action.getNewVersion().getName())) {
                    try {
                        String path = (String) action.getParameters().get(DriveAction.PARAMETER_PATH);
                        ServerFileVersion serverFileVersion = ServerFileVersion.valueOf(action.getNewVersion(), path, session);
                        if (DriveMetadata.class.isInstance(serverFileVersion.getFile())) {
                            DriveMetadata metadata = ((DriveMetadata) serverFileVersion.getFile());
                            action.getParameters().put(DriveAction.PARAMETER_DATA, metadata.getJSONData());
                        }
                    } catch (OXException e) {
                        LOG.warn("", e);
                    }
                }
            }
        }
        return result;
    }

}

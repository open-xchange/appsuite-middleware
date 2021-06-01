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

package com.openexchange.drive.impl.actions;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.file.storage.File;

/**
 * {@link DynamicMetadataFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DynamicMetadataFileAction extends AbstractFileAction {

    protected final SyncSession session;
    private File metadata;
    private AbstractFileAction dependingAction;

    /**
     * Initializes a new {@link DynamicMetadataFileAction}. The initial meta data is taken from the newFile parameter if possible.
     *
     * @param session The sync session
     * @param file The file
     * @param newFile the new file
     * @param comparison The comparison
     * @param path The path
     */
    public DynamicMetadataFileAction(SyncSession session, FileVersion file, ServerFileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path) {
        this(session, file, newFile, comparison, path, null != newFile ? newFile.getFile() : null);
    }

    /**
     * Initializes a new {@link DynamicMetadataFileAction}.
     *
     * @param session The sync session
     * @param file The file
     * @param newFile The new file
     * @param comparison The comparison
     * @param path The path
     * @param serverFile The server file to get the initial metadata from
     */
    public DynamicMetadataFileAction(SyncSession session, FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, File serverFile) {
        super(file, newFile, comparison);
        parameters.put(PARAMETER_PATH, path);
        this.session = session;
        this.metadata = serverFile;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> parameters = super.getParameters();
        /*
         * overwrite metadata with dependent action if available
         */
        if (null != this.dependingAction) {
            FileVersion fileVersion = dependingAction.getResultingVersion();
            if (null != fileVersion && ServerFileVersion.class.isInstance(fileVersion)) {
                this.metadata = ((ServerFileVersion)fileVersion).getFile();
            }
        }
        /*
         * inject current metadata to parameters
         */
        if (null != this.metadata) {
            parameters = new HashMap<String, Object>(parameters);
            applyMetadataParameters(metadata, session);
        }
        return parameters;
    }

    /**
     * Sets the depending action
     *
     * @param action The depending action to set
     */
    public void setDependingAction(AbstractFileAction action) {
        this.dependingAction = action;
    }

}


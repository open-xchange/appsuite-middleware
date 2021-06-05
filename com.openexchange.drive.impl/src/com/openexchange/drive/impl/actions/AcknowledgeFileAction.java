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

import com.openexchange.drive.Action;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ServerFileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.file.storage.File;

/**
 * {@link AcknowledgeFileAction}
 *
 * Acknowledges a change for the client, indicating that he should update his stored metadata. This currently happens in the following
 * situations:
 * <ul>
 * <li>if a file deletion reported by the client is executed on the server - the <code>file</code> parameter is set to the original file,
 *     while the <code>newFile</code> parameter is <code>null</code>.</li>
 * <li>if a file modification reported by the client is executed on the server - the <code>file</code> parameter is set to the original,
 *     and the <code>newFile</code> parameter to the modified file.</li>
 * <li>if a file creation reported by the client is executed on the server - the <code>file</code> parameter is set to <code>null</code>,
 *     and the <code>newFile</code> parameter is set to the new file.</li>
 * </ul>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AcknowledgeFileAction extends DynamicMetadataFileAction {

    public AcknowledgeFileAction(SyncSession session, FileVersion file, ServerFileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path) {
        this(session, file, newFile, comparison, path, null != newFile ? newFile.getFile() : null);
    }

    public AcknowledgeFileAction(SyncSession session, FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, File serverFile) {
        super(session, file, newFile, comparison, path, serverFile);
    }

    @Override
    public Action getAction() {
        return Action.ACKNOWLEDGE;
    }

}


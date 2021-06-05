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
import com.openexchange.drive.impl.comparison.ThreeWayComparison;

/**
 * {@link UploadFileAction}
 *
 * Instructs the client to upload a file to the server. The parameters found in this action should be used for the corresponding upload
 * request.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UploadFileAction extends AbstractFileAction {

    public UploadFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, long offset) {
        super(file, newFile, comparison);
        parameters.put(PARAMETER_OFFSET, Long.valueOf(offset));
        parameters.put(PARAMETER_PATH, path);
    }

    @Override
    public Action getAction() {
        return Action.UPLOAD;
    }

}


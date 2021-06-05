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
import com.openexchange.exception.OXException;

/**
 * {@link ErrorFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ErrorFileAction extends AbstractFileAction {

    public ErrorFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, OXException error, boolean quarantine) {
        this(file, newFile, comparison, path, error, quarantine, false);
    }

    public ErrorFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, OXException error, boolean quarantine, boolean stop) {
        super(file, newFile, comparison);
        parameters.put(PARAMETER_PATH, path);
        parameters.put(PARAMETER_ERROR, error);
        parameters.put(PARAMETER_QUARANTINE, Boolean.valueOf(quarantine));
        parameters.put(PARAMETER_STOP, Boolean.valueOf(stop));
    }

    @Override
    public Action getAction() {
        return Action.ERROR;
    }

}

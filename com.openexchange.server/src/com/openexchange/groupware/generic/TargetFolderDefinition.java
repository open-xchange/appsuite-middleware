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

package com.openexchange.groupware.generic;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link TargetFolderDefinition}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TargetFolderDefinition {

    protected String folderId;

    protected Context context;

    protected int userId;

    public TargetFolderDefinition() {

    }

    public TargetFolderDefinition(final String folderId, final int userId, final Context context) {
        this.folderId = folderId;
        this.userId = userId;
        this.context = context;
    }


    public String getFolderId() {
        return folderId;
    }

    public int getFolderIdAsInt() throws OXException {
        int retval = -1;
        try {
            retval = Integer.parseInt(folderId);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("folder", folderId);
        }
        return retval;
    }

    public void setFolderId(final String folderId) {
        this.folderId = folderId;
    }

    public void setFolderId(final int folderId) {
        setFolderId(Integer.toString(folderId));
    }

    public boolean containsFolderId() {
        return getFolderId() != null;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public boolean containsUserId() {
        return getUserId() > 0;
    }

}

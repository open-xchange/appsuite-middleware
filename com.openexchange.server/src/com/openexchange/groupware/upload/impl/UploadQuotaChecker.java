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

package com.openexchange.groupware.upload.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.session.Session;

/**
 * {@link UploadQuotaChecker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class UploadQuotaChecker {

    /**
     * Returns the maximum allowed size of a complete request.
     *
     * @return the maximum allowed size of a complete request. The default value of <code>-1</code> indicates, that there is no limit
     */
    public abstract long getQuotaMax();

    /**
     * Returns the maximum allowed size of a single uploaded file.
     *
     * @return the maximum allowed size of a single uploaded file. The default value of <code>-1</code> indicates, that there is no limit
     */
    public abstract long getFileQuotaMax();

    /**
     * Gets the upload quota checker appropriate for given module.
     *
     * @param module The module for which the upload starts
     * @param session The current session
     * @return The upload quota checker appropriate for given module
     * @throws OXException If the module is unknown
     */
    public static final UploadQuotaChecker getUploadQuotaChecker(final int module, final Session session, final Context ctx) throws OXException {
        if (module == FolderObject.MAIL) {
            return new MailUploadQuotaChecker(session, ctx);
        }
        throw UploadException.UploadCode.UNKNOWN_MODULE.create(null, Integer.valueOf(module));
    }

}

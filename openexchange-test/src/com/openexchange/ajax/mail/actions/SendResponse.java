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

package com.openexchange.ajax.mail.actions;

import org.slf4j.Logger;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;

/**
 * {@link SendResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class SendResponse extends AbstractAJAXResponse {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SendResponse.class);

    private String[] folderAndID;

    /**
     * @param response
     */
    public SendResponse(final Response response) {
        super(response);
    }

    /**
     * @return Folder and ID of sent mail which is located in default "Sent"
     *         folder
     */
    public String[] getFolderAndID() {
        if (null == folderAndID) {
            final String str;
            if (getData() == null || (str = getData().toString()).length() == 0 || "null".equalsIgnoreCase(str)) {
                return null;
            }
            try {
                final MailPath mp = new MailPath(str);
                return new String[] { mp.getFullnameArgument().getPreparedName(), String.valueOf(mp.getMailID()) };
            } catch (OXException e) {
                LOG.error("", e);
                return null;
            }

        }
        return folderAndID;
    }
}

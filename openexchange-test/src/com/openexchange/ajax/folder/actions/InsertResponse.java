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

package com.openexchange.ajax.folder.actions;

import java.util.Date;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.DataObject;

/**
 * {@link InsertResponse}
 *
 * @author
 */
public class InsertResponse extends CommonInsertResponse {

    private String mailFolderID;

    /**
     * @param response
     */
    public InsertResponse(final Response response) {
        super(response);
    }

    /**
     * Every new object gets a new identifier. With this method this identifier can be read.
     *
     * @return the new identifier of the new object.
     */
    public String getMailFolderID() {
        return mailFolderID;
    }

    /**
     * @param id the id to set
     */
    public void setMailFolderID(final String id) {
        this.mailFolderID = id;
    }

    /**
     * {@inheritDoc}
     * New folder API has a drawback: It does not fill the timestamp value of the response. Therefore a new Date() must be inserted there.
     */
    @Override
    public void fillObject(final DataObject obj) {
        if (!isMailFolder()) {
            obj.setObjectID(getId());
        }
        Date timestamp = getTimestamp();
        if (null == timestamp) {
            timestamp = new Date();
        }
        obj.setLastModified(timestamp);
        if (!obj.containsCreationDate()) {
            obj.setCreationDate(timestamp);
        }
    }

    private boolean isMailFolder() {
        return getMailFolderID() != null;
    }
}

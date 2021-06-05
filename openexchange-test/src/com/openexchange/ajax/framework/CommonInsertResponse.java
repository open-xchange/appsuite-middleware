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

package com.openexchange.ajax.framework;

import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.DataObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonInsertResponse extends AbstractAJAXResponse {

    private int id;

    /**
     * @param response
     */
    public CommonInsertResponse(final Response response) {
        super(response);
    }

    /**
     * Every new object gets a new identifier. With this method this identifier
     * can be read.
     * 
     * @return the new identifier of the new object.
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Puts the data of this insert response into the object. This are
     * especially the identifier and the modified time stamp.
     */
    public void fillObject(final DataObject obj) {
        obj.setObjectID(getId());
        obj.setLastModified(getTimestamp());
        if (!obj.containsCreationDate()) {
            obj.setCreationDate(obj.getLastModified());
        }
    }
}

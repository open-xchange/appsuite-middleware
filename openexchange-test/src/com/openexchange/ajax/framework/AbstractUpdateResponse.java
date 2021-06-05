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
 * {@link AbstractUpdateResponse}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class AbstractUpdateResponse extends AbstractAJAXResponse {

    public AbstractUpdateResponse(Response response) {
        super(response);
    }

    /**
     * Puts the data of this update response into the object. This are especially the modified time stamp.
     */
    public void fillObject(final DataObject obj) {
        obj.setLastModified(getTimestamp());
    }
}

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

import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractInsertParser;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - using InsertResponse instead of CommonInsertResponse
 */
class InsertParser extends AbstractInsertParser<InsertResponse> {

    public InsertParser(final boolean failOnError) {
        super(failOnError);
    }

    @Override
    protected InsertResponse instantiateResponse(final Response response) {
        return new InsertResponse(response);
    }

    @Override
    protected InsertResponse createResponse(final Response response) {
        final InsertResponse retval = instantiateResponse(response);
        if (JSONObject.NULL == retval.getData()) {
            fail("Problem while inserting folder: " + response.getErrorMessage());
        }
        try {
            final int folderId = Integer.parseInt((String) retval.getData());
            retval.setId(folderId);
            if (isFailOnError()) {
                assertTrue("Problem while inserting folder.", folderId > 0);
            }
        } catch (NumberFormatException e) {
            // if the value is not an integer, we're probably dealing with a mail folder here
            retval.setMailFolderID((String) retval.getData());
        }
        return retval;
    }
}

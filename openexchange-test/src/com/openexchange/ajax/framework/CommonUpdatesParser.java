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

import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class CommonUpdatesParser<T extends CommonUpdatesResponse> extends AbstractColumnsParser<T> {

    protected CommonUpdatesParser(final boolean failOnError, final int[] columns) {
        super(failOnError, columns);
    }

    /**
     * This method must be overwritten if some more detailed response class should be used instead of the common updates response class.
     *
     * @param response the general response object containing methods and data for handling the general JSON response object.
     * @return a detailed response object corresponding to the request and NEVER <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected T instantiateResponse(final Response response) {
        // I don't quite get this.
        return (T) new CommonUpdatesResponse(response);
    }

    @Override
    protected T createResponse(final Response response) throws JSONException {
        T updateResponse = super.createResponse(response);
        initUpdatedIds(updateResponse);
        return updateResponse;
    }

    /*
     * Deleted Objects are represented as String ids on the toplevel of the response array
     *
     * New or modified Objects are represented as array on the toplevel of the response array
     * [
     * 31279,
     * 35,
     * "UpdatedTask 4",
     * null,
     * null,
     * null,
     * null,
     * [
     * {
     * "type": 1,
     * "confirmation": 0,
     * "id": 5
     * }
     * ]
     * ]
     */
    protected void initUpdatedIds(T updatesResponse) {
        Object[][] responseData = updatesResponse.getArray();
        int idPosition = updatesResponse.getColumnPos(DataObject.OBJECT_ID);
        Set<Integer> newOrModifiedIds = new HashSet<Integer>(responseData.length);
        Set<Integer> deletedIds = new HashSet<Integer>(responseData.length);
        if (idPosition > -1) {
            for (Object[] objectArray : responseData) {
                if (objectArray.length == 1) {
                    deletedIds.add(getIdFromObject(objectArray[0]));
                } else {
                    newOrModifiedIds.add(getIdFromObject(objectArray[idPosition]));
                }
            }
        }
        updatesResponse.setNewOrModifiedIds(newOrModifiedIds);
        updatesResponse.setDeletedIds(deletedIds);
    }

    private Integer getIdFromObject(Object object) {
        Integer id = null;
        if (object instanceof String) {
            String s = (String) object;
            if (s.startsWith(FolderObject.SHARED_PREFIX)) {
                id = Integer.valueOf(s.substring(FolderObject.SHARED_PREFIX.length()));
            } else {
                id = Integer.valueOf((String) object);
            }
        } else {
            id = ((Integer) object);
        }
        return id == null ? new Integer(-1) : id;
    }

}

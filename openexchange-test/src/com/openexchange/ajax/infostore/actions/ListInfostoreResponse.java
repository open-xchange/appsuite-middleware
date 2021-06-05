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

package com.openexchange.ajax.infostore.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.json.FileMetadataFieldParser;

/**
 * {@link ListInfostoreResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ListInfostoreResponse extends AbstractColumnsResponse {

    private Object[][] convertedArray = null;

    /**
     * Initializes a new {@link ListInfostoreResponse}.
     * 
     * @param response
     */
    protected ListInfostoreResponse(Response response) {
        super(response);
    }

    @Override
    public Object[][] getArray() {
        if (convertedArray != null) {
            return convertedArray;
        }

        Object[][] array = super.getArray();
        if (array == null) {
            return null;
        }

        int[] columns = getColumns();
        convertedArray = new Object[array.length][];
        for (int i = 0; i < array.length; i++) {
            Object[] origObjects = array[i];
            Object[] convertedObjects = convertedArray[i] = new Object[origObjects.length];
            for (int j = 0; j < origObjects.length; j++) {
                Object orig = origObjects[j];
                Object converted = orig;
                Field field = File.Field.get(columns[j]);
                if (orig != null && field != null) {
                    try {
                        converted = FileMetadataFieldParser.convert(field, orig);
                    } catch (JSONException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    } catch (OXException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }

                convertedObjects[j] = converted;
            }
        }

        return convertedArray;
    }

}

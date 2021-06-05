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

import static com.openexchange.java.Autoboxing.I;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractColumnsParser<T extends AbstractColumnsResponse> extends AbstractAJAXParser<T> {

    private final int[] columns;

    protected AbstractColumnsParser(final boolean failOnError, final int[] columns) {
        super(failOnError);
        this.columns = columns;
    }

    @Override
    protected T createResponse(final Response response) throws JSONException {
        final T retval = instantiateResponse(response);
        retval.setColumns(columns);
        JSONArray array = (JSONArray) retval.getData();
        if (isFailOnError() || null != array) {
            retval.setArray(parseData(array));
        }
        return retval;
    }

    private static Object[][] parseData(final JSONArray array) throws JSONException {
        final Object[][] values = new Object[array.length()][];
        for (int i = 0; i < array.length(); i++) {
            try {
                // insert or update
                final JSONArray inner = array.getJSONArray(i);
                values[i] = new Object[inner.length()];
                for (int j = 0; j < inner.length(); j++) {
                    if (inner.isNull(j)) {
                        values[i][j] = null;
                    } else {
                        values[i][j] = inner.get(j);
                    }
                }
            } catch (JSONException e) {
                // delete
                values[i] = new Integer[] { I(array.getInt(i)) };
            }
        }
        return values;
    }

    protected abstract T instantiateResponse(final Response response);

    protected int[] getColumns() {
        return columns;
    }
}

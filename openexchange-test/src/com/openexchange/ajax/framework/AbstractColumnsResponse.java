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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import com.openexchange.ajax.container.Response;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractColumnsResponse extends AbstractAJAXResponse implements Iterable<Object[]> {

    private int[] columns;

    private Object[][] array;

    protected AbstractColumnsResponse(final Response response) {
        super(response);
    }

    public Object[][] getArray() {
        return array;
    }

    void setArray(final Object[][] array) {
        this.array = array;
    }

    @Override
    public Iterator<Object[]> iterator() {
        return Collections.unmodifiableList(Arrays.asList(array)).iterator();
    }

    public Object getValue(final int row, final int attributeId) {
        return array[row][getColumnPos(attributeId)];
    }

    public Iterator<Object> iterator(final int attributeId) {
        final int columnPos = getColumnPos(attributeId);
        return new Iterator<Object>() {

            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < getArray().length;
            }

            @Override
            public Object next() {
                return getArray()[pos++][columnPos];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Get the column position of a specific attribute
     * 
     * @param attributeId the attribute whose position you want to lookup
     * @return -1 if the attribute can't be found, the position of the attribute otherwise
     */
    public int getColumnPos(final int attributeId) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == attributeId) {
                return i;
            }
        }
        return -1;
    }

    public int[] getColumns() {
        return columns;
    }

    public void setColumns(final int[] columns) {
        this.columns = columns;
    }

    public int size() {
        return array.length;
    }
}

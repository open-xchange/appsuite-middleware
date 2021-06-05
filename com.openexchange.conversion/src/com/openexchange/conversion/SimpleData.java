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

package com.openexchange.conversion;

/**
 * {@link SimpleData} - A simple data
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SimpleData<D extends Object> implements Data<D> {

    private final D data;

    private final DataProperties dataProperties;

    /**
     * Initializes a new {@link SimpleData} with empty data properties
     *
     * @param data The data
     */
    public SimpleData(final D data) {
        this(data, DataProperties.EMPTY_PROPS);
    }

    /**
     * Initializes a new {@link SimpleData}
     *
     * @param data The data
     * @param dataProperties The data properties
     */
    public SimpleData(final D data, final DataProperties dataProperties) {
        super();
        this.data = data;
        this.dataProperties = dataProperties;
    }

    @Override
    public D getData() {
        return data;
    }

    @Override
    public DataProperties getDataProperties() {
        return dataProperties;
    }
}

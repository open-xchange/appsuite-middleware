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

package com.openexchange.messaging.json;

import java.util.Map;


/**
 * {@link SimEntry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimEntry<T1, T2> implements Map.Entry<T1, T2>{

    private T2 value;
    private final T1 key;

    public SimEntry(final T1 key, final T2 value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public T1 getKey() {
        return key;
    }

    @Override
    public T2 getValue() {
        return value;
    }

    @Override
    public T2 setValue(final T2 value) {
        final T2 old = this.value;
        this.value = value;
        return old;
    }

}

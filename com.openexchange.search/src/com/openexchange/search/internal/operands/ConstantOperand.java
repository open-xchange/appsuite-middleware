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

package com.openexchange.search.internal.operands;

import com.openexchange.search.Operand;

/**
 * {@link ConstantOperand} - The constant operand.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConstantOperand<V> implements Operand<V> {

    /**
     * The <code>null</code> operand.
     */
    public static final ConstantOperand<?> NULL = new ConstantOperand<Object>(null);

    private final V value;

    /**
     * Initializes a new {@link ConstantOperand}.
     */
    public ConstantOperand(final V value) {
        super();
        this.value = value;
    }

    @Override
    public com.openexchange.search.Operand.Type getType() {
        return Type.CONSTANT;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new StringBuilder(Type.CONSTANT.getType()).append(':').append(value).toString();
    }
}

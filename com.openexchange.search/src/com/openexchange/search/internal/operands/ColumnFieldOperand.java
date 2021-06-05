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
 * {@link ColumnFieldOperand}
 *
 * A column operand for a field.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ColumnFieldOperand<E extends Enum<?>> implements Operand<E> {

    private final E field;

    /**
     * Initializes a new {@link ColumnFieldOperand}.
     *
     * @param field The field
     */
    public ColumnFieldOperand(E field) {
        super();
        this.field = field;
    }

    @Override
    public com.openexchange.search.Operand.Type getType() {
        return Type.COLUMN;
    }

    @Override
    public E getValue() {
        return field;
    }

    @Override
    public String toString() {
        return Type.COLUMN.getType() + ':' + getValue();
    }

}

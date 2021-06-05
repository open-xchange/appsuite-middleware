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

package com.openexchange.groupware.update.tools.console.comparators;

import java.util.Date;

/**
 * {@link LastModifiedComparator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class LastModifiedComparator extends AbstractComparator {
    
    private static final long serialVersionUID = -7649815621234542779L;
    private static final int INDEX_POSITION = 2;

    /**
     * Initialises a new {@link LastModifiedComparator}.
     */
    public LastModifiedComparator() {
        super(Date.class, INDEX_POSITION);
    }

    @Override
    protected int innerCompare(Object o1, Object o2) {
        return ((Date) o1).compareTo((Date) o1);
    }
}

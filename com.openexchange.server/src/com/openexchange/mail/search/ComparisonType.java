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

package com.openexchange.mail.search;

/**
 * {@link ComparisonType}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ComparisonType {

    LESS_THAN(javax.mail.search.ComparisonTerm.LT),
    EQUALS(javax.mail.search.ComparisonTerm.EQ),
    GREATER_THAN(javax.mail.search.ComparisonTerm.GT),
    LESS_EQUALS(javax.mail.search.ComparisonTerm.LE),
    GREATER_EQUALS(javax.mail.search.ComparisonTerm.GE);

    private final int ct;

    private ComparisonType(int ct) {
        this.ct = ct;
    }

    /**
     * Gets the <i><a href="http://java.sun.com/products/javamail/">JavaMail</a></i> constant for this comparison type.
     *
     * @return The <i>JavaMail</i> constant for this comparison type
     */
    public int getType() {
        return ct;
    }
}

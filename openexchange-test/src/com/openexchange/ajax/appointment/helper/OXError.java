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

package com.openexchange.ajax.appointment.helper;

import com.openexchange.exception.OXException;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXError {

    private String category;

    private int number;

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public OXError(final String category, final int number) {
        setNumber(number);
        setCategory(category);
    }

    public boolean matches(final OXError other) {
        boolean matchesNumber = false, matchesCategory = false;

        if (category == null || other.getCategory() == null) {
            matchesCategory = true;
        } else {
            matchesCategory = category.equals(other.getCategory());
        }

        if (number == -1 || other.getNumber() == -1) {
            matchesNumber = true;
        } else {
            matchesNumber = number == other.getNumber();
        }

        return matchesNumber && matchesCategory;
    }

    public boolean matches(final OXException exception) {
        return matches(new OXError(exception.getPrefix(), exception.getCode()));
    }

    public boolean matches(final Throwable t) {
        try {
            final OXException exception = (OXException) t;
            return matches(new OXError(exception.getPrefix(), exception.getCode()));
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%3s-%04d", category, Integer.valueOf(number));
    }
}

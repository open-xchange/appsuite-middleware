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

package com.openexchange.i18n;


/**
 * {@link LocalizableArgument} - Represents an argument that is localizable.
 * <p>
 * Passed <code>argument</code> needs to be contained in a class, which implements the {@link LocalizableStrings} interface,
 * in order to be translatable to the appropriate language.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LocalizableArgument implements Localizable {

    private final String argument;

    /**
     * Initializes a new {@link LocalizableArgument}.
     */
    public LocalizableArgument(String argument) {
        super();
        this.argument = null == argument ? "null" : argument;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    @Override
    public String toString() {
        return argument;
    }

}

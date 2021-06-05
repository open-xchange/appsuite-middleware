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

package com.openexchange.config;

/**
 * {@link Interests} - Signals the interests of a {@link Reloadable} instance in certain {@link #getPropertiesOfInterest() property names} and/or {@link #getConfigFileNames() configuration files}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface Interests {

    /**
     * Gets the names of the properties of interest.
     * <p>
     * Each value of the returned string array describes the properties in which this <code>Reloadable</code> is interested.
     * An asterisk ('*') may be used as a trailing wild-card.
     * <p>
     * More precisely, the value of each string must conform to the following grammar:
     *
     * <pre>
     *  properties-description := '*' | property-name ( '.*' )?
     *  property-name := token ( '.' token )*
     * </pre>
     *
     * Examples
     * <pre>
     *  ["com.openexchange.moduleOne.*", "com.openexchange.moduleTwo.attributeFive"]
     *  ["*"]
     * </pre>
     *
     * @return An array of property names or <code>null</code> (interested in no properties)
     */
    String[] getPropertiesOfInterest();

    /**
     * Gets an array of names for configuration files of interest.
     *
     * @return An array of configuration file names or <code>null</code> (interested in no files)
     */
    String[] getConfigFileNames();

}

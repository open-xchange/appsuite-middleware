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

package com.openexchange.folderstorage;

import java.util.Locale;


/**
 * {@link AltNameAwareFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public interface AltNameAwareFolder extends Folder {

    /**
     * Checks if alternative App Suite folder names are supported.
     *
     * @return <code>true</code> if supported; otherwie <code>false</code>
     */
    boolean supportsAltName();

    /**
     * Gets the locale-sensitive name.
     *
     * @param locale The locale
     * @param altName <code>true</code> to prefer alternative App Suite name; otherwise <code>false</code>
     * @return The locale-sensitive name or <code>null</code> if not available
     */
    String getLocalizedName(Locale locale, boolean altName);

}

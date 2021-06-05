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

package com.openexchange.snippet;

/**
 * {@link PropertySwitch} - Adapts the visitor pattern for a snippet's properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface PropertySwitch {

    /**
     * Handles identifier.
     *
     * @return The result object or <code>null</code>
     */
    public Object id();

    /**
     * Handles (unnamed) properties.
     *
     * @return The result object or <code>null</code>
     */
    public Object properties();

    /**
     * Handles content.
     *
     * @return The result object or <code>null</code>
     */
    public Object content();

    /**
     * Handles attachments.
     *
     * @return The result object or <code>null</code>
     */
    public Object attachments();

    /**
     * Handles account identifier.
     *
     * @return The result object or <code>null</code>
     */
    public Object accountId();

    /**
     * Handles type identifier.
     *
     * @return The result object or <code>null</code>
     */
    public Object type();

    /**
     * Handles display name.
     *
     * @return The result object or <code>null</code>
     */
    public Object displayName();

    /**
     * Handles module identifier.
     *
     * @return The result object or <code>null</code>
     */
    public Object module();

    /**
     * Handles creator identifier.
     *
     * @return The result object or <code>null</code>
     */
    public Object createdBy();

    /**
     * Handles shared flag.
     *
     * @return The result object or <code>null</code>
     */
    public Object shared();

    /**
     * Handles miscellaneous JSON data.
     *
     * @return The result object or <code>null</code>
     */
    public Object misc();

}

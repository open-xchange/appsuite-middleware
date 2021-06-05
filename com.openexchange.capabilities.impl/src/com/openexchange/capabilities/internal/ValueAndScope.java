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

package com.openexchange.capabilities.internal;


/**
 * {@link ValueAndScope} - The value and config-cascade scope for a forced capability; e.g. <code>"com.openexchange.capability.forced.mycap"</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
class ValueAndScope {

    /** The config-cascade scope */
    final String scope;

    /** The value; whether to add or not */
    final Boolean value;

    /**
     * Initializes a new {@link ValueAndScope}.
     *
     * @param value The value; whether to add or not
     * @param scope The config-cascade scope
     */
    ValueAndScope(Boolean value, String scope) {
        super();
        this.value = value;
        this.scope = scope;
    }

}

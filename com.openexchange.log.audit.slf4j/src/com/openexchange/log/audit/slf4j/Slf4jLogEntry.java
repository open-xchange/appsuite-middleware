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

package com.openexchange.log.audit.slf4j;

import com.openexchange.log.audit.Attribute;

/**
 * {@link Slf4jLogEntry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Slf4jLogEntry {

    /** The attributes */
    public final Attribute<?>[] attributes;
    
    /** The event identifier */
    public final String entryId;

    /**
     * Initializes a new {@link Slf4jLogEntry}.
     */
    public Slf4jLogEntry(String entryId, Attribute<?>[] attributes) {
        super();
        this.entryId = entryId;
        this.attributes = attributes;
    }

}

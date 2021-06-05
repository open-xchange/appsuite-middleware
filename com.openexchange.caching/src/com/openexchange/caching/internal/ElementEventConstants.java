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

package com.openexchange.caching.internal;

/**
 * {@link ElementEventConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ElementEventConstants {

    /**
     * Initializes a new {@link ElementEventConstants}
     */
    private ElementEventConstants() {
        super();
    }

    /**
     * Background expiration
     */
    final static int ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND = 0;

    /**
     * Expiration discovered on request
     */
    final static int ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST = 1;

    /**
     * Background expiration
     */
    final static int ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND = 2;

    /**
     * Expiration discovered on request
     */
    final static int ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST = 3;

    /**
     * Moving from memory to disk (what if no disk?)
     */
    final static int ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE = 4;

    /**
     * Moving from memory to disk (what if no disk?)
     */
    final static int ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE = 5;

    /**
     * Moving from memory to disk, but item is not spoolable
     */
    final static int ELEMENT_EVENT_SPOOLED_NOT_ALLOWED = 6;
}

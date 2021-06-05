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

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.EventAdmin;
import com.openexchange.caching.Cache;


/**
 * {@link AbstractCache} - The abstract cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public abstract class AbstractCache implements Cache {

    /** The reference to {@link EventAdmin} service */
    protected static final AtomicReference<EventAdmin> EVENT_ADMIN_REF = new AtomicReference<EventAdmin>();

    /**
     * Sets the event admin.
     *
     * @param eventAdmin The event admin or <code>null</code>
     */
    public static void setEventAdmin(final EventAdmin eventAdmin) {
        EVENT_ADMIN_REF.set(eventAdmin);
    }

    /**
     * Initializes a new {@link AbstractCache}.
     */
    protected AbstractCache() {
        super();
    }

}

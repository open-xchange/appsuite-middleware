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

package com.openexchange.jslob.shared;

import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.session.Session;

/**
 * {@link SharedJSlobService} - Service for adding shared JSlobs to JSlobService
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public interface SharedJSlobService {

    /**
     * Topics for EventAdmin
     */
    public static final String EVENT_ADDED = "com/openexchange/jslob/sharedJSlob/added";

    public static final String EVENT_REMOVED = "com/openexchange/jslob/sharedJSlob/removed";

    /**
     * Default id for shared jslobs
     */
    public static final String DEFAULT_ID = "io.ox/shared";

    /**
     * Returns the shared jslob
     *
     * @return The shared jslob
     */
    JSlob getJSlob(Session session) throws OXException;

    /**
     * Returns the jslob's identifier
     *
     * @return The jslob's identifier
     */
    String getId();

    /**
     * Returns the jslob's service identifier
     *
     * @return The jslob's service identifier
     */
    String getServiceId();

}

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

package com.openexchange.authentication;

import com.openexchange.session.Session;

/**
 * {@link SessionEnhancement} - An optional interface which may be implemented by {@link Authenticated} to apply custom properties
 * to specified {@link Session}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> - JavaDoc
 */
public interface SessionEnhancement {

    /**
     * Sets custom properties in specified {@link Session}.
     * <p>
     * Most likely specified session is an instance of <code>SessionDescription</code> providing more possibilities to modify spawned session.
     *
     * @param session The session (description) to enhance
     */
    public void enhanceSession(Session session);

}

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

package com.openexchange.session;

import com.openexchange.session.delegate.DelegatePutIfAbsent;
import com.openexchange.session.delegate.DelegateSession;

/**
 * {@link SetableSessionFactory} - A factory for {@code SetableSession} via {@link #setableSessionFor(Session)}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SetableSessionFactory {

    private static final SetableSessionFactory FACTORY = new SetableSessionFactory();

    /**
     * Gets the factory instance.
     *
     * @return The factory instance
     */
    public static SetableSessionFactory getFactory() {
        return FACTORY;
    }

    // ---------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link SetableSessionFactory}.
     */
    private SetableSessionFactory() {
        super();
    }

    /**
     * Creates a new setable session for specified session.
     *
     * @param session The session
     * @return A setable session
     */
    public SetableSession setableSessionFor(final Session session) {
        if (null == session) {
            return null;
        }
        if (session instanceof PutIfAbsent) {
            return new DelegatePutIfAbsent((PutIfAbsent) session);
        }
        return new DelegateSession(session);
    }

}

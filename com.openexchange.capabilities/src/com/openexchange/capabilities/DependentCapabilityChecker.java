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

package com.openexchange.capabilities;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DependentCapabilityChecker}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class DependentCapabilityChecker implements CapabilityChecker {

    /**
     * Initializes a new {@link DependentCapabilityChecker}.
     */
    protected DependentCapabilityChecker() {
        super();
    }

    /**
     * Check whether the capability should be awarded for a certain user
     *
     * @param capability The capability to check
     * @param session Provides the users session for which to check
     * @param capabilities The capabilities collected so far (declared capabilities are added at the very end)
     * @return Whether to award this capability or not
     * @throws OXException If check fails
     */
    public abstract boolean isEnabled(String capability, Session session, CapabilitySet capabilities) throws OXException;

    @Override
    public boolean isEnabled(String capability, Session session) throws OXException {
        return isEnabled(capability, session, Capabilities.emptyCapabilitySet());
    }

}

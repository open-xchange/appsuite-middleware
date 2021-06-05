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

package com.openexchange.push.dovecot.locking;

import javax.annotation.concurrent.Immutable;
import com.openexchange.push.dovecot.registration.RegistrationContext;

/**
 * {@link SessionInfo} - The session info.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Immutable
public class SessionInfo {

    private final int contextId;
    private final int userId;
    private final String compositeId;
    private final boolean permanent;

    /**
     * Initializes a new {@link SessionInfo}.
     *
     * @param registrationContext The registration context
     * @param permanent Whether permanent or not
     */
    public SessionInfo(RegistrationContext registrationContext, boolean permanent) {
        super();
        this.contextId = registrationContext.getContextId();
        this.userId = registrationContext.getUserId();
        this.compositeId = registrationContext.getUserId() + "@" + registrationContext.getContextId();
        this.permanent = permanent;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the composite identifier.
     *
     * @return The composite identifier
     */
    public String getCompositeId() {
        return compositeId;
    }

    /**
     * Gets the permanent flag.
     *
     * @return The permanent flag
     */
    public boolean isPermanent() {
        return permanent;
    }

}

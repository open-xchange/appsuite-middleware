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

package com.openexchange.oauth.provider.authorizationserver.grant;

import java.util.Date;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * Provides a user-centric view on all grants for a certain client. A user might grant access
 * for a client several times, possibly even with different scopes. An instance of this interface
 * represents a combined view on all grants of a client-user combination.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface GrantView {

    /**
     * Gets the client.
     *
     * @return The client
     */
    Client getClient();

    /**
     * Gets a scope constructed of the super-set of all scope tokens the client has access to.
     * The individual tokens might belong to multiple different grants.
     *
     * @return The scopes
     */
    Scope getScope();

    /**
     * Gets the most recent date when a user granted access for this client.
     *
     * @return The date
     */
    Date getLatestGrantDate();

}

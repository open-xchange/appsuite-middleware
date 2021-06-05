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

package com.openexchange.sessiond;

import java.util.List;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;

/**
 * {@link AddSessionParameter} - The parameter object to create a {@link Session session}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface AddSessionParameter {

    /**
     * Gets the full login incl. context information; e.g <code>test@foo</code>
     *
     * @return The full login
     */
    String getFullLogin();

    /**
     * Gets the password
     *
     * @return The password
     */
    String getPassword();

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the user login information.
     *
     * @return The user login information
     */
    String getUserLoginInfo();

    /**
     * Gets the context of the authenticated user.
     *
     * @return The context.
     */
    Context getContext();

    /**
     * Gets the IP address of the connected client.
     *
     * @return The IP address
     */
    String getClientIP();

    /**
     * Gets the authentication identifier.
     *
     * @return The authentication identifier
     */
    String getAuthId();

    /**
     * Gets the hash.
     *
     * @return The hash
     */
    String getHash();

    /**
     * @return the identifier of the client using the session.
     */
    String getClient();

    /**
     * The client token will only be present when the token login is used. This attribute does not apply to any other login mechanism.
     * @return the client token from the token login. Otherwise <code>null</code>.
     */
    String getClientToken();

    /**
     * Gets a value indicating whether the session should be created in a transient way or not, i.e. the session should not be distributed
     * to other nodes in the cluster or put into another persistent storage.
     *
     * @return <code>true</code> if the session should be transient, <code>false</code>, otherwise.
     */
    boolean isTransient();

    /**
     * Checks whether "stay signed in" should be applied to new session.
     *
     * @return <code>true</code> to apply "stay signed in"; otherwise <code>false</code>
     */
    boolean isStaySignedIn();

    /**
     * A list of callbacks for modifying the session after it is created. This allows to put arbitrary additional information into a newly created
     * session. Normally some parameters are added. Use this to get this arbitrary information published to the whole cluster.
     * @return a list of callbacks for modifying the session after its creation or <code>null</code> if no modification should take place.
     */
    List<SessionEnhancement> getEnhancements();

    /**
     * Gets the identifier of the user-agent using the session.
     *
     * @return The user-agent
     */
    String getUserAgent();

    /**
     * Gets the session's origin.
     *
     * @return The origin or <code>null</code>
     */
    Origin getOrigin();

}

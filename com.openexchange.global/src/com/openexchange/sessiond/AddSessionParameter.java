/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.sessiond;

import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.groupware.contexts.Context;
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
     * A callback for modifying the session after it is created. This allows to put arbitrary additional information into a newly created
     * session. Normally some parameters are added. Use this to get this arbitrary information published to the whole cluster.
     * @return a callback for modifying the session after its creation or <code>null</code> if no modification should take place.
     */
    SessionEnhancement getEnhancement();
}

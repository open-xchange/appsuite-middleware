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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ApiClientService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
@SingletonService
public interface ApiClientService {

    /**
     * Gets a client that can execute requests to another OX server
     *
     * @param session The user's session
     * @param loginLink The link to the login endpoint
     * @param credentials The optional credentials to access the share
     * @return A client
     * @throws OXException In case an access can't be generated
     */
    ApiClient getApiClient(Session session, String loginLink, Optional<Credentials> credentials) throws OXException;

    /**
     * Gets a client that can execute requests to another OX server
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loginLink The link to the login endpoint
     * @param credentials The optional credentials to access the share
     * @return A client
     * @throws OXException In case an access can't be generated
     */
    ApiClient getApiClient(int contextId, int userId, String loginLink, Optional<Credentials> credentials) throws OXException;

    /**
     * Closes the client and logs out from the remote system
     *
     * @param client The client to close
     */
    void close(ApiClient client);

    /**
     * Closes the client and logs out from the remote system
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param loginLink The optional login link the client was created with. If not set, all clients for the user will be removed
     */
    void close(int contextId, int userId, Optional<String> loginLink);

}

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

package com.openexchange.push.dovecot.registration;

import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link RegistrationContext} - The context to use for registration/unregistration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class RegistrationContext {

    /**
     * Provides the DoveAdm client to use.
     */
    public static interface DoveAdmClientProvider {

        /**
         * Provides the DoveAdm client to use.
         *
         * @return The <DoveAdm client
         * @throws OXException If DoveAdm client cannot be returned
         */
        DoveAdmClient getDoveAdmClient() throws OXException;
    }

    // -----------------------------------------------------------------------------------

    /**
     * Creates a DoveAdm client context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param doveAdmClient The DoveAdm client
     * @return The DoveAdm client context
     */
    public static RegistrationContext createDoveAdmClientContext(int userId, int contextId, DoveAdmClientProvider doveAdmClientProvider) {
        return new RegistrationContext(userId, contextId, doveAdmClientProvider, null);
    }

    /**
     * Creates a session context.
     *
     * @param session The associated session
     * @return The session context
     */
    public static RegistrationContext createSessionContext(Session session) {
        return new RegistrationContext(session.getUserId(), session.getContextId(), null, session);
    }

    // -----------------------------------------------------------------------------------

    private final DoveAdmClientProvider doveAdmClientProvider;
    private final Session session;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link RegistrationContext}.
     */
    private RegistrationContext(int userId, int contextId, DoveAdmClientProvider doveAdmClientProvider, Session session) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.doveAdmClientProvider = doveAdmClientProvider;
        this.session = session;
    }

    /**
     * Checks whether this registration context is based on a session.
     *
     * @return <code>true</code> if there is a session associated with this registration context; otherwise <code>false</code>
     */
    public boolean isSessionBased() {
        return null != session;
    }

    /**
     * Checks whether this registration context operates using DoveAdm.
     *
     * @return <code>true</code> if this registration context uses DoveAdm; otherwise <code>false</code>
     */
    public boolean isDoveAdmBased() {
        return null != doveAdmClientProvider;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the DoveAdm client
     *
     * @return The DoveAdm client
     * @throws OXException If DoveAdm client cannot be returned
     */
    public DoveAdmClient getDoveAdmClient() throws OXException {
        return null == doveAdmClientProvider ? null : doveAdmClientProvider.getDoveAdmClient();
    }

    /**
     * Gets the session
     *
     * @return The session or <code>null</code>
     */
    public Session getSession() {
        return session;
    }

}

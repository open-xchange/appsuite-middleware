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

package com.openexchange.push.dovecot.registration;

import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.push.dovecot.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

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
     * @param doveAdmClientProvider The DoveAdm client provider
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

    /**
     * Checks if the user associated with this registration context has 'Webmail' permission and is active.
     *
     * @return <code>true</code> if user holds 'Webmail' permission and is active; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean hasWebMailAndIsActive() throws OXException {
        return session == null ? hasWebMailAndIsActive(userId, contextId) : hasWebMailAndIsActive(session);
    }

    private boolean hasWebMailAndIsActive(Session session) throws OXException {
        // com.openexchange.user.UserExceptionCode.USER_NOT_FOUND
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == serverSession.getUserPermissionBits().hasWebMail()) {
            return false;
        }
        if (false == serverSession.getUser().isMailEnabled()) {
            return false;
        }
        return true;
    }

    private boolean hasWebMailAndIsActive(int userId, int contextId) throws OXException {
        if (false == UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, contextId).hasWebMail()) {
            return false;
        }
        if (false == Services.getServiceLookup().getServiceSafe(UserService.class).getUser(userId, contextId).isMailEnabled()) {
            return false;
        }
        return true;
    }

}

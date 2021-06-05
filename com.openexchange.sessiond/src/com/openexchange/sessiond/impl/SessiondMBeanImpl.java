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

package com.openexchange.sessiond.impl;

import java.util.Set;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import com.openexchange.exception.OXException;
import com.openexchange.management.AnnotatedStandardMBean;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.mbean.SessiondMBean;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessiondMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SuppressWarnings("deprecation")
public final class SessiondMBeanImpl extends AnnotatedStandardMBean implements SessiondMBean {

    /**
     * Initializes a new {@link SessiondMBeanImpl}
     *
     * @throws NotCompliantMBeanException If the mbeanInterface does not follow JMX design patterns for Management Interfaces, or if this
     *             does not implement the specified interface.
     */
    public SessiondMBeanImpl() throws NotCompliantMBeanException {
        super("Management Bean for SessionD service", SessiondMBean.class);
    }

    @Override
    public int clearUserSessions(final int userId, final int contextId) {
        return SessionHandler.removeUserSessions(userId, contextId).length;
    }

    @Override
    public void clearUserSessionsGlobally(int userId, int contextId) throws MBeanException {
        try {
            SessionHandler.removeUserSessionsGlobal(userId, contextId);
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SessiondMBeanImpl.class);
            logger.error("", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public int getNumberOfUserSessons(int userId, int contextId) throws MBeanException {
        return SessionHandler.getNumOfUserSessions(userId, contextId, false);
    }

    @Override
    public void clearContextSessions(final int contextId) {
        /*
         * Clear context-associated sessions
         */
        SessionHandler.removeContextSessions(contextId);
    }

    @Override
    public int[] getNumberOfShortTermSessions() {
        return SessionHandler.getNumberOfShortTermSessions();
    }

    @Override
    public int[] getNumberOfLongTermSessions() {
        return SessionHandler.getNumberOfLongTermSessions();
    }

    @Override
    public void clearSessionStorage() throws MBeanException {
        SessionStorageService storageService = Services.getService(SessionStorageService.class);
        try {
            storageService.cleanUp();
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearContextSessionsGlobal(Set<Integer> contextIds) throws MBeanException {
        final SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();

        try {
            sessiondService.removeContextSessionsGlobal(contextIds);
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }
}

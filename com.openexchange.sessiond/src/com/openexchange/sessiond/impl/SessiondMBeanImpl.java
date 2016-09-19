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

package com.openexchange.sessiond.impl;

import java.util.Set;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import com.openexchange.exception.OXException;
import com.openexchange.management.AnnotatedStandardMBean;
import com.openexchange.sessiond.mbean.SessiondMBean;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessiondMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
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

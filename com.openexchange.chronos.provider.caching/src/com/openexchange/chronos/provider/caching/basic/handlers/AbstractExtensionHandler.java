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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.chronos.provider.caching.basic.handlers;

import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link AbstractExtensionHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractExtensionHandler {

    private final CalendarParameters parameters;
    private final Session session;
    private final CalendarAccount account;
    private final CalendarSession calendarSession;

    /**
     * Initialises a new {@link AbstractExtensionHandler}.
     * 
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param calendarParameters The {@link CalendarParameters}
     * @throws OXException if the property {@link CalendarSession} cannot be initialised
     */
    public AbstractExtensionHandler(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super();
        this.session = session;
        this.account = account;
        this.parameters = parameters;
        this.calendarSession = Services.getService(CalendarService.class).init(session);
    }

    /**
     * Gets the parameters
     *
     * @return The parameters
     */
    public CalendarParameters getParameters() {
        return parameters;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the account
     *
     * @return The account
     */
    public CalendarAccount getAccount() {
        return account;
    }

    /**
     * Gets the calendarSession
     *
     * @return The calendarSession
     */
    public CalendarSession getCalendarSession() {
        return calendarSession;
    }

    /**
     * Initialises and returns a {@link CalendarStorage} for the current {@link Session}
     * 
     * @return The initialised storage
     * @throws OXException if the storage cannot be initialised
     */
    CalendarStorage getStorage() throws OXException {
        ContextService contextService = Services.getService(ContextService.class);
        Context context = contextService.loadContext(getSession().getContextId());

        CalendarStorageFactory storageFactory = Services.getService(CalendarStorageFactory.class);
        return storageFactory.create(context, getAccount().getAccountId(), getCalendarSession().getEntityResolver());
    }

    /**
     * Helper method for getting the {@link EventStorage}
     * 
     * @return The {@link EventStorage}
     * @throws OXException if the {@link EventStorage} cannot be returned
     */
    EventStorage getEventStorage() throws OXException {
        return getStorage().getEventStorage();
    }
}

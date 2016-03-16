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
package com.openexchange.groupware.calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Multiplexes calendar events
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarCallbacks implements CalendarListener {
    private final List<CalendarListener> listeners = new ArrayList<CalendarListener>();
    private List<CalendarListener> copyForReading = new ArrayList<CalendarListener>();

    private static final CalendarCallbacks INSTANCE = new CalendarCallbacks();
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarCallbacks.class);

    private boolean mustCopy;

    private final Lock lock = new ReentrantLock();

    public static CalendarCallbacks getInstance() {
        return INSTANCE;
    }

    public void addListener(final CalendarListener listener) {
        try {
            lock.lock();
            mustCopy = true;
            listeners.add(listener);
        } finally {
            lock.unlock();
        }
    }

    public void removeListener(final CalendarListener listener) {
        try {
            lock.lock();
            mustCopy = true;
            listeners.remove(listener);
        } finally {
            lock.unlock();
        }
    }

    public List<CalendarListener> getListeners() {
        if (mustCopy) {
            try {
                lock.lock();
                copyForReading = new ArrayList<CalendarListener>(listeners);
            } finally {
                lock.unlock();
            }
        }
        return copyForReading;
    }

    public void createdChangeExceptionInRecurringAppointment(final Appointment master, final Appointment changeException, final int inFolder, final Session session) throws OXException {
        final ServerSession serverSession = getServerSession(session);
        createdChangeExceptionInRecurringAppointment(master, changeException, inFolder, serverSession);
    }

    private ServerSession getServerSession(final Session session) throws OXException {
        if(ServerSession.class.isAssignableFrom(session.getClass())) {
            return (ServerSession) session;
        }
        return ServerSessionAdapter.valueOf(session);
    }

    @Override
    public void createdChangeExceptionInRecurringAppointment(final Appointment master, final Appointment changeException,final int inFolder, final ServerSession serverSession) throws OXException {
        final List<String> exceptionIDs = new ArrayList<String>();
        for (final CalendarListener listener : getListeners()) {
            try {
                listener.createdChangeExceptionInRecurringAppointment(master, changeException,inFolder, serverSession);
            } catch (final OXException x) {
                LOG.error("", x);
                exceptionIDs.add(x.getExceptionId());
            }
        }
        if(!exceptionIDs.isEmpty()) {
            throw OXCalendarExceptionCodes.CALLBACK_EXCEPTIONS.create(exceptionIDs.toString());
        }
    }
}

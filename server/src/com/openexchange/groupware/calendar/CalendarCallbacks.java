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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Multiplexes calendar events
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarCallbacks implements CalendarListener {
    private List<CalendarListener> listeners = new ArrayList<CalendarListener>();
    private List<CalendarListener> copyForReading = new ArrayList<CalendarListener>();
    
    private static final CalendarCallbacks INSTANCE = new CalendarCallbacks();
    private static final Log LOG = LogFactory.getLog(CalendarCallbacks.class);

    private boolean mustCopy;

    private Lock lock = new ReentrantLock();

    public static CalendarCallbacks getInstance() {
        return INSTANCE;
    }

    public void addListener(CalendarListener listener) {
        try {
            lock.lock();
            mustCopy = true;
            listeners.add(listener);
        } finally {
            lock.unlock();
        }
    }

    public void removeListener(CalendarListener listener) {
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

    public void createdChangeExceptionInRecurringAppointment(AppointmentObject master, AppointmentObject changeException, int inFolder, Session session) throws AbstractOXException {
        ServerSession serverSession = getServerSession(session);
        createdChangeExceptionInRecurringAppointment(master, changeException, inFolder, serverSession);
    }

    private ServerSession getServerSession(Session session) throws ContextException {
        if(ServerSession.class.isAssignableFrom(session.getClass())) {
            return (ServerSession) session;
        }
        return new ServerSessionAdapter(session);
    }

    public void createdChangeExceptionInRecurringAppointment(AppointmentObject master, AppointmentObject changeException,int inFolder, ServerSession serverSession) throws AbstractOXException {
        List<String> exceptionIDs = new ArrayList<String>();
        for (CalendarListener listener : getListeners()) {
            try {
                listener.createdChangeExceptionInRecurringAppointment(master, changeException,inFolder, serverSession);
            } catch (AbstractOXException x) {
                LOG.error(x.getMessage(), x);
                exceptionIDs.add(x.getExceptionID());
            }
        }
        if(!exceptionIDs.isEmpty()) {
            throw new OXCalendarException(OXCalendarException.Code.CALLBACK_EXCEPTIONS, exceptionIDs.toString());
        }
    }
}

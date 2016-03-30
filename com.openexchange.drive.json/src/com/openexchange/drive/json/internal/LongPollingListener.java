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

package com.openexchange.drive.json.internal;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.session.Session;

/**
 * {@link LongPollingListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LongPollingListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LongPollingListener.class);

    private final String rootFolderID;
    private final Session session;
    private final ReentrantLock lock;
    private final Condition hasEvent;
    private DriveEvent event;

    /**
     * Initializes a new {@link LongPollingListener}.
     *
     * @param session The session
     * @param rootFolderID The root folder ID
     */
    public LongPollingListener(Session session, String rootFolderID) {
        super();
        this.session = session;
        this.rootFolderID = rootFolderID;
        this.lock = new ReentrantLock();
        this.hasEvent = this.lock.newCondition();
    }

    public void onEvent(DriveEvent event) {
        if (false == isInteresting(event)) {
            LOG.debug("Skipping uninteresting event: {}", event);
            return;
        }
        lock.lock();
        try {
            this.event = event;
            this.hasEvent.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Blocks until event data is available, or the specified waiting time elapses. May return immediately if a previously received
     * but not yet processed event is available.
     *
     * @param timeout The timeout in milliseconds to wait
     * @return the event if available, <code>null</code>, otherwise
     * @throws InterruptedException
     */
    public DriveEvent await(long timeout) throws InterruptedException {
        DriveEvent data = null;
        lock.lock();
        try {
            if (null == this.event) {
                LOG.debug("Awaiting events for max. {}ms...", timeout);
                hasEvent.await(timeout, TimeUnit.MILLISECONDS);
            } else {
                LOG.debug("Stored event available, no need to wait.");
            }
            data = this.event;
            this.event = null;
        } finally {
            lock.unlock();
        }
        if (null == data) {
            LOG.debug("No event available.");
        } else {
            LOG.debug("Available event: {}", data);
        }
        return data;
    }

    private boolean isInteresting(DriveEvent event) {
        if (null != event) {
//            Event osgiEvent = event.getEvent();
//            if (null != osgiEvent) {
//                Session session = (Session)osgiEvent.getProperty(FileStorageEventConstants.SESSION);
//                if (null != session && this.session.getSessionID().equals(session.getSessionID())) {
//                    return false;
//                }
//            }
            return true;
        }
        return false;
    }

    /**
     * Gets the rootFolderID
     *
     * @return The rootFolderID
     */
    public String getRootFolderID() {
        return rootFolderID;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    @Override
    public String toString() {
        return "LongPollingListener [sessionID=" + session.getSessionID() + ", " +
        		"rootFolderID=" + rootFolderID + ", contextID=" + session.getContextId() + "]";
    }

}

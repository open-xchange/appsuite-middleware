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

package com.openexchange.monitoring;

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.counter.Counter;

/**
 * {@link MonitoringInfo} - Container for various monitoring information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MonitoringInfo {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MonitoringInfo.class);

    // Constants for connection types

    /**
     * The connection type constant for open connections/requests to AJAX interface.
     */
    public static final int AJAX = 1;

    /**
     * The connection type constant for open connections/requests to Outlook WebDAV/XML interface.
     */
    public static final int OUTLOOK = 2;

    public static final int WEBDAV_STORAGE = 3;

    public static final int WEBDAV_USER = 4;

    public static final int SYNCML = 5;

    /**
     * The connection type constant for open connections to mail back-ends.
     */
    public static final int IMAP = 6;

    /**
     * The connection type constant for the number of open sessions.
     */
    public static final int SESSION = 7;

    /**
     * The connection type constant for the number of idling mail connections waiting for server notifications.
     */
    public static final int MAIL_IDLE = 8;

    /**
     * Unknown connection type
     */
    public static final int UNKNOWN = -1;

    // Static fields
    private static final AtomicInteger numberOfAJAXConnections = new AtomicInteger();

    private static final AtomicInteger numberOfWebDAVUserConnections = new AtomicInteger();

    private static final AtomicInteger numberOfOutlookConnections = new AtomicInteger();

    private static final AtomicInteger numberOfSyncMLConnections = new AtomicInteger();

    private static final AtomicInteger numberOfActiveSessions = new AtomicInteger();

    private static int[] numberOfSessionsInContainer;

    private static final ConcurrentMap<Integer, Counter> COUNTER_MAP = new ConcurrentHashMap<Integer, Counter>(8, 0.9f, 1);

    private static final Counter EMPTY_COUNTER = new Counter() {

        @Override
        public int getCount() {
            return 0;
        }
    };

    public static boolean putIfAbsent(final int connectionType, final Counter value) {
        return null == COUNTER_MAP.putIfAbsent(Integer.valueOf(connectionType), value);
    }

    public static void remove(final int connectionType) {
        COUNTER_MAP.remove(Integer.valueOf(connectionType));
    }

    private static Counter getCounter(final int connectionType) {
        final Counter counter = COUNTER_MAP.get(Integer.valueOf(connectionType));
        return null == counter ? EMPTY_COUNTER : counter;
    }

    public static int getNumberOfActiveSessions() {
        return getNumberOfConnections(SESSION);
    }

    public static void incrementNumberOfActiveSessions() {
        changeNumberOfConnections(SESSION, true);
    }

    public static void decrementNumberOfActiveSessions() {
        changeNumberOfConnections(SESSION, false);
    }

    public static void decrementNumberOfActiveSessions(final int number) {
        for (int a = 0; a < number; a++) {
            decrementNumberOfActiveSessions();
        }
    }

    public static int getNumberOfAJAXConnections() {
        return getNumberOfConnections(AJAX);
    }

    /**
     * Omit call to this method if <code>connectionType</code> is {@link #IMAP}.
     */
    public static void incrementNumberOfConnections(final int connectionType) {
        changeNumberOfConnections(connectionType, true);
    }

    /**
     * Omit call to this method if <code>connectionType</code> is {@link #IMAP}.
     */
    public static void decrementNumberOfConnections(final int connectionType) {
        changeNumberOfConnections(connectionType, false);
    }

    public int getNumberOfConnectionsPerSecond(final int connectionType) throws InterruptedException {
        final int firstVal = getNumberOfConnections(connectionType);
        Thread.sleep(1000);
        return getNumberOfConnections(connectionType) - firstVal;
    }

    public static int getNumberOfConnections(final int connectionType) {
        int retval = -1;
        switch (connectionType) {
        case AJAX:
            retval = numberOfAJAXConnections.get();
            break;
        case WEBDAV_USER:
            retval = numberOfWebDAVUserConnections.get();
            break;
        case OUTLOOK:
            retval = numberOfOutlookConnections.get();
            break;
        case SYNCML:
            retval = numberOfSyncMLConnections.get();
            break;
        case IMAP:
            retval = getCounter(connectionType).getCount();
            break;
        case SESSION:
            retval = numberOfActiveSessions.get();
            break;
        case MAIL_IDLE:
            retval = getCounter(connectionType).getCount();
            break;
        default:
            LOG.error("MonitoringInfo.getNumberOfConnections(): Unknown connection type: {}", I(connectionType));
        }
        return retval;
    }

    private static void changeNumberOfConnections(final int connectionType, final boolean increment) {
        switch (connectionType) {
        case AJAX:
            if (increment) {
                numberOfAJAXConnections.incrementAndGet();
            } else {
                numberOfAJAXConnections.decrementAndGet();
            }
            break;
        case WEBDAV_USER:
            if (increment) {
                numberOfWebDAVUserConnections.incrementAndGet();
            } else {
                numberOfWebDAVUserConnections.decrementAndGet();
            }
            break;
        case OUTLOOK:
            if (increment) {
                numberOfOutlookConnections.incrementAndGet();
            } else {
                numberOfOutlookConnections.decrementAndGet();
            }
            break;
        case SYNCML:
            if (increment) {
                numberOfSyncMLConnections.incrementAndGet();
            } else {
                numberOfSyncMLConnections.decrementAndGet();
            }
            break;
        case IMAP:
            // Nothing to do
            break;
        case SESSION:
            if (increment) {
                numberOfActiveSessions.incrementAndGet();
            } else {
                numberOfActiveSessions.decrementAndGet();
            }
            break;
        case MAIL_IDLE:
            // Nothing to do
            break;
        default:
            LOG.info("MonitoringInfo.changeNumberOfConnections(): Unknown connection type: {}", I(connectionType));
        }
    }

    public static void setNumberOfSessionsInContainer(final int[] numberOfSessionsInContainer) {
        MonitoringInfo.numberOfSessionsInContainer = new int[numberOfSessionsInContainer.length];
        System.arraycopy(numberOfSessionsInContainer, 0, MonitoringInfo.numberOfSessionsInContainer, 0, numberOfSessionsInContainer.length);
    }

    public static int[] getNumberOfSessionsInContainer() {
        final int[] retval = new int[numberOfSessionsInContainer.length];
        System.arraycopy(numberOfSessionsInContainer, 0, retval, 0, retval.length);
        return retval;
    }
}

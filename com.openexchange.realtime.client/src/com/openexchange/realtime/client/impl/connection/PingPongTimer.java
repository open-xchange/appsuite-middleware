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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.realtime.client.impl.connection;

import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PingPongTimer}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class PingPongTimer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PingPongTimer.class);

    private final RTProtocolCallback callback;

    private final long idleTime;

    private final boolean commit;

    private final AtomicLong lastContact;

    private final AtomicLong lastPong;

    public PingPongTimer(final RTProtocolCallback callback, final long idleTime, final boolean commit) {
        super();
        this.callback = callback;
        this.idleTime = idleTime;
        this.commit = commit;
        lastContact = new AtomicLong(System.currentTimeMillis());
        lastPong = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                logInterruption();
                return;
            }

            long now = System.currentTimeMillis();
            LOG.debug("Now: {}", now);
            LOG.debug("lastContact: {}", lastContact.get());
            LOG.debug("idleTime: {}", idleTime);
            long timeToPing = lastContact.get() + idleTime;
            LOG.debug("timeToPing adds up to: {}", timeToPing);
            try {
                if (timeToPing <= now) {
                    LOG.debug("timeToPing <= now");
                    /*
                     * {"type": "ping", "commit": true }
                     */
                    try {
                        JSONObject ping = new JSONObject();
                        ping.put("type", "ping");
                        ping.put("commit", commit);

                        callback.sendPing(ping);
                        resetPingTimer0();
                    } catch (Throwable e) {
                        LOG.warn("Could not send PING.", e);
                    }

                    if (commit && lastPong.get() < (System.currentTimeMillis() - 120000L)) {
                        callback.onTimeout();
                    }
                    Thread.sleep(idleTime);
                } else {
                    LOG.debug("Thread.sleep(timeToPing - System.currentTimeMillis()): {}", timeToPing - System.currentTimeMillis());
                    Thread.sleep(timeToPing - System.currentTimeMillis());
                }
            } catch (InterruptedException e) {
                logInterruption();
                return;
            }
        }
    }

    public void resetPingTimer() {
        resetPingTimer0();
    }

    public void resetPongTimer() {
        resetPongTimer0();
    }

    public void onPong() {
        resetPongTimer0();
    }

    private void resetPingTimer0() {
        lastContact.set(System.currentTimeMillis());
    }

    private void resetPongTimer0() {
        lastPong.set(System.currentTimeMillis());
    }

    private void logInterruption() {
        LOG.info("PingPongTimer shuts down due to interrupt...");
    }
}

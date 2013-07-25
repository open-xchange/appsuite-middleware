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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.realtime.client.RTException;


/**
 * {@link ResendBuffer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ResendBuffer {

    private static final Logger LOG = LoggerFactory.getLogger(ResendBuffer.class);

    private static final int RESEND_LIMIT = 100;

    private final ConcurrentMap<Long, ResendTask> activeTimers;

    private final AbstractRTConnection connection;

    private final Timer timer;

    public ResendBuffer(AbstractRTConnection connection) {
        super();
        this.connection = connection;
        timer = new Timer();
        activeTimers = new ConcurrentHashMap<Long, ResendTask>();
        LOG.info("ResendBuffer started...");
    }

    public void put(long seq, JSONValue message) {
        ResendTask task = new ResendTask(connection, seq, message);
        timer.scheduleAtFixedRate(task, 0L, 3000L);
        activeTimers.put(seq, task);
    }

    public void remove(long seq) {
        ResendTask task = activeTimers.remove(seq);
        if (task != null) {
            task.cancel();
            timer.purge();
        }
    }

    public void stop() {
        LOG.info("ResendBuffer shuts down...");
        timer.cancel();
    }

    private static final class ResendTask extends TimerTask {

        private final AbstractRTConnection connection;

        private final long seq;

        private final JSONValue message;

        private int resendCount;

        public ResendTask(AbstractRTConnection connection, long seq, JSONValue message) {
            super();
            this.connection = connection;
            this.seq = seq;
            this.message = message;
            resendCount = 0;
        }

        @Override
        public void run() {
            try {
                if (resendCount == RESEND_LIMIT) {
                    LOG.error("Could not send message " + seq + " after " + RESEND_LIMIT + " tries.");
                    cancel();
                }

                connection.doSend(message);
                resendCount++;
            } catch (RTException e) {
                LOG.warn("Error while sending message " + seq + ". Resend count is: " + resendCount, e);
            }
        }

    }

}

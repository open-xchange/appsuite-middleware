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

package com.openexchange.saml.state;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link SimStateManagement}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SimStateManagement implements StateManagement {

    private final Map<String, TimedEntry<AuthnRequestInfo>> authnRequests = new HashMap<String, TimedEntry<AuthnRequestInfo>>();

    private final Map<String, TimedEntry<LogoutRequestInfo>> logoutRequests = new HashMap<String, TimedEntry<LogoutRequestInfo>>();

    private final Map<String, TimedEntry<String>> authnResponses = new HashMap<String, TimedEntry<String>>();

    private final Timer timer = new Timer();

    public SimStateManagement() {
        super();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeOut(authnRequests);
                timeOut(logoutRequests);
                timeOut(authnResponses);
            }

            private <T> void timeOut(Map<String, TimedEntry<T>> map) {
                List<String> timedOutKeys = new LinkedList<String>();
                long now = System.currentTimeMillis();
                for (Entry<String, TimedEntry<T>> entry : map.entrySet()) {
                    if (now >= entry.getValue().getTimeout()) {
                        timedOutKeys.add(entry.getKey());
                    }
                }

                for (String key : timedOutKeys) {
                    map.remove(key);
                }
            }

        }, 100l, 100l);
    }

    @Override
    public String addAuthnRequestInfo(AuthnRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException {
        String id = generateID();
        authnRequests.put(id, new TimedEntry<AuthnRequestInfo>(requestInfo, ttl, timeUnit));
        return id;
    }

    @Override
    public AuthnRequestInfo removeAuthnRequestInfo(String id) throws OXException {
        TimedEntry<AuthnRequestInfo> entry = authnRequests.get(id);
        if (entry == null) {
            return null;
        }

        return entry.getValue();
    }

    @Override
    public void addAuthnResponseID(String responseID, long ttl, TimeUnit timeUnit) throws OXException {
        authnResponses.put(responseID, new TimedEntry<String>(responseID, ttl, timeUnit));
    }

    @Override
    public boolean hasAuthnResponseID(String responseID) throws OXException {
        return authnResponses.containsKey(responseID);
    }

    @Override
    public String addLogoutRequestInfo(LogoutRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException {
        String id = generateID();
        logoutRequests.put(id, new TimedEntry<LogoutRequestInfo>(requestInfo, ttl, timeUnit));
        return id;
    }

    @Override
    public LogoutRequestInfo removeLogoutRequestInfo(String id) throws OXException {
        TimedEntry<LogoutRequestInfo> entry = logoutRequests.get(id);
        if (entry == null) {
            return null;
        }

        return entry.getValue();
    }

    private static String generateID() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    private static final class TimedEntry<T> {

        private final T value;

        private final long timeout;

        public TimedEntry(T value, long ttl, TimeUnit timeUnit) {
            super();
            this.value = value;
            timeout = System.currentTimeMillis() +  timeUnit.toMillis(ttl);
        }

        public T getValue() {
            return value;
        }

        public long getTimeout() {
            return timeout;
        }

    }

}

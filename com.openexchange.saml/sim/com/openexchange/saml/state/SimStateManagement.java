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
            @SuppressWarnings("synthetic-access")
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

    public String addLogoutRequestInfo(String id, LogoutRequestInfo requestInfo, long ttl, TimeUnit timeUnit) {
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

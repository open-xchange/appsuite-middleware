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

package com.openexchange.authentication.kerberos.impl;

import static com.openexchange.kerberos.KerberosUtils.SESSION_PRINCIPAL;
import static com.openexchange.kerberos.KerberosUtils.disposeSubject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.KerberosUtils;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.timer.TimerService;

/**
 * Starts a ticket renewal timer for every created session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class DelegationTicketLifecycle implements LoginHandlerService, EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DelegationTicketLifecycle.class);

    private final KerberosService kerberosService;
    private final TimerService timerService;
    private final Map<String, TicketRenewalTimer> timers = new ConcurrentHashMap<String, TicketRenewalTimer>();

    public DelegationTicketLifecycle(KerberosService kerberosService, TimerService timerService) {
        super();
        this.kerberosService = kerberosService;
        this.timerService = timerService;
    }

    @Override
    public void handleLogin(LoginResult result) throws OXException {
        Session session = result.getSession();
        scheduleTicketRenewal(session);
    }

    private void scheduleTicketRenewal(Session session) throws OXException {
        TicketRenewalTimer kt = new TicketRenewalTimer(session, kerberosService, timerService);
        kt.start();
        timers.put(session.getSessionID(), kt);
    }

    @Override
    public void handleLogout(LoginResult logout) {
        removeTicketRenewalTimer(logout.getSession().getSessionID());
    }

    public void stopAll() {
        for (TicketRenewalTimer kt : timers.values()) {
            kt.cancel();
        }
        timers.clear();
    }

    @Override
    public void handleEvent(Event event) {
        final String topic = event.getTopic();
        if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
            handleRemovedSession((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
        } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
            @SuppressWarnings("unchecked")
            final Map<String, Session> container = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
            for (final Session session : container.values()) {
                handleRemovedSession(session);
            }
        } else if (KerberosUtils.TOPIC_TICKET_READDED.equals(topic)) {
            // a new ticket was transferred to the backend through action ticketReload after it expired. Now schedule to expire the new
            // ticket.
            try {
                scheduleTicketRenewal((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
            } catch (OXException e) {
                LOG.error("Can not schedule ticket renewal.", e);
            }
        }
    }

    private void handleRemovedSession(Session session) {
        removeTicketRenewalTimer(session.getSessionID());
        final ClientPrincipal principal = (ClientPrincipal) session.getParameter(SESSION_PRINCIPAL);
        if (null != principal) {
            disposeSubject(principal.getClientSubject());
            disposeSubject(principal.getDelegateSubject());
        }
    }

    private void removeTicketRenewalTimer(String sessionId) {
        final TicketRenewalTimer timer = timers.remove(sessionId);
        if (null != timer) {
            timer.cancel();
        }
    }
}

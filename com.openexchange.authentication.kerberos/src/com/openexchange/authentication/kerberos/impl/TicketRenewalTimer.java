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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.kerberos.KerberosUtils.SESSION_PRINCIPAL;
import static com.openexchange.kerberos.KerberosUtils.SESSION_SUBJECT;
import static com.openexchange.kerberos.KerberosUtils.disposeSubject;
import static com.openexchange.kerberos.KerberosUtils.getName;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.security.auth.Subject;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosExceptionCodes;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.kerberos.KerberosUtils;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * Timer for renewing the Kerberos ticket in a session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
class TicketRenewalTimer implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TicketRenewalTimer.class);

    private final Session session;
    private final KerberosService kerberosService;
    private final TimerService timerService;
    private ScheduledTimerTask scheduled;

    public TicketRenewalTimer(Session session, KerberosService kerberosService, TimerService timerService) {
        super();
        this.session = session;
        this.kerberosService = kerberosService;
        this.timerService = timerService;
    }

    public void start() throws OXException {
        final ClientPrincipal principal = (ClientPrincipal) session.getParameter(SESSION_PRINCIPAL);
        if (null == principal) {
            throw KerberosExceptionCodes.TICKET_MISSING.create(session.getSessionID());
        }
        schedule(principal.getClientSubject(), principal.getDelegateSubject());
    }

    private void schedule(final Subject clientSubject, Subject delegateSubject) throws OXException {
        OXException exc = null;
        int clientSubjectExpires;
        try {
            clientSubjectExpires = KerberosUtils.calculateRenewalTime(clientSubject);
        } catch (OXException e) {
            exc = e;
            clientSubjectExpires = Integer.MAX_VALUE;
        }
        int delegateSubjectExpires;
        try {
            delegateSubjectExpires = KerberosUtils.calculateRenewalTime(delegateSubject);
        } catch (OXException e) {
            exc = e;
            delegateSubjectExpires = Integer.MAX_VALUE;
        }
        final int ticketExpiresInSeconds = Math.min(clientSubjectExpires, delegateSubjectExpires);
        if (Integer.MAX_VALUE == ticketExpiresInSeconds && null != exc) {
            throw exc;
        }
        {
            Object name = new Object() {
                @Override
                public String toString() {
                    return getName(clientSubject);
                }
            };
            Object time = new Object() {
                @Override
                public String toString() {
                    Calendar cal = new GregorianCalendar(TimeZones.UTC, Locale.ENGLISH);
                    cal.add(Calendar.SECOND, ticketExpiresInSeconds);
                    return cal.getTime().toString();
                }
            };
            LOG.debug("Ticket for {} expires at {}. Running timer in {} seconds.", name, time, I(ticketExpiresInSeconds));
        }
        scheduled = timerService.schedule(this, ticketExpiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        final ClientPrincipal principal = (ClientPrincipal) session.getParameter(SESSION_PRINCIPAL);
        if (null == principal) {
            OXException e = KerberosExceptionCodes.TICKET_MISSING.create(session.getSessionID());
            LOG.error(e.getMessage(), e);
            return;
        }
        LOG.debug("Ticket for {} expired.", getName(principal.getClientSubject()));
        if (principal.isSPNEGO()) {
            // we got a ticket through SPNEGO. Just delete the ticket in the session and we will refetch it from the client like after a
            // session migration where transfering the ticket through hazelcast and serialization is prohibited.
            session.setParameter(SESSION_PRINCIPAL, null);
            session.setParameter(SESSION_SUBJECT, null);
        } else if (null != session.getPassword()) {
            try {
                ClientPrincipal newPrincipal = kerberosService.authenticate(session.getLogin(), session.getPassword());
                session.setParameter(SESSION_PRINCIPAL, newPrincipal);
                session.setParameter(SESSION_SUBJECT, newPrincipal.getDelegateSubject());
                schedule(newPrincipal.getClientSubject(), newPrincipal.getDelegateSubject());
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        disposeSubject(principal.getClientSubject());
        disposeSubject(principal.getDelegateSubject());
    }

    public void cancel() {
        scheduled.cancel();
    }
}

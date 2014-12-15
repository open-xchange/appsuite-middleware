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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
        schedule(principal.getDelegateSubject());
    }

    private void schedule(Subject subject) throws OXException {
        final int ticketExpiresInSeconds = KerberosUtils.calculateRenewalTime(subject);
        if (LOG.isDebugEnabled()) {
            Calendar cal = new GregorianCalendar(TimeZones.UTC, Locale.ENGLISH);
            cal.add(Calendar.SECOND, ticketExpiresInSeconds);
            LOG.debug("Ticket for {} expires in {}. Running timer in {} seconds.", getName(subject), cal.toString(), I(ticketExpiresInSeconds));
        }
        scheduled = timerService.schedule(this, ticketExpiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            final ClientPrincipal principal = (ClientPrincipal) session.getParameter(SESSION_PRINCIPAL);
            if (null == principal) {
                throw KerberosExceptionCodes.TICKET_MISSING.create(session.getSessionID());
            }
            ClientPrincipal newPrincipal = kerberosService.verifyAndDelegate(principal.getClientTicket());
            session.setParameter(SESSION_PRINCIPAL, newPrincipal);
            final Subject subject = newPrincipal.getDelegateSubject();
            session.setParameter(SESSION_SUBJECT, subject);
            schedule(subject);
            disposeSubject(principal.getClientSubject());
            disposeSubject(principal.getDelegateSubject());
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    public void cancel() {
        scheduled.cancel();
    }
}